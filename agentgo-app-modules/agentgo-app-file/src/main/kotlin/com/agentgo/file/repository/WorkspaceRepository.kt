package com.agentgo.file.repository

import com.agentgo.file.entity.WorkspaceEntity
import java.util.UUID
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.repository.query.QueryByExampleExecutor

/**
 * Persistence access for [WorkspaceEntity] rows.
 *
 * Query methods intentionally require an owner identifier so callers cannot load
 * another user's workspaces by id alone.
 */
interface WorkspaceRepository :
    JpaRepository<WorkspaceEntity, UUID>,
    JpaSpecificationExecutor<WorkspaceEntity>,
    QueryByExampleExecutor<WorkspaceEntity> {
    /**
     * Finds a workspace owned by [ownerUserId].
     *
     * @param id workspace identifier
     * @param ownerUserId authenticated owner
     * @return matching entity, or `null` when absent
     */
    fun findByIdAndOwnerUserId(id: UUID, ownerUserId: String): WorkspaceEntity?

    /**
     * Lists every workspace owned by [ownerUserId], newest updates first.
     *
     * @param ownerUserId authenticated owner
     * @return ordered workspace entities
     */
    fun findByOwnerUserIdOrderByUpdatedAtDesc(ownerUserId: String): List<WorkspaceEntity>

    /**
     * Returns whether the owner already has a workspace with [name].
     *
     * @param ownerUserId authenticated owner
     * @param name candidate workspace name
     * @return `true` when a conflicting row exists
     */
    fun existsByOwnerUserIdAndName(ownerUserId: String, name: String): Boolean

    /**
     * Returns whether the owner already has another workspace with [name].
     *
     * @param ownerUserId authenticated owner
     * @param name candidate workspace name
     * @param id workspace identifier to exclude from the uniqueness check
     * @return `true` when a conflicting row exists
     */
    fun existsByOwnerUserIdAndNameAndIdNot(ownerUserId: String, name: String, id: UUID): Boolean
}
