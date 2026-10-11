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

    private fun McpServerEntity.copy(
        id: UUID? = this.id,
        createdAt: Instant? = this.createdAt,
        updatedAt: Instant? = this.updatedAt,
    ): McpServerEntity =
        McpServerEntity(
            id = id,
            ownerUserId = ownerUserId,
            name = name,
            transport = transport,
            endpointUrl = endpointUrl,
            authType = authType,
            authToken = authToken,
            authHeaderName = authHeaderName,
            customHeaders = customHeaders,
            protocolVersion = protocolVersion,
            enabled = enabled,
            status = status,
            remoteServerName = remoteServerName,
            remoteServerVersion = remoteServerVersion,
            serverCapabilities = serverCapabilities,
            toolsCache = toolsCache,
            lastConnectedAt = lastConnectedAt,
            lastError = lastError,
            description = description,
            creator = creator,
            createdAt = createdAt,
            updatedAt = updatedAt,
            updatedBy = updatedBy,
        )

    private class PassThroughTransactionManager : PlatformTransactionManager {
        override fun getTransaction(definition: TransactionDefinition?): TransactionStatus =
            SimpleTransactionStatus()

        override fun commit(status: TransactionStatus) = Unit

        override fun rollback(status: TransactionStatus) = Unit
    }
}
