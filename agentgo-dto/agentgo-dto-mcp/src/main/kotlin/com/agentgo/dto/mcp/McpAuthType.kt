package com.agentgo.dto.mcp

import io.swagger.v3.oas.annotations.media.Schema

/**
 * Outbound HTTP authentication applied when calling an MCP Streamable HTTP endpoint.
 */
@Schema(
    description = "Outbound authentication applied to MCP HTTP requests",
    allowableValues = ["NONE", "BEARER", "HEADER"],
)
enum class McpAuthType {
    /** No Authorization or custom auth header is sent. */
    NONE,

    /** Sends `Authorization: Bearer <authToken>`. */
    BEARER,

    /** Sends `<authHeaderName>: <authToken>`. */
    HEADER,
}
