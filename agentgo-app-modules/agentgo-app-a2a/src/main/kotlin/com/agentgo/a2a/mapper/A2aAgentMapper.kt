package com.agentgo.a2a.mapper

import com.agentgo.a2a.entity.A2aAgentEntity
import com.agentgo.a2a.service.NormalizedA2aAgent
import com.agentgo.dto.a2a.A2aAgentResponse

/** Maps between A2A agent persistence entities and API DTOs. */
interface A2aAgentMapper {
    /**
     * Converts a persisted agent into the API response.
     *
     * @param entity managed or detached entity with an id and audit timestamps
     * @return response DTO
     */
    fun toResponse(entity: A2aAgentEntity): A2aAgentResponse

    /**
     * Creates a new entity from validated fields. The caller assigns the description vector.
     *
     * @param ownerUserId authenticated owner
     * @param agent validated fields
     * @return unsaved entity
     */
    fun toEntity(ownerUserId: String, agent: NormalizedA2aAgent): A2aAgentEntity

    /**
     * Copies validated fields onto an existing entity. The caller assigns the description vector.
     *
     * @param entity entity loaded for the authenticated owner
     * @param agent validated fields
     */
    fun applyUpdate(entity: A2aAgentEntity, agent: NormalizedA2aAgent)
}
