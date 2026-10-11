package com.agentgo.mcp.service.impl

import com.agentgo.dto.mcp.McpAuthType
import com.agentgo.dto.mcp.McpServerCreateRequest
import com.agentgo.dto.mcp.McpServerResponse
import com.agentgo.dto.mcp.McpServerStatus
import com.agentgo.dto.mcp.McpServerUpdateRequest
import com.agentgo.dto.mcp.McpTransport
import com.agentgo.mcp.client.McpProbeResult
import com.agentgo.mcp.client.McpStreamableHttpClient
import com.agentgo.mcp.entity.McpServerEntity
import com.agentgo.mcp.exception.DuplicateMcpServerNameException
import com.agentgo.mcp.exception.InvalidMcpServerRequestException
import com.agentgo.mcp.exception.McpProbeFailedException
import com.agentgo.mcp.exception.McpServerNotFoundException
import com.agentgo.mcp.mapper.McpServerMapper
import com.agentgo.mcp.repository.McpServerRepository
import com.agentgo.mcp.security.AuthenticatedUserIdResolver
import java.time.Instant
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.TransactionDefinition
import org.springframework.transaction.TransactionStatus
import org.springframework.transaction.support.SimpleTransactionStatus

class McpServerServiceImplTest {
    private val mcpServerRepository = mock<McpServerRepository>()
    private val mcpServerMapper = mock<McpServerMapper>()
    private val authenticatedUserIdResolver = mock<AuthenticatedUserIdResolver>()
    private val mcpStreamableHttpClient = mock<McpStreamableHttpClient>()
    private val service = McpServerServiceImpl(
        mcpServerRepository,
        mcpServerMapper,
        authenticatedUserIdResolver,
        mcpStreamableHttpClient,
        PassThroughTransactionManager(),
    )

    @Test
    fun `creates streamable http server for authenticated owner`() {
        val request = createRequest("docs", "https://mcp.example.com/mcp")
        val entity = McpServerEntity(
            ownerUserId = "ada",
            name = request.name,
            endpointUrl = request.endpointUrl,
        )
        val id = UUID.randomUUID()
        val saved = entity.copy(id = id, createdAt = Instant.now(), updatedAt = Instant.now())
        val response = sampleResponse(id, request.name)
        whenever(authenticatedUserIdResolver.requireUserId()).thenReturn("ada")
        whenever(mcpServerRepository.existsByOwnerUserIdAndName("ada", "docs")).thenReturn(false)
        whenever(mcpServerMapper.toEntity(eq("ada"), any())).thenReturn(entity)
        whenever(mcpServerRepository.save(entity)).thenReturn(saved)
        whenever(mcpServerMapper.toResponse(saved)).thenReturn(response)

        assertEquals(response, service.create(request))
        verify(mcpServerRepository).save(entity)
    }

    @Test
    fun `rejects blank endpoint url on create`() {
        whenever(authenticatedUserIdResolver.requireUserId()).thenReturn("ada")

        assertFailsWith<InvalidMcpServerRequestException> {
            service.create(createRequest("docs", "  "))
        }
        verify(mcpServerRepository, never()).save(any())
    }

    @Test
    fun `rejects non http endpoint url on create`() {
        whenever(authenticatedUserIdResolver.requireUserId()).thenReturn("ada")

        assertFailsWith<InvalidMcpServerRequestException> {
            service.create(createRequest("docs", "ftp://mcp.example.com/mcp"))
        }
    }

    @Test
    fun `rejects duplicate name on create`() {
        whenever(authenticatedUserIdResolver.requireUserId()).thenReturn("ada")
        whenever(mcpServerRepository.existsByOwnerUserIdAndName("ada", "docs")).thenReturn(true)

        assertFailsWith<DuplicateMcpServerNameException> {
            service.create(createRequest("docs", "https://mcp.example.com/mcp"))
        }
    }

    @Test
    fun `requires bearer token when auth type is bearer`() {
        whenever(authenticatedUserIdResolver.requireUserId()).thenReturn("ada")
        val request = createRequest("docs", "https://mcp.example.com/mcp").apply {
            authType = McpAuthType.BEARER
            authToken = null
        }

        assertFailsWith<InvalidMcpServerRequestException> { service.create(request) }
    }

    @Test
    fun `gets and lists only owner scoped servers`() {
        val id = UUID.randomUUID()
        val entity = McpServerEntity(
            id = id,
            ownerUserId = "ada",
            name = "docs",
            endpointUrl = "https://mcp.example.com/mcp",
            createdAt = Instant.now(),
            updatedAt = Instant.now(),
        )
        val response = sampleResponse(id, "docs")
        whenever(authenticatedUserIdResolver.requireUserId()).thenReturn("ada")
        whenever(mcpServerRepository.findByIdAndOwnerUserId(id, "ada")).thenReturn(entity)
        whenever(mcpServerMapper.toResponse(entity)).thenReturn(response)
        whenever(mcpServerRepository.findByOwnerUserIdAndStatusOrderByUpdatedAtDesc("ada", McpServerStatus.CONNECTED))
            .thenReturn(listOf(entity))

        assertEquals(response, service.get(id))
        assertEquals(listOf(response), service.list(McpServerStatus.CONNECTED))
    }

    @Test
    fun `updates and deletes owned servers`() {
        val id = UUID.randomUUID()
        val entity = McpServerEntity(
            id = id,
            ownerUserId = "ada",
            name = "old",
            endpointUrl = "https://mcp.example.com/mcp",
        )
        val update = updateRequest("new", "https://mcp.example.com/v2")
        val response = sampleResponse(id, "new")
        whenever(authenticatedUserIdResolver.requireUserId()).thenReturn("ada")
        whenever(mcpServerRepository.findByIdAndOwnerUserId(id, "ada")).thenReturn(entity)
        whenever(mcpServerRepository.existsByOwnerUserIdAndNameAndIdNot("ada", "new", id)).thenReturn(false)
        whenever(mcpServerRepository.save(entity)).thenReturn(entity)
        whenever(mcpServerMapper.toResponse(entity)).thenReturn(response)

        assertEquals(response, service.update(id, update))
        verify(mcpServerMapper).applyUpdate(eq(entity), any())

        service.delete(id)
        verify(mcpServerRepository).delete(entity)
    }

    @Test
    fun `treats missing owned server as not found`() {
        val id = UUID.randomUUID()
        whenever(authenticatedUserIdResolver.requireUserId()).thenReturn("ada")
        whenever(mcpServerRepository.findByIdAndOwnerUserId(id, "ada")).thenReturn(null)

        assertFailsWith<McpServerNotFoundException> { service.get(id) }
        assertFailsWith<McpServerNotFoundException> {
            service.update(id, updateRequest("n", "https://mcp.example.com/mcp"))
        }
        assertFailsWith<McpServerNotFoundException> { service.delete(id) }
        assertFailsWith<McpServerNotFoundException> { service.probe(id) }
    }

    @Test
    fun `probe refreshes discovery cache on success`() {
        val id = UUID.randomUUID()
        val entity = McpServerEntity(
            id = id,
            ownerUserId = "ada",
            name = "docs",
            endpointUrl = "https://mcp.example.com/mcp",
        )
        val response = sampleResponse(id, "docs").copy(status = McpServerStatus.CONNECTED)
        whenever(authenticatedUserIdResolver.requireUserId()).thenReturn("ada")
        whenever(mcpServerRepository.findByIdAndOwnerUserId(id, "ada")).thenReturn(entity)
        whenever(mcpStreamableHttpClient.probe(entity)).thenReturn(
            McpProbeResult(
                protocolVersion = "2025-03-26",
                remoteServerName = "example",
                remoteServerVersion = "1.0.0",
                serverCapabilities = mapOf("tools" to emptyMap<String, Any?>()),
                tools = listOf(mapOf("name" to "search", "description" to "Search")),
            ),
        )
        whenever(mcpServerRepository.save(entity)).thenReturn(entity)
        whenever(mcpServerMapper.toResponse(entity)).thenReturn(response)

        assertEquals(response, service.probe(id))
        assertEquals(McpServerStatus.CONNECTED, entity.status)
        assertEquals("example", entity.remoteServerName)
        assertEquals(1, entity.toolsCache.size)
        verify(mcpServerRepository).save(entity)
    }

    @Test
    fun `probe marks error status when streamable http handshake fails`() {
        val id = UUID.randomUUID()
        val entity = McpServerEntity(
            id = id,
            ownerUserId = "ada",
            name = "docs",
            endpointUrl = "https://mcp.example.com/mcp",
        )
        whenever(authenticatedUserIdResolver.requireUserId()).thenReturn("ada")
        whenever(mcpServerRepository.findByIdAndOwnerUserId(id, "ada")).thenReturn(entity)
        whenever(mcpStreamableHttpClient.probe(entity)).thenThrow(McpProbeFailedException("down"))
        whenever(mcpServerRepository.save(entity)).thenReturn(entity)

        assertFailsWith<McpProbeFailedException> { service.probe(id) }
        assertEquals(McpServerStatus.ERROR, entity.status)
        assertEquals("down", entity.lastError)
    }

    private fun createRequest(name: String, endpointUrl: String): McpServerCreateRequest =
        McpServerCreateRequest().apply {
            this.name = name
            this.endpointUrl = endpointUrl
            transport = McpTransport.STREAMABLE_HTTP
            authType = McpAuthType.NONE
            enabled = true
        }

    private fun updateRequest(name: String, endpointUrl: String): McpServerUpdateRequest =
        McpServerUpdateRequest().apply {
            this.name = name
            this.endpointUrl = endpointUrl
            transport = McpTransport.STREAMABLE_HTTP
            authType = McpAuthType.NONE
            enabled = true
        }

    private fun sampleResponse(id: UUID, name: String): McpServerResponse =
        McpServerResponse(
            id = id,
            name = name,
