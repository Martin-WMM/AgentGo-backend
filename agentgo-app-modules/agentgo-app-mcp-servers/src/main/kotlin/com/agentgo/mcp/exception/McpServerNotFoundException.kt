package com.agentgo.mcp.exception

import org.springframework.http.HttpStatus

/** Raised when an MCP server id is unknown for the authenticated owner. */
class McpServerNotFoundException : McpException(
    code = 9501,
    responseType = "MCP-001",
    status = HttpStatus.NOT_FOUND,
    message = "The requested MCP server was not found.",
)
