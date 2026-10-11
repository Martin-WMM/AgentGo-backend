package com.agentgo.file.mapper

import com.agentgo.dto.workspace.WorkspaceCreateRequest
import com.agentgo.dto.workspace.WorkspaceResponse
import com.agentgo.dto.workspace.WorkspaceUpdateRequest
import com.agentgo.file.entity.WorkspaceEntity

/** Maps between workspace persistence entities and API DTOs. */
interface WorkspaceMapper {
    /**
     * Converts a persisted entity into the API response shape.
     *
     * @param entity persisted workspace
     * @return API response
     */
    fun toResponse(entity: WorkspaceEntity): WorkspaceResponse

    /**
     * Builds a new entity for [ownerUserId] from a create request.
     *
     * @param ownerUserId authenticated owner
     * @param request create payload
     * @return unsaved entity
     */
    fun toEntity(ownerUserId: String, request: WorkspaceCreateRequest): WorkspaceEntity

    /**
     * Applies an update request onto an existing entity.
     *
     * @param entity mutable entity
     * @param request update payload
     */
    fun applyUpdate(entity: WorkspaceEntity, request: WorkspaceUpdateRequest)
}
