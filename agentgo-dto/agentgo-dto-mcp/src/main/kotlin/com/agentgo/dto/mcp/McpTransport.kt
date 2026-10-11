package com.agentgo.dto.mcp

import io.swagger.v3.oas.annotations.media.Schema

/**
 * MCP transport modes accepted by AgentGo.
 *
 * AgentGo only supports remote [STREAMABLE_HTTP] servers. Local/stdio subprocess
 * servers are intentionally unsupported.
 */
@Schema(
    description = "MCP transport mode. AgentGo only supports STREAMABLE_HTTP.",
    allowableValues = ["STREAMABLE_HTTP"],
)
enum class McpTransport {
    /** MCP Streamable HTTP transport (POST/GET against a single MCP endpoint URL). */
    STREAMABLE_HTTP,
}
