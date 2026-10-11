package com.agentgo.mcp.mapper

import com.agentgo.dto.mcp.McpServerCreateRequest
import com.agentgo.dto.mcp.McpServerResponse
import com.agentgo.dto.mcp.McpServerUpdateRequest
import com.agentgo.mcp.entity.McpServerEntity

/** Maps between MCP server persistence entities and API DTOs. */
interface McpServerMapper {
    fun toResponse(entity: McpServerEntity): McpServerResponse

    fun toEntity(ownerUserId: String, request: McpServerCreateRequest): McpServerEntity

    fun applyUpdate(entity: McpServerEntity, request: McpServerUpdateRequest)
}
