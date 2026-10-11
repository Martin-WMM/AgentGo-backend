package com.agentgo.dto.mcp

import io.swagger.v3.oas.annotations.media.Schema

/**
 * Request body used to replace fields of an existing user-owned MCP server.
 *
 * Mutable JavaBean-style properties are required so Spring Boot 4 / Jackson 3 can
 * deserialize the JSON body without a Jackson Kotlin module on the HTTP mapper.
 */
@Schema(description = "Request body for updating a user-owned MCP Streamable HTTP server")
class McpServerUpdateRequest {
    @field:Schema(
        description = "Human-readable MCP server name unique for the authenticated user",
        example = "docs-tools",
        requiredMode = Schema.RequiredMode.REQUIRED,
        maxLength = 255,
    )
    var name: String = ""

    @field:Schema(
        description = "MCP transport. Must be STREAMABLE_HTTP; local/stdio is unsupported.",
        example = "STREAMABLE_HTTP",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED,
    )
    var transport: McpTransport? = McpTransport.STREAMABLE_HTTP

    @field:Schema(
        description = "Single MCP Streamable HTTP endpoint URL that accepts POST and GET",
        example = "https://mcp.example.com/mcp",
        requiredMode = Schema.RequiredMode.REQUIRED,
        maxLength = 2048,
    )
    var endpointUrl: String = ""

    @field:Schema(
        description = "Outbound authentication applied to MCP HTTP requests",
        example = "BEARER",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED,
    )
    var authType: McpAuthType? = McpAuthType.NONE

    @field:Schema(
        description = "Bearer token or custom header value when authType is BEARER or HEADER",
        example = "sk-example",
        maxLength = 4000,
    )
    var authToken: String? = null

    @field:Schema(
        description = "HTTP header name when authType is HEADER",
        example = "X-API-Key",
        maxLength = 255,
    )
    var authHeaderName: String? = null

    @field:Schema(
        description = "Additional HTTP headers sent with every MCP request",
        example = "{\"X-Tenant\":\"demo\"}",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED,
    )
    var customHeaders: Map<String, String> = emptyMap()

    @field:Schema(
        description = "Preferred MCP protocol version negotiated during initialize",
        example = "2025-03-26",
        maxLength = 32,
    )
    var protocolVersion: String? = "2025-03-26"

    @field:Schema(
        description = "Whether the server is available for Agent runtime assembly",
        example = "true",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED,
    )
    var enabled: Boolean? = true

    @field:Schema(
        description = "Optional free-form description of the MCP server",
        example = "Documentation search tools over Streamable HTTP",
        maxLength = 2000,
    )
    var description: String? = null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is McpServerUpdateRequest) return false
        return name == other.name &&
            transport == other.transport &&
            endpointUrl == other.endpointUrl &&
            authType == other.authType &&
            authToken == other.authToken &&
            authHeaderName == other.authHeaderName &&
            customHeaders == other.customHeaders &&
            protocolVersion == other.protocolVersion &&
            enabled == other.enabled &&
            description == other.description
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + (transport?.hashCode() ?: 0)
        result = 31 * result + endpointUrl.hashCode()
        result = 31 * result + (authType?.hashCode() ?: 0)
        result = 31 * result + (authToken?.hashCode() ?: 0)
        result = 31 * result + (authHeaderName?.hashCode() ?: 0)
        result = 31 * result + customHeaders.hashCode()
        result = 31 * result + (protocolVersion?.hashCode() ?: 0)
        result = 31 * result + (enabled?.hashCode() ?: 0)
        result = 31 * result + (description?.hashCode() ?: 0)
        return result
    }
}
