package com.agentgo.mcp.service

import com.agentgo.dto.mcp.McpServerCreateRequest
import com.agentgo.dto.mcp.McpServerResponse
import com.agentgo.dto.mcp.McpServerStatus
import com.agentgo.dto.mcp.McpServerUpdateRequest

/**
 * Application service for authenticated, user-scoped MCP Streamable HTTP server management.
 *
 * Implementations must resolve ownership exclusively from the security context and
 * must never trust a client-supplied user identifier. Local/stdio transports are rejected.
 */
interface McpServerService {
    fun create(request: McpServerCreateRequest): McpServerResponse

    fun get(id: Long): McpServerResponse

    fun list(status: McpServerStatus? = null): List<McpServerResponse>

    fun update(id: Long, request: McpServerUpdateRequest): McpServerResponse

    fun delete(id: Long)

    /**
     * Deletes every listed MCP server owned by the authenticated user.
     *
     * The operation is all-or-nothing. A missing id deletes none of the servers.
     *
     * @param ids MCP server identifiers
     */
    fun deleteAll(ids: List<Long>)

    /**
     * Probes the configured Streamable HTTP endpoint, refreshes discovery cache, and
     * updates connectivity status.
     *
     * @param id MCP server identifier
     * @return updated server response including tools cache
     */
    fun probe(id: Long): McpServerResponse
}
