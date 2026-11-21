package com.agentgo.file.support

import com.agentgo.file.service.FileService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionSynchronization
import org.springframework.transaction.support.TransactionSynchronizationManager
import org.springframework.transaction.support.TransactionTemplate

/**
 * Coordinates JDBC units of work with MinIO object lifecycle.
 *
 * Object storage is not enlisted in the database transaction. Upload stores the
 * object first, then persists metadata and compensates by deleting the object when
 * the database work fails. Delete removes metadata first and deletes the object
 * only after a successful commit so a rolled-back delete cannot leave metadata
 * pointing at a missing object.
 *
 * @property fileService MinIO object operations used for compensation and deferred deletes
 */
@Component
class FileStorageTransactionSupport(
    transactionManager: PlatformTransactionManager,
    private val fileService: FileService,
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val writeTransaction = TransactionTemplate(transactionManager)
    private val readTransaction = TransactionTemplate(transactionManager).apply {
        isReadOnly = true
    }

    /**
     * Runs [action] inside a read-only database transaction.
     *
     * @param action unit of work that only reads persistent state
     * @return the value returned by [action]
     */
    fun <T> readOnly(action: () -> T): T =
        readTransaction.execute { action() } ?: error("read-only transaction returned null")

    /**
     * Runs [action] inside a write database transaction.
     *
     * @param action unit of work that may mutate persistent state
     * @return the value returned by [action]
     */
    fun <T> write(action: () -> T): T =
        writeTransaction.execute { action() } ?: error("write transaction returned null")

    /**
     * Persists metadata for an object that was already stored at [objectKey].
     *
     * When the write transaction fails and [compensateOnFailure] is `true`, the
     * stored object is deleted so a brand-new upload does not leave orphan storage.
     * Callers that overwrite an object that already had metadata should pass
     * `compensateOnFailure = false` so a failed metadata update cannot delete the
     * only remaining object while the previous metadata row still points at it.
     *
     * @param objectKey MinIO object key that was just written
     * @param compensateOnFailure whether to delete [objectKey] when [action] fails
     * @param action metadata persistence unit of work
     * @return the value returned by [action]
     */
    fun <T> commitMetadataForStoredObject(
        objectKey: String,
        compensateOnFailure: Boolean = true,
        action: () -> T,
    ): T =
        try {
            write(action)
        } catch (failure: Exception) {
            if (compensateOnFailure) {
                compensateStoredObject(objectKey, failure)
            }
            throw failure
        }

    /**
     * Deletes [objectKey] from MinIO after the surrounding write transaction commits.
     *
     * @param objectKey MinIO object key to remove after commit
     * @throws IllegalStateException when called outside an active transaction
     */
    fun deleteObjectAfterCommit(objectKey: String) {
        deleteObjectsAfterCommit(listOf(objectKey))
    }

    /**
     * Deletes [objectKeys] from MinIO after the surrounding write transaction commits.
     *
     * @param objectKeys MinIO object keys to remove after commit
     * @throws IllegalStateException when called outside an active transaction
     */
    fun deleteObjectsAfterCommit(objectKeys: Collection<String>) {
        val keys = objectKeys.filter { it.isNotBlank() }.distinct()
        if (keys.isEmpty()) {
            return
        }
        check(TransactionSynchronizationManager.isSynchronizationActive()) {
            "Object deletion after commit requires an active transaction"
        }
        TransactionSynchronizationManager.registerSynchronization(
            object : TransactionSynchronization {
                override fun afterCommit() {
                    keys.forEach { key ->
                        try {
                            fileService.deleteObject(key)
                        } catch (failure: Exception) {
                            logger.warn("Failed to delete MinIO object after commit: {}", key, failure)
                        }
                    }
                }
            },
        )
    }

    private fun compensateStoredObject(objectKey: String, cause: Exception) {
        try {
            fileService.deleteObject(objectKey)
        } catch (compensationFailure: Exception) {
            logger.error(
                "Failed to compensate MinIO object after metadata transaction failure: {}",
                objectKey,
                compensationFailure,
            )
            cause.addSuppressed(compensationFailure)
        }
    }
}
