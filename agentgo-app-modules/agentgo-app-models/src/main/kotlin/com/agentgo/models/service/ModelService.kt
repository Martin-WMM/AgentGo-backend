package com.agentgo.models.service

import com.agentgo.dto.models.ModelCreateRequest
import com.agentgo.dto.models.ModelResponse
import com.agentgo.dto.models.ModelType
import com.agentgo.dto.models.ModelUpdateRequest
import java.util.UUID

/**
 * Application service for authenticated, user-scoped model management.
 *
 * Implementations must resolve ownership exclusively from the security context and
 * must never trust a client-supplied user identifier.
 */
interface ModelService {
    /**
     * Creates a model for the authenticated user.
     *
     * @param request create payload
     * @return persisted model response
     */
    fun create(request: ModelCreateRequest): ModelResponse

    /**
     * Retrieves one model owned by the authenticated user.
     *
     * @param id model identifier
     * @return model response
     */
    fun get(id: UUID): ModelResponse

    /**
     * Lists models owned by the authenticated user.
     *
     * @param type optional capability filter
     * @return ordered model responses
     */
    fun list(type: ModelType? = null): List<ModelResponse>

    /**
     * Replaces fields of a model owned by the authenticated user.
     *
     * @param id model identifier
     * @param request update payload
     * @return updated model response
     */
    fun update(id: UUID, request: ModelUpdateRequest): ModelResponse

    /**
     * Deletes a model owned by the authenticated user.
     *
     * @param id model identifier
     */
    fun delete(id: UUID)
}
