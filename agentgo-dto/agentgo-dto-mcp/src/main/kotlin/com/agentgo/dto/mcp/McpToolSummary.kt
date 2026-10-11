package com.agentgo.dto.mcp

import io.swagger.v3.oas.annotations.media.Schema

/**
 * Compact representation of a tool discovered via MCP `tools/list`.
 *
 * @property name tool name
 * @property description optional tool description
 */
@Schema(description = "Tool summary cached from MCP tools/list")
data class McpToolSummary(
    @field:Schema(
        description = "Tool name exposed by the MCP server",
        example = "search",
        requiredMode = Schema.RequiredMode.REQUIRED,
    )
    val name: String,
    @field:Schema(
        description = "Optional human-readable tool description",
        example = "Search the knowledge base",
    )
    val description: String? = null,
)
