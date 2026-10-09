package com.agentgo.models.repository

import com.agentgo.dto.models.ModelType
import com.agentgo.models.entity.ModelEntity
import java.util.UUID
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.repository.query.QueryByExampleExecutor

/**
 * Persistence access for [ModelEntity] rows.
 *
 * Query methods intentionally require an owner identifier so callers cannot load
 * another user's models by id alone.
 */
interface ModelRepository :
    JpaRepository<ModelEntity, UUID>,
    JpaSpecificationExecutor<ModelEntity>,
    QueryByExampleExecutor<ModelEntity> {
    /**
     * Finds a model owned by [ownerUserId].
     *
     * @param id model identifier
     * @param ownerUserId authenticated owner
     * @return matching entity, or `null` when absent
     */
    fun findByIdAndOwnerUserId(id: UUID, ownerUserId: String): ModelEntity?

    /**
     * Lists every model owned by [ownerUserId], newest updates first.
     *
     * @param ownerUserId authenticated owner
     * @return ordered model entities
     */
    fun findByOwnerUserIdOrderByUpdatedAtDesc(ownerUserId: String): List<ModelEntity>

    /**
     * Lists models owned by [ownerUserId] filtered by [type], newest updates first.
     *
     * @param ownerUserId authenticated owner
     * @param type model capability category
     * @return ordered model entities
     */
    fun findByOwnerUserIdAndTypeOrderByUpdatedAtDesc(ownerUserId: String, type: ModelType): List<ModelEntity>

    /**
     * Returns whether the owner already has a model with [name].
     *
     * @param ownerUserId authenticated owner
     * @param name candidate model name
     * @return `true` when a conflicting row exists
     */
    fun existsByOwnerUserIdAndName(ownerUserId: String, name: String): Boolean

    /**
     * Returns whether the owner already has another model with [name].
     *
     * @param ownerUserId authenticated owner
     * @param name candidate model name
     * @param id model identifier to exclude from the uniqueness check
     * @return `true` when a conflicting row exists
     */
    fun existsByOwnerUserIdAndNameAndIdNot(ownerUserId: String, name: String, id: UUID): Boolean
}
