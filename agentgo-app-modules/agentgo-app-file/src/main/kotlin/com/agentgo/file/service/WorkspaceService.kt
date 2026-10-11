package com.agentgo.file.service

import com.agentgo.dto.workspace.WorkspaceCreateRequest
import com.agentgo.dto.workspace.WorkspaceResponse
import com.agentgo.dto.workspace.WorkspaceUpdateRequest
import java.util.UUID

/** Coordinates authenticated file-workspace business operations and persistence. */
interface WorkspaceService {
    /**
     * Creates a workspace for the authenticated user.
     *
     * @param request create payload
     * @return created workspace
     */
    fun create(request: WorkspaceCreateRequest): WorkspaceResponse

    /**
     * Returns one workspace owned by the authenticated user.
     *
     * @param id workspace identifier
     * @return matching workspace
     */
    fun get(id: UUID): WorkspaceResponse

    /**
     * Lists every workspace owned by the authenticated user.
     *
     * @return workspaces ordered by newest update first
     */
    fun list(): List<WorkspaceResponse>

    /**
     * Replaces fields of a workspace owned by the authenticated user.
     *
     * @param id workspace identifier
     * @param request update payload
     * @return updated workspace
     */
    fun update(id: UUID, request: WorkspaceUpdateRequest): WorkspaceResponse

    /**
     * Deletes an empty workspace owned by the authenticated user.
     *
     * @param id workspace identifier
     */
    fun delete(id: UUID)
}
