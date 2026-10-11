package com.agentgo.auth.service.impl

import com.agentgo.auth.service.AvatarService
import com.agentgo.commons.api.ApiPaths
import com.agentgo.dto.file.FileMetadataResponse
import com.agentgo.file.exception.EmptyFileException
import com.agentgo.file.exception.FileException
import com.agentgo.file.exception.FileTooLargeException
import com.agentgo.file.mapper.FileMapper
import com.agentgo.file.properties.FileProperties
import com.agentgo.file.repository.FileRepository
import com.agentgo.file.service.FileService
import com.agentgo.file.service.bytes
import com.agentgo.file.storage.StoredFile
import com.agentgo.file.support.FileStorageTransactionSupport
import java.security.MessageDigest
import java.util.Base64
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

/**
 * Avatar upload and download with programmatic DB transactions and MinIO compensation.
 *
 * @property fileService MinIO object storage
 * @property fileRepository file metadata persistence
 * @property fileMapper entity and response mapping
 * @property properties file storage configuration
 * @property storageTransactions JDBC and object-storage coordination
 */
@Service
class AvatarServiceImpl(
    private val fileService: FileService,
    private val fileRepository: FileRepository,
    private val fileMapper: FileMapper,
    private val properties: FileProperties,
    private val storageTransactions: FileStorageTransactionSupport,
) : AvatarService {
    override fun uploadAvatar(file: MultipartFile): FileMetadataResponse {
        val ownerId = currentOwnerId()
        validateFile(file)
        fileService.ensureBucket()
        val extension = extensionOf(file.originalFilename)
        val objectKey = buildString {
            append(properties.avatarPrefix.trimEnd('/'))
            append('/')
            append(ownerFolder(ownerId))
            if (extension.isNotEmpty()) append('.').append(extension)
        }
        val contentType = file.contentType?.takeIf { it.isNotBlank() } ?: "application/octet-stream"
        val originalFilename = safeFilename(file.originalFilename, "avatar")
        val metadata = mapOf("original-filename" to originalFilename)
        val replacingExisting = storageTransactions.readOnly {
            fileRepository.findByBucketNameAndObjectKey(properties.bucketName, objectKey) != null
        }
        val etag = fileService.putObject(objectKey, file.bytes(), contentType, metadata)

        return storageTransactions.commitMetadataForStoredObject(
            objectKey = objectKey,
            compensateOnFailure = !replacingExisting,
        ) {
            val existing = fileRepository.findByBucketNameAndObjectKeyStartingWith(
                properties.bucketName,
                avatarPrefix(ownerId),
            )
            val current = existing.firstOrNull { it.objectKey == objectKey }
            val obsolete = existing.filter { it.objectKey != objectKey }
            obsolete.forEach(fileRepository::delete)
            storageTransactions.deleteObjectsAfterCommit(obsolete.map { it.objectKey })
            val entity = current ?: fileMapper.toEntity(
                bucketName = properties.bucketName,
                objectKey = objectKey,
                contentType = contentType,
                sizeBytes = file.size,
                etag = etag,
                metadata = metadata,
            )
            if (current != null) {
                entity.contentType = contentType
                entity.sizeBytes = file.size
                entity.etag = etag
                entity.metadata = metadata
            }
            fileMapper.toResponse(fileRepository.save(entity), ApiPaths.V1_FILES_AVATAR)
        }
    }

    override fun downloadAvatar(): StoredFile {
        val ownerId = currentOwnerId()
        val entity = storageTransactions.readOnly {
            fileRepository.findByBucketNameAndObjectKeyStartingWith(
                properties.bucketName,
                avatarPrefix(ownerId),
            ).firstOrNull()
        } ?: throw FileException(2005, "FILE-005", HttpStatus.NOT_FOUND, "Avatar was not found")
        return try {
            fileService.getObject(
                entity.objectKey,
                entity.contentType,
                entity.metadata["original-filename"] ?: entity.objectKey.substringAfterLast('/'),
            )
        } catch (_: Exception) {
            storageTransactions.write {
                fileRepository.findByBucketNameAndObjectKey(properties.bucketName, entity.objectKey)
                    ?.let(fileRepository::delete)
            }
            throw FileException(2005, "FILE-005", HttpStatus.NOT_FOUND, "Avatar was not found")
        }
    }

    private fun currentOwnerId(): String {
        val authentication = SecurityContextHolder.getContext().authentication
        if (authentication == null || !authentication.isAuthenticated || authentication is AnonymousAuthenticationToken) {
            throw FileException(1002, "AUTH-002", HttpStatus.UNAUTHORIZED, "Authentication is required")
        }
        return authentication.name
    }

    private fun validateFile(file: MultipartFile) {
        if (file.isEmpty) throw EmptyFileException()
        if (file.size > properties.maxFileSizeBytes) throw FileTooLargeException(properties.maxFileSizeBytes)
    }

    private fun safeFilename(filename: String?, fallback: String): String =
        filename?.let { it.substringAfterLast('/').substringAfterLast('\\') }?.takeIf { it.isNotBlank() } ?: fallback

    private fun extensionOf(filename: String?): String =
        filename?.substringAfterLast('.', "")?.lowercase()?.takeIf { it.matches(Regex("[a-z0-9]{1,10}")) } ?: ""

    private fun ownerFolder(ownerId: String): String = Base64.getUrlEncoder().withoutPadding().encodeToString(
        MessageDigest.getInstance("SHA-256").digest(ownerId.toByteArray(Charsets.UTF_8)),
    )

    private fun avatarPrefix(ownerId: String): String =
        "${properties.avatarPrefix.trimEnd('/')}/${ownerFolder(ownerId)}"
}
