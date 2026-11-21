package com.agentgo.file.service

import com.agentgo.dto.workspace.WorkspaceCreateRequest
import com.agentgo.dto.workspace.WorkspaceResponse
import com.agentgo.dto.workspace.WorkspaceUpdateRequest

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
    fun get(id: Long): WorkspaceResponse

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
    fun update(id: Long, request: WorkspaceUpdateRequest): WorkspaceResponse

    /**
     * Deletes an empty workspace owned by the authenticated user.
     *
     * @param id workspace identifier
     */
    fun delete(id: Long)

    /**
     * Deletes every listed empty workspace owned by the authenticated user.
     *
     * The operation is all-or-nothing. A missing id or a workspace that still contains
     * files deletes none of the workspaces.
     *
     * @param ids workspace identifiers
     */
    fun deleteAll(ids: List<Long>)
}
