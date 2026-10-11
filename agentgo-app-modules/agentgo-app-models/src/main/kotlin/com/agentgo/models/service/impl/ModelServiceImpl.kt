package com.agentgo.models.service.impl

import com.agentgo.dto.models.ModelCreateRequest
import com.agentgo.dto.models.ModelResponse
import com.agentgo.dto.models.ModelType
import com.agentgo.dto.models.ModelUpdateRequest
import com.agentgo.models.exception.DuplicateModelNameException
import com.agentgo.models.exception.InvalidModelRequestException
import com.agentgo.models.exception.ModelNotFoundException
import com.agentgo.models.mapper.ModelMapper
import com.agentgo.models.repository.ModelRepository
import com.agentgo.models.security.AuthenticatedUserIdResolver
import com.agentgo.models.service.ModelService
import java.util.UUID
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

    override fun create(request: ModelCreateRequest): ModelResponse {
        val ownerUserId = authenticatedUserIdResolver.requireUserId()
        val normalized = normalizeCreate(request)
        return write {
            if (modelRepository.existsByOwnerUserIdAndName(ownerUserId, normalized.name)) {
                throw DuplicateModelNameException()
            }
            val saved = modelRepository.save(modelMapper.toEntity(ownerUserId, normalized))
            modelRepository.flush()
            modelMapper.toResponse(saved)
        }
    }

    override fun get(id: UUID): ModelResponse {
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

    override fun update(id: UUID, request: ModelUpdateRequest): ModelResponse {
        val ownerUserId = authenticatedUserIdResolver.requireUserId()
        val normalized = normalizeUpdate(request)
        return write {
            val entity = modelRepository.findByIdAndOwnerUserId(id, ownerUserId)
                ?: throw ModelNotFoundException()
            if (modelRepository.existsByOwnerUserIdAndNameAndIdNot(ownerUserId, normalized.name, id)) {
                throw DuplicateModelNameException()
            }
            modelMapper.applyUpdate(entity, normalized)
            val saved = modelRepository.save(entity)
            modelRepository.flush()
            modelMapper.toResponse(saved)
        }
    }

    override fun delete(id: UUID) {
        val ownerUserId = authenticatedUserIdResolver.requireUserId()
        write {
            val entity = modelRepository.findByIdAndOwnerUserId(id, ownerUserId)
                ?: throw ModelNotFoundException()
            modelRepository.delete(entity)
        }
    }

    private fun normalizeCreate(request: ModelCreateRequest): ModelCreateRequest =
        ModelCreateRequest().apply {
            name = requireText(request.name, "name")
            provider = requireText(request.provider, "provider")
            type = requireType(request.type)
            configuration = request.configuration
            description = normalizeDescription(request.description)
        }

    private fun normalizeUpdate(request: ModelUpdateRequest): ModelUpdateRequest =
        ModelUpdateRequest().apply {
            name = requireText(request.name, "name")
            provider = requireText(request.provider, "provider")
            type = requireType(request.type)
            configuration = request.configuration
            description = normalizeDescription(request.description)
        }

    private fun requireType(type: ModelType?): ModelType =
        type ?: throw InvalidModelRequestException("The model type is required.")

