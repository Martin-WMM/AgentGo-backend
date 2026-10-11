package com.agentgo.dto.mcp

import java.time.Instant
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class McpServerDtoTest {
    @Test
    fun `create request stores fields and matches an equal copy`() {
        val request = McpServerCreateRequest().apply {
            name = "docs-tools"
            transport = McpTransport.STREAMABLE_HTTP
            endpointUrl = "https://mcp.example.com/mcp"
            authType = McpAuthType.BEARER
            authToken = "sk-example"
            authHeaderName = null
            customHeaders = mapOf("X-Tenant" to "demo")
            protocolVersion = "2025-03-26"
            enabled = true
            description = "Documentation search"
        }
        val same = McpServerCreateRequest().apply {
            name = request.name
            transport = request.transport
            endpointUrl = request.endpointUrl
            authType = request.authType
            authToken = request.authToken
            authHeaderName = request.authHeaderName
            customHeaders = request.customHeaders
            protocolVersion = request.protocolVersion
            enabled = request.enabled
            description = request.description
        }

        assertEquals(request, same)
        assertEquals(request.hashCode(), same.hashCode())
        assertEquals(request, request)
        assertNotEquals(request, McpServerCreateRequest())
        assertFalse(request.equals("docs-tools"))
        assertEquals(McpTransport.STREAMABLE_HTTP, McpTransport.valueOf("STREAMABLE_HTTP"))
        assertEquals(McpAuthType.HEADER, McpAuthType.valueOf("HEADER"))
    }

    @Test
    fun `update request stores fields and rejects a different name`() {
        val request = McpServerUpdateRequest().apply {
            name = "docs-tools"
            transport = McpTransport.STREAMABLE_HTTP
            endpointUrl = "https://mcp.example.com/mcp"
            authType = McpAuthType.HEADER
            authToken = "secret"
            authHeaderName = "X-API-Key"
            customHeaders = emptyMap()
            protocolVersion = "2025-03-26"
            enabled = false
            description = null
        }
        val renamed = McpServerUpdateRequest().apply {
            name = "other"
            transport = request.transport
            endpointUrl = request.endpointUrl
            authType = request.authType
            authToken = request.authToken
            authHeaderName = request.authHeaderName
            customHeaders = request.customHeaders
            protocolVersion = request.protocolVersion
            enabled = request.enabled
            description = request.description
        }

        assertEquals("docs-tools", request.name)
        assertNull(request.description)
        assertEquals(request, request)
        assertNotEquals(request, renamed)
        assertNotEquals(request.hashCode(), renamed.hashCode())
        assertFalse(request.equals(McpServerCreateRequest()))
    }

    @Test
    fun `response and tool summary keep probe fields`() {
        val createdAt = Instant.parse("2026-10-11T00:00:00Z")
        val updatedAt = Instant.parse("2026-10-11T01:00:00Z")
        val tool = McpToolSummary(name = "search", description = "Search the knowledge base")
        val response = McpServerResponse(
            id = UUID.fromString("11111111-1111-1111-1111-111111111111"),
            name = "docs-tools",
            transport = McpTransport.STREAMABLE_HTTP,
            endpointUrl = "https://mcp.example.com/mcp",
            authType = McpAuthType.NONE,
            customHeaders = emptyMap(),
            enabled = true,
            status = McpServerStatus.CONNECTED,
            serverCapabilities = mapOf("tools" to true),
            tools = listOf(tool),
            createdAt = createdAt,
            updatedAt = updatedAt,
        )

        assertEquals("search", response.tools.single().name)
        assertEquals(McpServerStatus.CONNECTED, response.status)
        assertEquals(McpServerStatus.ERROR, McpServerStatus.valueOf("ERROR"))
        assertEquals(McpServerStatus.DISCONNECTED, McpServerStatus.valueOf("DISCONNECTED"))
        assertEquals(McpServerStatus.UNKNOWN, McpServerStatus.valueOf("UNKNOWN"))
        assertTrue(response.copy(lastError = "HTTP 401").lastError == "HTTP 401")
        assertEquals(tool, tool.copy())
        assertEquals(response, response.copy())
        assertEquals("search", tool.copy(description = null).name)
        val populated = response.copy(
            authToken = "secret",
            authHeaderName = "X-API-Key",
            protocolVersion = "2025-03-26",
            remoteServerName = "example-mcp",
            remoteServerVersion = "1.0.0",
            lastConnectedAt = updatedAt,
            lastError = "HTTP 401",
            description = "Documentation search",
        )
        assertEquals(populated, populated.copy())
        assertNotEquals(populated.hashCode(), response.hashCode())
        assertNotEquals(response, response.copy(name = "other"))
        assertTrue(populated.toString().contains("example-mcp"))
        assertEquals(McpToolSummary("search"), McpToolSummary("search", null))
    }

    @Test
    fun `nullable request fields participate in equality and hashing`() {
        val filled = filledCreate()
        val cleared = McpServerCreateRequest().apply {
            name = filled.name
            transport = null
            endpointUrl = filled.endpointUrl
            authType = null
            authToken = null
            authHeaderName = null
            customHeaders = filled.customHeaders
            protocolVersion = null
            enabled = null
            description = null
        }
        assertNotEquals(filled, cleared)
        assertNotEquals(filled.hashCode(), cleared.hashCode())
        assertEquals(cleared.hashCode(), cleared.hashCode())

        val filledUpdate = filledUpdate()
        val clearedUpdate = McpServerUpdateRequest().apply {
            name = filledUpdate.name
            transport = null
            endpointUrl = filledUpdate.endpointUrl
            authType = null
            authToken = null
            authHeaderName = null
            customHeaders = filledUpdate.customHeaders
            protocolVersion = null
            enabled = null
            description = null
        }
        assertNotEquals(filledUpdate, clearedUpdate)
        assertNotEquals(filledUpdate.hashCode(), clearedUpdate.hashCode())
        assertNotEquals(
            filledUpdate,
            McpServerUpdateRequest().apply {
                name = filledUpdate.name
                transport = filledUpdate.transport
                endpointUrl = "https://other.example/mcp"
                authType = filledUpdate.authType
                authToken = filledUpdate.authToken
                authHeaderName = filledUpdate.authHeaderName
                customHeaders = mapOf("X-Other" to "1")
                protocolVersion = "2024-11-05"
                enabled = false
                description = "other"
            },
        )
        assertEquals(McpAuthType.NONE, McpAuthType.valueOf("NONE"))
        assertEquals(McpAuthType.BEARER, McpAuthType.valueOf("BEARER"))
    }

    private fun filledCreate(): McpServerCreateRequest = McpServerCreateRequest().apply {
        name = "docs-tools"
        transport = McpTransport.STREAMABLE_HTTP
        endpointUrl = "https://mcp.example.com/mcp"
        authType = McpAuthType.HEADER
        authToken = "secret"
        authHeaderName = "X-API-Key"
        customHeaders = mapOf("X-Tenant" to "demo")
        protocolVersion = "2025-03-26"
        enabled = true
        description = "Documentation search"
    }

    private fun filledUpdate(): McpServerUpdateRequest = McpServerUpdateRequest().apply {
        name = "docs-tools"
        transport = McpTransport.STREAMABLE_HTTP
        endpointUrl = "https://mcp.example.com/mcp"
        authType = McpAuthType.BEARER
        authToken = "secret"
        authHeaderName = "X-API-Key"
        customHeaders = mapOf("X-Tenant" to "demo")
        protocolVersion = "2025-03-26"
        enabled = true
        description = "Documentation search"
    }
}
