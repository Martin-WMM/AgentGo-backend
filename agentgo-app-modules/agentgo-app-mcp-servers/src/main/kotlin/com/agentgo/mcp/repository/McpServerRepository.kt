package com.agentgo.mcp.repository

import com.agentgo.dto.mcp.McpServerStatus
import com.agentgo.mcp.entity.McpServerEntity
import java.util.UUID
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.repository.query.QueryByExampleExecutor

/**
 * Persistence access for [McpServerEntity] rows.
 *
 * Query methods intentionally require an owner identifier so callers cannot load
 * another user's MCP servers by id alone.
 */
interface McpServerRepository :
    JpaRepository<McpServerEntity, UUID>,
    JpaSpecificationExecutor<McpServerEntity>,
    QueryByExampleExecutor<McpServerEntity> {
    fun findByIdAndOwnerUserId(id: UUID, ownerUserId: String): McpServerEntity?

    fun findByOwnerUserIdOrderByUpdatedAtDesc(ownerUserId: String): List<McpServerEntity>

    fun findByOwnerUserIdAndStatusOrderByUpdatedAtDesc(
        ownerUserId: String,
        status: McpServerStatus,
    ): List<McpServerEntity>

    fun existsByOwnerUserIdAndName(ownerUserId: String, name: String): Boolean

    fun existsByOwnerUserIdAndNameAndIdNot(ownerUserId: String, name: String, id: UUID): Boolean
}
