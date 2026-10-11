package com.agentgo.mcp.exception

import org.springframework.http.HttpStatus

/**
 * Raised when create or update payloads fail domain validation.
 *
 * @param message safe client-facing validation message
 */
class InvalidMcpServerRequestException(
    message: String = "The MCP server request is invalid.",
) : McpException(
    code = 9502,
    responseType = "MCP-002",
    status = HttpStatus.BAD_REQUEST,
    message = message,
)
