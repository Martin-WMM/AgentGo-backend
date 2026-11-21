package com.agentgo.a2a.repository

import com.agentgo.a2a.entity.A2aAgentEntity
import com.agentgo.dto.a2a.A2aAgentStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.repository.query.QueryByExampleExecutor

/**
 * Persistence access for [A2aAgentEntity] rows.
 *
 * Query methods intentionally require an owner identifier so callers cannot load
 * another user's A2A agents by id alone.
 */
interface A2aAgentRepository :
    JpaRepository<A2aAgentEntity, Long>,
    JpaSpecificationExecutor<A2aAgentEntity>,
    QueryByExampleExecutor<A2aAgentEntity> {
    fun findByIdAndOwnerUserId(id: Long, ownerUserId: String): A2aAgentEntity?

    fun findByOwnerUserIdOrderByUpdatedAtDesc(ownerUserId: String): List<A2aAgentEntity>

    fun findByOwnerUserIdAndStatusOrderByUpdatedAtDesc(
        ownerUserId: String,
        status: A2aAgentStatus,
    ): List<A2aAgentEntity>

    fun existsByGroupNameAndNameAndCreator(groupName: String, name: String, creator: String): Boolean

    fun existsByGroupNameAndNameAndCreatorAndIdNot(
        groupName: String,
        name: String,
        creator: String,
        id: Long,
    ): Boolean
}
