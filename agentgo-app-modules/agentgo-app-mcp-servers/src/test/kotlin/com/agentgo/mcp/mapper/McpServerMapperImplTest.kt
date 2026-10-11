package com.agentgo.mcp.mapper

import com.agentgo.dto.mcp.McpAuthType
import com.agentgo.dto.mcp.McpServerCreateRequest
import com.agentgo.dto.mcp.McpServerStatus
import com.agentgo.dto.mcp.McpTransport
import com.agentgo.mcp.entity.McpServerEntity
import java.time.Instant
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals

class McpServerMapperImplTest {
    private val mapper = McpServerMapperImpl()

    @Test
    fun `maps entity to response including tools cache`() {
        val entity = McpServerEntity(
            id = UUID.randomUUID(),
            ownerUserId = "ada",
            name = "docs",
            transport = McpTransport.STREAMABLE_HTTP,
            endpointUrl = "https://mcp.example.com/mcp",
            authType = McpAuthType.BEARER,
            authToken = "token",
            customHeaders = mapOf("X-Tenant" to "demo"),
            protocolVersion = "2025-03-26",
            enabled = true,
            status = McpServerStatus.CONNECTED,
            remoteServerName = "example",
            toolsCache = listOf(mapOf("name" to "search", "description" to "Search docs")),
            createdAt = Instant.parse("2026-01-01T00:00:00Z"),
            updatedAt = Instant.parse("2026-01-02T00:00:00Z"),
        )

        val response = mapper.toResponse(entity)

        assertEquals(entity.id, response.id)
        assertEquals("docs", response.name)
        assertEquals(1, response.tools.size)
        assertEquals("search", response.tools[0].name)
        assertEquals("Search docs", response.tools[0].description)
    }

    @Test
    fun `maps create request to entity`() {
        val request = McpServerCreateRequest().apply {
            name = "docs"
            endpointUrl = "https://mcp.example.com/mcp"
            authType = McpAuthType.NONE
            protocolVersion = "2025-03-26"
            enabled = true
            description = "demo"
        }

        val entity = mapper.toEntity("ada", request)

        assertEquals("ada", entity.ownerUserId)
        assertEquals("docs", entity.name)
        assertEquals(McpTransport.STREAMABLE_HTTP, entity.transport)
        assertEquals("demo", entity.description)
    }
}
