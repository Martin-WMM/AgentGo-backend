package com.agentgo.models.service.impl

import com.agentgo.commons.dto.http.request.BatchIds
import com.agentgo.dto.models.ModelCreateRequest
import com.agentgo.dto.models.ModelResponse
import com.agentgo.dto.models.ModelType
import com.agentgo.dto.models.ModelUpdateRequest
import com.agentgo.events.annotation.RecordSystemEvent
import com.agentgo.events.domain.SystemEventDomains
import com.agentgo.models.exception.DuplicateModelNameException
import com.agentgo.models.exception.InvalidModelRequestException
import com.agentgo.models.exception.ModelNotFoundException
import com.agentgo.models.mapper.ModelMapper
import com.agentgo.models.repository.ModelRepository
import com.agentgo.models.security.AuthenticatedUserIdResolver
import com.agentgo.models.service.ModelService
import org.springframework.stereotype.Service
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate

/**
 * Default [ModelService] implementation backed by JPA persistence.
 *
 * Each public operation opens its own programmatic transaction. Writes flush before
 * mapping so JPA auditing timestamps are present on the returned model.
 *
 * @property modelRepository model persistence access
 * @property modelMapper entity/DTO mapper
 * @property authenticatedUserIdResolver security-context owner resolver
 */
@Service
class ModelServiceImpl(
    private val modelRepository: ModelRepository,
    private val modelMapper: ModelMapper,
    private val authenticatedUserIdResolver: AuthenticatedUserIdResolver,
    transactionManager: PlatformTransactionManager,
) : ModelService {
    private val writeTransaction = TransactionTemplate(transactionManager)
    private val readTransaction = TransactionTemplate(transactionManager).apply {
        isReadOnly = true
    }

    @RecordSystemEvent(
        domain = SystemEventDomains.MODELS,
        name = "Model created",
        comment = "Created model #{#result.name} (#{#result.id})",
    )
    override fun create(request: ModelCreateRequest): ModelResponse {
        val ownerUserId = authenticatedUserIdResolver.requireUserId()
        val normalized = normalizeCreate(request)
        return write {
            if (
                modelRepository.existsByGroupNameAndNameAndCreator(
                    normalized.groupName,
                    normalized.name,
                    ownerUserId,
                )
            ) {
                throw DuplicateModelNameException()
            }
            val saved = modelRepository.save(modelMapper.toEntity(ownerUserId, normalized))
            modelRepository.flush()
            modelMapper.toResponse(saved)
        }
    }

    override fun get(id: Long): ModelResponse {
        val ownerUserId = authenticatedUserIdResolver.requireUserId()
        return read {
            val entity = modelRepository.findByIdAndOwnerUserId(id, ownerUserId)
                ?: throw ModelNotFoundException()
            modelMapper.toResponse(entity)
        }
    }

    override fun list(type: ModelType?): List<ModelResponse> {
        val ownerUserId = authenticatedUserIdResolver.requireUserId()
        return read {
            val entities = if (type == null) {
                modelRepository.findByOwnerUserIdOrderByUpdatedAtDesc(ownerUserId)
            } else {
                modelRepository.findByOwnerUserIdAndTypeOrderByUpdatedAtDesc(ownerUserId, type)
            }
            entities.map(modelMapper::toResponse)
        }
    }

    @RecordSystemEvent(
        domain = SystemEventDomains.MODELS,
        name = "Model updated",
        comment = "Updated model #{#result.name} (#{#id})",
    )
    override fun update(id: Long, request: ModelUpdateRequest): ModelResponse {
        val ownerUserId = authenticatedUserIdResolver.requireUserId()
        val normalized = normalizeUpdate(request)
        return write {
            val entity = modelRepository.findByIdAndOwnerUserId(id, ownerUserId)
                ?: throw ModelNotFoundException()
            val creator = entity.creator?.takeIf { it.isNotBlank() } ?: ownerUserId
            if (
                modelRepository.existsByGroupNameAndNameAndCreatorAndIdNot(
                    normalized.groupName,
                    normalized.name,
                    creator,
                    id,
                )
            ) {
                throw DuplicateModelNameException()
            }
            modelMapper.applyUpdate(entity, normalized)
            val saved = modelRepository.save(entity)
            modelRepository.flush()
            modelMapper.toResponse(saved)
        }
    }

    @RecordSystemEvent(
        domain = SystemEventDomains.MODELS,
        name = "Model deleted",
        comment = "Deleted model #{#id}",
    )
    override fun delete(id: Long) {
        val ownerUserId = authenticatedUserIdResolver.requireUserId()
        write {
            val entity = modelRepository.findByIdAndOwnerUserId(id, ownerUserId)
                ?: throw ModelNotFoundException()
            modelRepository.delete(entity)
        }
    }

    @RecordSystemEvent(
        domain = SystemEventDomains.MODELS,
        name = "Models deleted",
        comment = "Deleted #{#ids.size()} models",
    )
    override fun deleteAll(ids: List<Long>) {
        val ownerUserId = authenticatedUserIdResolver.requireUserId()
        val distinct = BatchIds.requireDistinctOr(ids) { InvalidModelRequestException(it) }
        write {
            val entities = distinct.map { id ->
                modelRepository.findByIdAndOwnerUserId(id, ownerUserId) ?: throw ModelNotFoundException()
            }
            entities.forEach(modelRepository::delete)
        }
    }

    private fun normalizeCreate(request: ModelCreateRequest): ModelCreateRequest =
        ModelCreateRequest().apply {
            groupName = normalizeGroup(request.groupName)
            name = requireText(request.name, "name")
            provider = requireText(request.provider, "provider")
            type = requireType(request.type)
            configuration = request.configuration
            description = normalizeDescription(request.description)
        }

    private fun normalizeUpdate(request: ModelUpdateRequest): ModelUpdateRequest =
        ModelUpdateRequest().apply {
            groupName = normalizeGroup(request.groupName)
            name = requireText(request.name, "name")
            provider = requireText(request.provider, "provider")
            type = requireType(request.type)
            configuration = request.configuration
            description = normalizeDescription(request.description)
        }

    private fun normalizeGroup(value: String): String {
        val trimmed = value.trim()
        if (trimmed.isEmpty()) {
            return "default"
        }
        if (trimmed.length > 255) {
            throw InvalidModelRequestException("The model groupName must be at most 255 characters.")
        }
        return trimmed
    }

    private fun requireType(type: ModelType?): ModelType =
        type ?: throw InvalidModelRequestException("The model type is required.")

    private fun requireText(value: String, fieldName: String): String {
        val trimmed = value.trim()
        if (trimmed.isEmpty()) {
            throw InvalidModelRequestException("The model $fieldName must not be blank.")
        }
        if (trimmed.length > 255) {
            throw InvalidModelRequestException("The model $fieldName must be at most 255 characters.")
        }
        return trimmed
    }

    private fun normalizeDescription(description: String?): String? {
        val trimmed = description?.trim().orEmpty()
        if (trimmed.isEmpty()) {
            return null
        }
        if (trimmed.length > 2000) {
            throw InvalidModelRequestException("The model description must be at most 2000 characters.")
        }
        return trimmed
    }

    private fun <T> read(action: () -> T): T =
        readTransaction.execute { action() } ?: error("Model read transaction returned null")

    private fun <T> write(action: () -> T): T =
        writeTransaction.execute { action() } ?: error("Model write transaction returned null")
}
