package com.agentgo.mcp.controller

import com.agentgo.commons.dto.http.response.status.success.McpResponseCodeRegistry
import com.agentgo.dto.mcp.McpAuthType
import com.agentgo.dto.mcp.McpServerCreateRequest
import com.agentgo.dto.mcp.McpServerResponse
import com.agentgo.dto.mcp.McpServerStatus
import com.agentgo.dto.mcp.McpServerUpdateRequest
import com.agentgo.dto.mcp.McpTransport
import com.agentgo.mcp.service.McpServerService
import java.time.Instant
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus

class McpServerControllerTest {
    private val mcpServerService = mock<McpServerService>()
    private val controller = McpServerController(mcpServerService)

    @Test
    fun `delegates crud and probe operations and wraps success codes`() {
        val id = UUID.randomUUID()
        val createRequest = McpServerCreateRequest().apply {
            name = "docs"
            endpointUrl = "https://mcp.example.com/mcp"
        }
        val updateRequest = McpServerUpdateRequest().apply {
            name = "docs"
            endpointUrl = "https://mcp.example.com/mcp"
            authType = McpAuthType.NONE
            enabled = true
        }
        val response = sampleResponse(id)
        whenever(mcpServerService.create(createRequest)).thenReturn(response)
        whenever(mcpServerService.get(id)).thenReturn(response)
        whenever(mcpServerService.list(McpServerStatus.CONNECTED)).thenReturn(listOf(response))
        whenever(mcpServerService.update(id, updateRequest)).thenReturn(response)
        whenever(mcpServerService.probe(id)).thenReturn(response)

        val created = controller.create(createRequest)
        val retrieved = controller.get(id)
        val listed = controller.list(McpServerStatus.CONNECTED)
        val updated = controller.update(id, updateRequest)
        val probed = controller.probe(id)
        val deleted = controller.delete(id)

        assertEquals(McpResponseCodeRegistry.CREATED, created.code)
        assertEquals(McpResponseCodeRegistry.RETRIEVED, retrieved.code)
        assertEquals(McpResponseCodeRegistry.LIST_RETRIEVED, listed.code)
        assertEquals(McpResponseCodeRegistry.UPDATED, updated.code)
        assertEquals(McpResponseCodeRegistry.PROBED, probed.code)
        assertEquals(response, created.data)
        assertEquals(listOf(response), listed.data)
        assertEquals(HttpStatus.NO_CONTENT, deleted.statusCode)
        assertNull(deleted.body)
        verify(mcpServerService).delete(id)
    }

    private fun sampleResponse(id: UUID): McpServerResponse =
        McpServerResponse(
            id = id,
            name = "docs",
            transport = McpTransport.STREAMABLE_HTTP,
            endpointUrl = "https://mcp.example.com/mcp",
            authType = McpAuthType.NONE,
            customHeaders = emptyMap(),
            protocolVersion = "2025-03-26",
            enabled = true,
            status = McpServerStatus.UNKNOWN,
            serverCapabilities = emptyMap(),
            tools = emptyList(),
            createdAt = Instant.parse("2026-01-01T00:00:00Z"),
            updatedAt = Instant.parse("2026-01-02T00:00:00Z"),
        )
}
