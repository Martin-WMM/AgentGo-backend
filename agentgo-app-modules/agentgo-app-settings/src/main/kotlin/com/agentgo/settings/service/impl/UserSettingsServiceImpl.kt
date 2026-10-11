package com.agentgo.settings.service.impl

import com.agentgo.dto.settings.UserSettingsResponse
import com.agentgo.dto.settings.UserSettingsUpdateRequest
import com.agentgo.settings.mapper.UserSettingsMapper
import com.agentgo.settings.repository.UserSettingsRepository
import com.agentgo.settings.security.SettingsUserIdResolver
import com.agentgo.settings.service.UserSettingsService
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate

/**
 * Default [UserSettingsService] implementation backed by JPA persistence.
 *
 * Each public operation opens its own programmatic transaction. A concurrent first
 * save that loses the unique-owner race is retried as an update in a new transaction.
 *
 * @property userSettingsRepository settings persistence access
 * @property userSettingsMapper entity/DTO mapper
 * @property settingsUserIdResolver security-context owner resolver
 */
@Service
class UserSettingsServiceImpl(
    private val userSettingsRepository: UserSettingsRepository,
    private val userSettingsMapper: UserSettingsMapper,
    private val settingsUserIdResolver: SettingsUserIdResolver,
    transactionManager: PlatformTransactionManager,
) : UserSettingsService {
    private val writeTransaction = TransactionTemplate(transactionManager)
    private val readTransaction = TransactionTemplate(transactionManager).apply {
        isReadOnly = true
    }

    override fun get(): UserSettingsResponse {
        val ownerUserId = settingsUserIdResolver.requireUserId()
        return read {
            userSettingsRepository.findByOwnerUserId(ownerUserId)
                ?.let(userSettingsMapper::toResponse)
                ?: userSettingsMapper.unpersistedDefaults()
        }
    }

    override fun update(request: UserSettingsUpdateRequest): UserSettingsResponse {
        val ownerUserId = settingsUserIdResolver.requireUserId()
        return try {
            write { upsert(ownerUserId, request) }
        } catch (conflict: DataIntegrityViolationException) {
            write { replaceExisting(ownerUserId, request, conflict) }
        }
    }

    private fun upsert(ownerUserId: String, request: UserSettingsUpdateRequest): UserSettingsResponse {
        val existing = userSettingsRepository.findByOwnerUserId(ownerUserId)
        val saved = if (existing == null) {
            userSettingsRepository.save(userSettingsMapper.newEntity(ownerUserId, request))
        } else {
            userSettingsMapper.applyUpdate(existing, request)
            userSettingsRepository.save(existing)
        }
        return userSettingsMapper.toResponse(saved)
    }

    private fun replaceExisting(
        ownerUserId: String,
        request: UserSettingsUpdateRequest,
        conflict: DataIntegrityViolationException,
    ): UserSettingsResponse {
        val existing = userSettingsRepository.findByOwnerUserId(ownerUserId) ?: throw conflict
        userSettingsMapper.applyUpdate(existing, request)
        return userSettingsMapper.toResponse(userSettingsRepository.save(existing))
    }

    private fun <T> read(action: () -> T): T =
        readTransaction.execute { action() } ?: error("Settings read transaction returned null")

    private fun <T> write(action: () -> T): T =
        writeTransaction.execute { action() } ?: error("Settings write transaction returned null")
}
