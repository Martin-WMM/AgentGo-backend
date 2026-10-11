package com.agentgo.mcp.controller

import com.agentgo.commons.dto.http.response.CommonHttpResponse
import com.agentgo.commons.dto.http.response.status.success.McpResponseCodeRegistry
import com.agentgo.dto.mcp.McpServerCreateRequest
import com.agentgo.dto.mcp.McpServerResponse
import com.agentgo.dto.mcp.McpServerStatus
import com.agentgo.dto.mcp.McpServerUpdateRequest
import com.agentgo.mcp.controller.apidoc.McpServerControllerApiDoc
import com.agentgo.mcp.service.McpServerService
import java.util.UUID
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

/**
 * HTTP adapter for authenticated MCP Streamable HTTP server CRUD and probe operations.
 */
@RestController
class McpServerController(
    private val mcpServerService: McpServerService,
) : McpServerControllerApiDoc {
    override fun create(request: McpServerCreateRequest): CommonHttpResponse<McpServerResponse> =
        CommonHttpResponse(
            code = McpResponseCodeRegistry.CREATED,
            responseType = McpResponseCodeRegistry.CREATED_TYPE,
            message = "MCP server created",
            data = mcpServerService.create(request),
        )

    override fun get(id: UUID): CommonHttpResponse<McpServerResponse> =
        CommonHttpResponse(
            code = McpResponseCodeRegistry.RETRIEVED,
            responseType = McpResponseCodeRegistry.RETRIEVED_TYPE,
            message = "MCP server retrieved",
            data = mcpServerService.get(id),
        )

    override fun list(status: McpServerStatus?): CommonHttpResponse<List<McpServerResponse>> =
        CommonHttpResponse(
            code = McpResponseCodeRegistry.LIST_RETRIEVED,
            responseType = McpResponseCodeRegistry.LIST_RETRIEVED_TYPE,
            message = "MCP servers listed",
            data = mcpServerService.list(status),
        )

    override fun update(id: UUID, request: McpServerUpdateRequest): CommonHttpResponse<McpServerResponse> =
        CommonHttpResponse(
            code = McpResponseCodeRegistry.UPDATED,
            responseType = McpResponseCodeRegistry.UPDATED_TYPE,
            message = "MCP server updated",
            data = mcpServerService.update(id, request),
        )

    override fun delete(id: UUID): ResponseEntity<Void> {
        mcpServerService.delete(id)
        return ResponseEntity.noContent().build()
    }

    override fun probe(id: UUID): CommonHttpResponse<McpServerResponse> =
        CommonHttpResponse(
            code = McpResponseCodeRegistry.PROBED,
            responseType = McpResponseCodeRegistry.PROBED_TYPE,
            message = "MCP server probed",
            data = mcpServerService.probe(id),
        )
}
