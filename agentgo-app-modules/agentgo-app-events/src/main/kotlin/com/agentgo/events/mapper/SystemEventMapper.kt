package com.agentgo.events.mapper

import com.agentgo.dto.events.SystemEventCreateRequest
import com.agentgo.dto.events.SystemEventResponse
import com.agentgo.dto.events.SystemEventUpdateRequest
import com.agentgo.events.entity.SystemEventEntity
import java.time.Instant

/**
 * Maps between system-event persistence entities and API DTOs.
 */
interface SystemEventMapper {
    /**
     * Converts a persisted entity into an API response.
     *
     * @param entity stored event
     * @return response DTO
     * @throws IllegalArgumentException when required generated fields are missing
     */
    fun toResponse(entity: SystemEventEntity): SystemEventResponse

    /**
     * Builds a new entity from a create request.
     *
     * @param actor authenticated user stored when [SystemEventCreateRequest.operator] is blank
     * @param request create payload
     * @param now clock reading used when [SystemEventCreateRequest.occurredAt] is omitted
     * @return unsaved entity
     */
    fun toEntity(actor: String, request: SystemEventCreateRequest, now: Instant): SystemEventEntity

    /**
     * Replaces the business fields of an existing entity.
     *
     * A blank operator stores [actor]. An omitted [SystemEventUpdateRequest.occurredAt]
     * keeps the stored instant.
     *
     * @param entity mutable event
     * @param actor authenticated user stored when the requested operator is blank
     * @param request update payload
     */
    fun applyUpdate(entity: SystemEventEntity, actor: String, request: SystemEventUpdateRequest)
}
