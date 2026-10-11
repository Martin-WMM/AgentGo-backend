package com.agentgo.mcp.controller.apidoc

import com.agentgo.commons.api.ApiPaths
import com.agentgo.commons.dto.http.response.CommonHttpResponse
import com.agentgo.dto.mcp.McpServerCreateRequest
import com.agentgo.dto.mcp.McpServerResponse
import com.agentgo.dto.mcp.McpServerStatus
import com.agentgo.dto.mcp.McpServerUpdateRequest
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import java.util.UUID
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam

/**
 * Springdoc contract for authenticated MCP Streamable HTTP server management endpoints.
 *
 * AgentGo does not support local/stdio MCP servers. Every server uses a single remote
 * Streamable HTTP endpoint URL. Ownership is resolved from the authentication context.
 */
@Tag(
    name = "MCP Servers",
    description = "Versioned user-owned MCP Streamable HTTP servers at /api/v1/mcpServers. " +
        "Local/stdio transports are unsupported. Every server belongs to the authenticated user; " +
        "clients must not send a user id. name is unique per owner (HTTP 409, MCP-003). " +
        "transport must be STREAMABLE_HTTP. endpointUrl is the single MCP POST/GET endpoint. " +
        "authType is NONE, BEARER, or HEADER. Probe performs initialize + tools/list and caches tools. " +
        "Success codes are 9600 through 9605. MCP-001 is not found, MCP-002 is invalid, " +
        "MCP-006 is probe failure. Missing authentication uses AUTH-002.",
)
@RequestMapping(ApiPaths.V1_MCP_SERVERS)
interface McpServerControllerApiDoc {
    @Operation(
        summary = "Create an MCP server for the authenticated user",
        description = "Stores a Streamable HTTP MCP server owned by the caller. Returns 9601 MCP-CREATED.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "MCP server created", useReturnTypeSchema = true),
            ApiResponse(responseCode = "400", description = "Invalid MCP server request"),
            ApiResponse(responseCode = "401", description = "Authentication required"),
            ApiResponse(responseCode = "409", description = "Duplicate MCP server name"),
        ],
    )
    @PostMapping
    fun create(@RequestBody request: McpServerCreateRequest): CommonHttpResponse<McpServerResponse>

    @Operation(
        summary = "Get one MCP server owned by the authenticated user",
        description = "Returns 9602 MCP-RETRIEVED when the id belongs to the caller.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "MCP server retrieved", useReturnTypeSchema = true),
            ApiResponse(responseCode = "401", description = "Authentication required"),
            ApiResponse(responseCode = "404", description = "MCP server not found"),
        ],
    )
    @GetMapping("/{id}")
    fun get(
        @Parameter(description = "MCP server identifier", `in` = ParameterIn.PATH, required = true)
        @PathVariable("id") id: UUID,
    ): CommonHttpResponse<McpServerResponse>

    @Operation(
        summary = "List MCP servers owned by the authenticated user",
        description = "Lists the caller's MCP servers, newest update first. Optional status filter. " +
            "Returns 9600 MCP-LIST-RETRIEVED.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "MCP servers listed", useReturnTypeSchema = true),
            ApiResponse(responseCode = "401", description = "Authentication required"),
        ],
    )
    @GetMapping
    fun list(
        @Parameter(description = "Optional connectivity status filter", `in` = ParameterIn.QUERY, example = "CONNECTED")
        @RequestParam(value = "status", required = false) status: McpServerStatus?,
    ): CommonHttpResponse<List<McpServerResponse>>

    @Operation(
        summary = "Replace an MCP server owned by the authenticated user",
        description = "Replaces connection fields. Returns 9603 MCP-UPDATED.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "MCP server updated", useReturnTypeSchema = true),
            ApiResponse(responseCode = "400", description = "Invalid MCP server request"),
            ApiResponse(responseCode = "401", description = "Authentication required"),
            ApiResponse(responseCode = "404", description = "MCP server not found"),
            ApiResponse(responseCode = "409", description = "Duplicate MCP server name"),
        ],
    )
    @PutMapping("/{id}")
    fun update(
        @Parameter(description = "MCP server identifier", `in` = ParameterIn.PATH, required = true)
        @PathVariable("id") id: UUID,
        @RequestBody request: McpServerUpdateRequest,
    ): CommonHttpResponse<McpServerResponse>

    @Operation(
        summary = "Delete an MCP server owned by the authenticated user",
        description = "Deletes the caller's MCP server and returns HTTP 204.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "MCP server deleted"),
            ApiResponse(responseCode = "401", description = "Authentication required (AUTH-002)"),
            ApiResponse(responseCode = "404", description = "MCP server not found (MCP-001)"),
        ],
    )
    @DeleteMapping("/{id}")
    fun delete(
        @Parameter(description = "MCP server identifier", `in` = ParameterIn.PATH, required = true)
        @PathVariable("id") id: UUID,
    ): ResponseEntity<Void>

    @Operation(
        summary = "Probe an MCP Streamable HTTP server",
        description = "Runs initialize + notifications/initialized + tools/list against the configured " +
            "endpoint, refreshes tools cache and status, and returns 9605 MCP-PROBED. Failures use MCP-006.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "MCP server probed", useReturnTypeSchema = true),
            ApiResponse(responseCode = "401", description = "Authentication required"),
            ApiResponse(responseCode = "404", description = "MCP server not found"),
            ApiResponse(responseCode = "502", description = "Probe failed"),
        ],
    )
    @PostMapping("/{id}/probe")
    fun probe(
        @Parameter(description = "MCP server identifier", `in` = ParameterIn.PATH, required = true)
        @PathVariable("id") id: UUID,
    ): CommonHttpResponse<McpServerResponse>
}
