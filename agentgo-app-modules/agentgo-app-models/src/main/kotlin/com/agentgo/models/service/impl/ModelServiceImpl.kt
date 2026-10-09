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
import org.springframework.transaction.annotation.Transactional

/**
 * Default [ModelService] implementation backed by JPA persistence.
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
) : ModelService {
    @Transactional
    override fun create(request: ModelCreateRequest): ModelResponse {
        val ownerUserId = authenticatedUserIdResolver.requireUserId()
        val normalized = normalizeCreate(request)
        if (modelRepository.existsByOwnerUserIdAndName(ownerUserId, normalized.name)) {
            throw DuplicateModelNameException()
        }
        val saved = modelRepository.save(modelMapper.toEntity(ownerUserId, normalized))
        return modelMapper.toResponse(saved)
    }

    @Transactional(readOnly = true)
    override fun get(id: UUID): ModelResponse {
        val ownerUserId = authenticatedUserIdResolver.requireUserId()
        val entity = modelRepository.findByIdAndOwnerUserId(id, ownerUserId)
            ?: throw ModelNotFoundException()
        return modelMapper.toResponse(entity)
    }

    @Transactional(readOnly = true)
    override fun list(type: ModelType?): List<ModelResponse> {
        val ownerUserId = authenticatedUserIdResolver.requireUserId()
        val entities = if (type == null) {
            modelRepository.findByOwnerUserIdOrderByUpdatedAtDesc(ownerUserId)
        } else {
            modelRepository.findByOwnerUserIdAndTypeOrderByUpdatedAtDesc(ownerUserId, type)
        }
        return entities.map(modelMapper::toResponse)
    }

    @Transactional
    override fun update(id: UUID, request: ModelUpdateRequest): ModelResponse {
        val ownerUserId = authenticatedUserIdResolver.requireUserId()
        val entity = modelRepository.findByIdAndOwnerUserId(id, ownerUserId)
            ?: throw ModelNotFoundException()
        val normalized = normalizeUpdate(request)
        if (modelRepository.existsByOwnerUserIdAndNameAndIdNot(ownerUserId, normalized.name, id)) {
            throw DuplicateModelNameException()
        }
        modelMapper.applyUpdate(entity, normalized)
        return modelMapper.toResponse(modelRepository.save(entity))
    }

    @Transactional
    override fun delete(id: UUID) {
        val ownerUserId = authenticatedUserIdResolver.requireUserId()
        val entity = modelRepository.findByIdAndOwnerUserId(id, ownerUserId)
            ?: throw ModelNotFoundException()
        modelRepository.delete(entity)
    }

    private fun normalizeCreate(request: ModelCreateRequest): ModelCreateRequest =
        ModelCreateRequest(
            name = requireText(request.name, "name"),
            provider = requireText(request.provider, "provider"),
            type = request.type,
            configuration = request.configuration,
            description = normalizeDescription(request.description),
        )

    private fun normalizeUpdate(request: ModelUpdateRequest): ModelUpdateRequest =
        ModelUpdateRequest(
            name = requireText(request.name, "name"),
            provider = requireText(request.provider, "provider"),
            type = request.type,
            configuration = request.configuration,
            description = normalizeDescription(request.description),
        )

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
}
