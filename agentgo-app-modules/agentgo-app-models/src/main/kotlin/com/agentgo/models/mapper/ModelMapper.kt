package com.agentgo.models.mapper

import com.agentgo.dto.models.ModelCreateRequest
import com.agentgo.dto.models.ModelResponse
import com.agentgo.dto.models.ModelUpdateRequest
import com.agentgo.models.entity.ModelEntity

/**
 * Maps between model persistence entities and API DTOs.
 */
interface ModelMapper {
    /**
     * Converts a persisted entity into an API response.
     *
     * @param entity stored model entity
     * @return response DTO
     * @throws IllegalStateException when required generated fields are missing
     */
    fun toResponse(entity: ModelEntity): ModelResponse

    /**
     * Builds a new entity owned by [ownerUserId] from a create request.
     *
     * @param ownerUserId authenticated owner
     * @param request create payload
     * @return unsaved entity
     */
    fun toEntity(ownerUserId: String, request: ModelCreateRequest): ModelEntity

    /**
     * Applies an update request onto an existing entity.
     *
     * @param entity mutable entity owned by the caller
     * @param request update payload
     */
    fun applyUpdate(entity: ModelEntity, request: ModelUpdateRequest)
}
