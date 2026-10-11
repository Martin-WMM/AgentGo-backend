package com.agentgo.mcp.exception

import org.springframework.http.HttpStatus

/**
 * Raised when a Streamable HTTP initialize / tools discovery probe fails.
 *
 * @param message safe client-facing failure message
 */
class McpProbeFailedException(
    message: String = "Unable to connect to the MCP server.",
) : McpException(
    code = 9506,
    responseType = "MCP-006",
    status = HttpStatus.BAD_GATEWAY,
    message = message,
)
