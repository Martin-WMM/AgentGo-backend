package com.agentgo.file.repository

import com.agentgo.file.entity.WorkspaceEntity
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
    JpaRepository<WorkspaceEntity, Long>,
    JpaSpecificationExecutor<WorkspaceEntity>,
    QueryByExampleExecutor<WorkspaceEntity> {
    /**
     * Finds a workspace owned by [ownerUserId].
     *
     * @param id workspace identifier
     * @param ownerUserId authenticated owner
     * @return matching entity, or `null` when absent
     */
    fun findByIdAndOwnerUserId(id: Long, ownerUserId: String): WorkspaceEntity?

    /**
     * Lists every workspace owned by [ownerUserId], newest updates first.
     *
     * @param ownerUserId authenticated owner
     * @return ordered workspace entities
     */
    fun findByOwnerUserIdOrderByUpdatedAtDesc(ownerUserId: String): List<WorkspaceEntity>

    /**
     * Returns whether [creator] already has a workspace with [groupName] and [name].
     *
     * @param groupName resource group
     * @param name candidate workspace name
     * @param creator audit creator that owns the uniqueness key
     * @return `true` when a conflicting row exists
     */
    fun existsByGroupNameAndNameAndCreator(groupName: String, name: String, creator: String): Boolean

    /**
     * Returns whether [creator] already has another workspace with [groupName] and [name].
     *
     * @param groupName resource group
     * @param name candidate workspace name
     * @param creator audit creator that owns the uniqueness key
     * @param id workspace identifier to exclude from the uniqueness check
     * @return `true` when a conflicting row exists
     */
    fun existsByGroupNameAndNameAndCreatorAndIdNot(
        groupName: String,
        name: String,
        creator: String,
        id: Long,
    ): Boolean
}
