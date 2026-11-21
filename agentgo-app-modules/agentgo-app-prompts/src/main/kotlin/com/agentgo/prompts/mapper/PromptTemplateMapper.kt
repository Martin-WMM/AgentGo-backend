package com.agentgo.prompts.mapper

import com.agentgo.dto.prompts.PromptTemplateCreateRequest
import com.agentgo.dto.prompts.PromptTemplateResponse
import com.agentgo.dto.prompts.PromptTemplateUpdateRequest
import com.agentgo.prompts.entity.PromptTemplateEntity

/**
 * Maps between prompt-template persistence entities and API DTOs.
 */
interface PromptTemplateMapper {
    /**
     * Converts a persisted entity into an API response.
     *
     * @param entity stored template entity
     * @return response DTO
     * @throws IllegalArgumentException when required generated fields are missing
     */
    fun toResponse(entity: PromptTemplateEntity): PromptTemplateResponse

    /**
     * Builds a new entity owned by [ownerUserId] from a create request.
     *
     * @param ownerUserId authenticated owner
     * @param request create payload
     * @return unsaved entity
     */
    fun toEntity(ownerUserId: String, request: PromptTemplateCreateRequest): PromptTemplateEntity

    /**
     * Applies an update request onto an existing entity.
     *
     * @param entity mutable entity owned by the caller
     * @param request update payload
     */
    fun applyUpdate(entity: PromptTemplateEntity, request: PromptTemplateUpdateRequest)
}
