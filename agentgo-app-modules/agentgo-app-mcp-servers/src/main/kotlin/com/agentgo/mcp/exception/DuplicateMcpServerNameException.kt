package com.agentgo.mcp.exception

import org.springframework.http.HttpStatus

/** Raised when the authenticated user already owns an MCP server with the same name. */
class DuplicateMcpServerNameException : McpException(
    code = 9503,
    responseType = "MCP-003",
    status = HttpStatus.CONFLICT,
    message = "An MCP server with this name already exists.",
)
