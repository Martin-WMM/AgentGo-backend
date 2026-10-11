package com.agentgo.mcp.service.impl

import com.agentgo.dto.mcp.McpAuthType
import com.agentgo.dto.mcp.McpServerCreateRequest
import com.agentgo.dto.mcp.McpServerResponse
import com.agentgo.dto.mcp.McpServerStatus
import com.agentgo.dto.mcp.McpServerUpdateRequest
import com.agentgo.dto.mcp.McpTransport
import com.agentgo.mcp.client.McpStreamableHttpClient
import com.agentgo.mcp.exception.DuplicateMcpServerNameException
import com.agentgo.mcp.exception.InvalidMcpServerRequestException
import com.agentgo.mcp.exception.McpProbeFailedException
import com.agentgo.mcp.exception.McpServerNotFoundException
import com.agentgo.mcp.mapper.McpServerMapper
import com.agentgo.mcp.repository.McpServerRepository
import com.agentgo.mcp.security.AuthenticatedUserIdResolver
import com.agentgo.mcp.service.McpServerService
import java.net.URI
import java.time.Instant
import java.util.UUID
import org.springframework.stereotype.Service
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate

/**
 * Default [McpServerService] implementation backed by JPA persistence and Streamable HTTP probes.
 */
@Service
class McpServerServiceImpl(
    private val mcpServerRepository: McpServerRepository,
    private val mcpServerMapper: McpServerMapper,
    private val authenticatedUserIdResolver: AuthenticatedUserIdResolver,
    private val mcpStreamableHttpClient: McpStreamableHttpClient,
    transactionManager: PlatformTransactionManager,
) : McpServerService {
    private val writeTransaction = TransactionTemplate(transactionManager)
    private val readTransaction = TransactionTemplate(transactionManager).apply {
        isReadOnly = true
    }

            throw InvalidMcpServerRequestException("The MCP server $fieldName must not be blank.")
        }
        if (trimmed.length > maxLength) {
            throw InvalidMcpServerRequestException(
                "The MCP server $fieldName must be at most $maxLength characters.",
            )
        }
        return trimmed
    }

    private fun normalizeOptionalText(value: String?, fieldName: String, maxLength: Int): String? {
        val trimmed = value?.trim().orEmpty()
        if (trimmed.isEmpty()) {
            return null
        }
        if (trimmed.length > maxLength) {
            throw InvalidMcpServerRequestException(
                "The MCP server $fieldName must be at most $maxLength characters.",
            )
        }
        return trimmed
    }

    private fun normalizeDescription(description: String?): String? {
        val trimmed = description?.trim().orEmpty()
        if (trimmed.isEmpty()) {
            return null
        }
        if (trimmed.length > 2000) {
            throw InvalidMcpServerRequestException("The MCP server description must be at most 2000 characters.")
        }
        return trimmed
    }

    private fun <T> read(action: () -> T): T =
        readTransaction.execute { action() } ?: error("MCP read transaction returned null")

    private fun <T> write(action: () -> T): T =
        writeTransaction.execute { action() } ?: error("MCP write transaction returned null")
}
