package com.agentgo.mcp.mapper

import com.agentgo.dto.mcp.McpServerCreateRequest
import com.agentgo.dto.mcp.McpServerResponse
import com.agentgo.dto.mcp.McpServerUpdateRequest
import com.agentgo.dto.mcp.McpToolSummary
import com.agentgo.dto.mcp.McpTransport
import com.agentgo.mcp.entity.McpServerEntity
import org.springframework.stereotype.Component

/** Default [McpServerMapper] implementation used by the MCP servers service layer. */
@Component
class McpServerMapperImpl : McpServerMapper {
    override fun toResponse(entity: McpServerEntity): McpServerResponse =
        McpServerResponse(
            id = requireNotNull(entity.id) { "Persisted MCP server must have an identifier." },
            name = entity.name,
            transport = entity.transport,
            endpointUrl = entity.endpointUrl,
            authType = entity.authType,
            authToken = entity.authToken,
            authHeaderName = entity.authHeaderName,
            customHeaders = entity.customHeaders,
            protocolVersion = entity.protocolVersion,
            enabled = entity.enabled,
            status = entity.status,
            remoteServerName = entity.remoteServerName,
            remoteServerVersion = entity.remoteServerVersion,
            serverCapabilities = entity.serverCapabilities,
            tools = entity.toolsCache.map(::toToolSummary),
            lastConnectedAt = entity.lastConnectedAt,
            lastError = entity.lastError,
            description = entity.description,
            createdAt = requireNotNull(entity.createdAt) {
                "Persisted MCP server must have a creation timestamp."
            },
            updatedAt = requireNotNull(entity.updatedAt) {
                "Persisted MCP server must have an update timestamp."
            },
        )

    override fun toEntity(ownerUserId: String, request: McpServerCreateRequest): McpServerEntity =
        McpServerEntity(
            ownerUserId = ownerUserId,
            name = request.name,
            transport = request.transport ?: McpTransport.STREAMABLE_HTTP,
            endpointUrl = request.endpointUrl,
            authType = checkNotNull(request.authType) { "Auth type must be normalized before mapping." },
            authToken = request.authToken,
            authHeaderName = request.authHeaderName,
            customHeaders = request.customHeaders,
            protocolVersion = request.protocolVersion,
            enabled = request.enabled ?: true,
            description = request.description,
        )

    override fun applyUpdate(entity: McpServerEntity, request: McpServerUpdateRequest) {
        entity.name = request.name
        entity.transport = request.transport ?: McpTransport.STREAMABLE_HTTP
        entity.endpointUrl = request.endpointUrl
        entity.authType = checkNotNull(request.authType) { "Auth type must be normalized before mapping." }
        entity.authToken = request.authToken
        entity.authHeaderName = request.authHeaderName
        entity.customHeaders = request.customHeaders
        entity.protocolVersion = request.protocolVersion
        entity.enabled = request.enabled ?: true
        entity.description = request.description
    }

    private fun toToolSummary(raw: Map<String, Any?>): McpToolSummary {
        val name = raw["name"]?.toString()?.trim().orEmpty()
        val description = raw["description"]?.toString()?.trim()?.takeIf { it.isNotEmpty() }
        return McpToolSummary(name = name, description = description)
    }
}
