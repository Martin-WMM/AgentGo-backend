package com.agentgo.mcp.service

import com.agentgo.dto.mcp.McpServerCreateRequest
import com.agentgo.dto.mcp.McpServerResponse
import com.agentgo.dto.mcp.McpServerStatus
import com.agentgo.dto.mcp.McpServerUpdateRequest
import java.util.UUID

/**
 * Application service for authenticated, user-scoped MCP Streamable HTTP server management.
 *
 * Implementations must resolve ownership exclusively from the security context and
 * must never trust a client-supplied user identifier. Local/stdio transports are rejected.
 */
interface McpServerService {
    fun create(request: McpServerCreateRequest): McpServerResponse

    fun get(id: UUID): McpServerResponse

    fun list(status: McpServerStatus? = null): List<McpServerResponse>

    fun update(id: UUID, request: McpServerUpdateRequest): McpServerResponse

    fun delete(id: UUID)

    /**
     * Probes the configured Streamable HTTP endpoint, refreshes discovery cache, and
     * updates connectivity status.
     *
     * @param id MCP server identifier
     * @return updated server response including tools cache
     */
    fun probe(id: UUID): McpServerResponse
}
