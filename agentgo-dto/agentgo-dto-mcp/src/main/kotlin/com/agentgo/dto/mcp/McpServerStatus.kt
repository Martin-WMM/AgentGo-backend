package com.agentgo.dto.mcp

import io.swagger.v3.oas.annotations.media.Schema

/**
 * Last known connectivity status of a managed MCP server.
 */
@Schema(
    description = "Connectivity status derived from probe or runtime use",
    allowableValues = ["UNKNOWN", "CONNECTED", "DISCONNECTED", "ERROR"],
)
enum class McpServerStatus {
    /** No successful probe has been recorded yet. */
    UNKNOWN,

    /** The last Streamable HTTP initialize handshake succeeded. */
    CONNECTED,

    /** The server is disabled or intentionally marked offline. */
    DISCONNECTED,

    /** The last probe or runtime call failed. */
    ERROR,
}
