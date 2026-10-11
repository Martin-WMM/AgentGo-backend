package com.agentgo.mcp.client

import com.agentgo.mcp.entity.McpServerEntity

/**
 * Performs a one-shot MCP Streamable HTTP initialize + tools/list probe.
 *
 * Implementations must speak the MCP Streamable HTTP transport only
 * (POST JSON-RPC to the single endpoint URL, Accept json + SSE).
 */
interface McpStreamableHttpClient {
    /**
     * Probes [entity] over Streamable HTTP and returns discovery data.
     *
     * @param entity persisted MCP server configuration
     * @return initialize + tools/list summary
     * @throws com.agentgo.mcp.exception.McpProbeFailedException when the handshake fails
     */
    fun probe(entity: McpServerEntity): McpProbeResult

    companion object {
        /** Default MCP protocol version offered during initialize. */
        const val DEFAULT_PROTOCOL_VERSION: String = "2025-03-26"
    }
}
