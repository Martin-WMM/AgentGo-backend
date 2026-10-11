package com.agentgo.dto.mcp

import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant
import java.util.UUID

/**
 * API representation of a user-owned MCP Streamable HTTP server.
 */
@Schema(description = "User-owned MCP server returned by the mcpServers API")
data class McpServerResponse(
    @field:Schema(
        description = "Persistent MCP server identifier",
        format = "uuid",
        requiredMode = Schema.RequiredMode.REQUIRED,
    )
    val id: UUID,
    @field:Schema(
        description = "Human-readable MCP server name unique for the authenticated user",
        example = "docs-tools",
        requiredMode = Schema.RequiredMode.REQUIRED,
    )
    val name: String,
    @field:Schema(
        description = "MCP transport mode",
        example = "STREAMABLE_HTTP",
        requiredMode = Schema.RequiredMode.REQUIRED,
    )
    val transport: McpTransport,
    @field:Schema(
        description = "Single MCP Streamable HTTP endpoint URL",
        example = "https://mcp.example.com/mcp",
        requiredMode = Schema.RequiredMode.REQUIRED,
    )
    val endpointUrl: String,
    @field:Schema(
        description = "Outbound authentication mode",
        example = "BEARER",
        requiredMode = Schema.RequiredMode.REQUIRED,
    )
    val authType: McpAuthType,
    @field:Schema(
        description = "Bearer token or custom header value when configured",
        example = "sk-example",
    )
    val authToken: String? = null,
    @field:Schema(
        description = "HTTP header name when authType is HEADER",
        example = "X-API-Key",
    )
    val authHeaderName: String? = null,
    @field:Schema(
        description = "Additional HTTP headers sent with every MCP request",
        requiredMode = Schema.RequiredMode.REQUIRED,
    )
    val customHeaders: Map<String, String>,
    @field:Schema(
        description = "Preferred or last-negotiated MCP protocol version",
        example = "2025-03-26",
    )
    val protocolVersion: String? = null,
    @field:Schema(
        description = "Whether the server is available for Agent runtime assembly",
        requiredMode = Schema.RequiredMode.REQUIRED,
    )
    val enabled: Boolean,
    @field:Schema(
        description = "Last known connectivity status",
        example = "CONNECTED",
        requiredMode = Schema.RequiredMode.REQUIRED,
    )
    val status: McpServerStatus,
    @field:Schema(
        description = "serverInfo.name from the last successful initialize",
        example = "example-mcp",
    )
    val remoteServerName: String? = null,
    @field:Schema(
        description = "serverInfo.version from the last successful initialize",
        example = "1.0.0",
    )
    val remoteServerVersion: String? = null,
    @field:Schema(
        description = "Server capabilities object from the last successful initialize",
        requiredMode = Schema.RequiredMode.REQUIRED,
    )
    val serverCapabilities: Map<String, Any?>,
    @field:Schema(
        description = "Cached tools discovered via tools/list",
        requiredMode = Schema.RequiredMode.REQUIRED,
    )
    val tools: List<McpToolSummary>,
    @field:Schema(
        description = "Timestamp of the last successful initialize handshake",
        format = "date-time",
    )
    val lastConnectedAt: Instant? = null,
    @field:Schema(
        description = "Safe message from the last failed probe",
        example = "HTTP 401 from MCP endpoint",
    )
    val lastError: String? = null,
    @field:Schema(
        description = "Optional free-form description",
        example = "Documentation search tools over Streamable HTTP",
    )
    val description: String? = null,
    @field:Schema(
        description = "Creation timestamp",
        format = "date-time",
        requiredMode = Schema.RequiredMode.REQUIRED,
    )
    val createdAt: Instant,
    @field:Schema(
        description = "Last modification timestamp",
        format = "date-time",
        requiredMode = Schema.RequiredMode.REQUIRED,
    )
    val updatedAt: Instant,
)
