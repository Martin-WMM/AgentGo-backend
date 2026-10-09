package com.agentgo.models.mapper

import com.agentgo.dto.models.ModelCreateRequest
import com.agentgo.dto.models.ModelResponse
import com.agentgo.dto.models.ModelUpdateRequest
import com.agentgo.models.entity.ModelEntity
import org.springframework.stereotype.Component

/**
 * Default [ModelMapper] implementation used by the models service layer.
 */
@Component
class ModelMapperImpl : ModelMapper {
    override fun toResponse(entity: ModelEntity): ModelResponse =
        ModelResponse(
            id = requireNotNull(entity.id) { "Persisted model must have an identifier." },
            name = entity.name,
            provider = entity.provider,
            type = entity.type,
            configuration = entity.configuration,
            description = entity.description,
            createdAt = requireNotNull(entity.createdAt) { "Persisted model must have a creation timestamp." },
            updatedAt = requireNotNull(entity.updatedAt) { "Persisted model must have an update timestamp." },
        )

    override fun toEntity(ownerUserId: String, request: ModelCreateRequest): ModelEntity =
        ModelEntity(
            ownerUserId = ownerUserId,
            name = request.name,
            provider = request.provider,
            type = request.type,
            configuration = request.configuration,
            description = request.description,
        )

    override fun applyUpdate(entity: ModelEntity, request: ModelUpdateRequest) {
        entity.name = request.name
        entity.provider = request.provider
        entity.type = request.type
        entity.configuration = request.configuration
        entity.description = request.description
    }
}
