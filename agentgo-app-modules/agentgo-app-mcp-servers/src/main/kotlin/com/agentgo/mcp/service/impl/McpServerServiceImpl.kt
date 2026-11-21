package com.agentgo.mcp.service.impl

import com.agentgo.commons.dto.http.request.BatchIds
import com.agentgo.dto.mcp.McpAuthType
import com.agentgo.dto.mcp.McpServerCreateRequest
import com.agentgo.dto.mcp.McpServerResponse
import com.agentgo.dto.mcp.McpServerStatus
import com.agentgo.dto.mcp.McpServerUpdateRequest
import com.agentgo.dto.mcp.McpTransport
import com.agentgo.events.annotation.RecordSystemEvent
import com.agentgo.events.domain.SystemEventDomains
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

    @RecordSystemEvent(
        domain = SystemEventDomains.MCP_SERVERS,
        name = "MCP server created",
        comment = "Created MCP server #{#result.name} (#{#result.id})",
    )
    override fun create(request: McpServerCreateRequest): McpServerResponse {
        val ownerUserId = authenticatedUserIdResolver.requireUserId()
        val normalized = normalizeCreate(request)
        return write {
            if (
                mcpServerRepository.existsByGroupNameAndNameAndCreator(
                    normalized.groupName,
                    normalized.name,
                    ownerUserId,
                )
            ) {
                throw DuplicateMcpServerNameException()
            }
            val saved = mcpServerRepository.save(mcpServerMapper.toEntity(ownerUserId, normalized))
            mcpServerRepository.flush()
            mcpServerMapper.toResponse(saved)
        }
    }

    override fun get(id: Long): McpServerResponse {
        val ownerUserId = authenticatedUserIdResolver.requireUserId()
        return read {
            val entity = mcpServerRepository.findByIdAndOwnerUserId(id, ownerUserId)
                ?: throw McpServerNotFoundException()
            mcpServerMapper.toResponse(entity)
        }
    }

    override fun list(status: McpServerStatus?): List<McpServerResponse> {
        val ownerUserId = authenticatedUserIdResolver.requireUserId()
        return read {
            val entities = if (status == null) {
                mcpServerRepository.findByOwnerUserIdOrderByUpdatedAtDesc(ownerUserId)
            } else {
                mcpServerRepository.findByOwnerUserIdAndStatusOrderByUpdatedAtDesc(ownerUserId, status)
            }
            entities.map(mcpServerMapper::toResponse)
        }
    }

    @RecordSystemEvent(
        domain = SystemEventDomains.MCP_SERVERS,
        name = "MCP server updated",
        comment = "Updated MCP server #{#result.name} (#{#id})",
    )
    override fun update(id: Long, request: McpServerUpdateRequest): McpServerResponse {
        val ownerUserId = authenticatedUserIdResolver.requireUserId()
        val normalized = normalizeUpdate(request)
        return write {
            val entity = mcpServerRepository.findByIdAndOwnerUserId(id, ownerUserId)
                ?: throw McpServerNotFoundException()
            val creator = entity.creator?.takeIf { it.isNotBlank() } ?: ownerUserId
            if (
                mcpServerRepository.existsByGroupNameAndNameAndCreatorAndIdNot(
                    normalized.groupName,
                    normalized.name,
                    creator,
                    id,
                )
            ) {
                throw DuplicateMcpServerNameException()
            }
            mcpServerMapper.applyUpdate(entity, normalized)
            if (!entity.enabled && entity.status == McpServerStatus.CONNECTED) {
                entity.status = McpServerStatus.DISCONNECTED
            }
            val saved = mcpServerRepository.save(entity)
            mcpServerRepository.flush()
            mcpServerMapper.toResponse(saved)
        }
    }

    @RecordSystemEvent(
        domain = SystemEventDomains.MCP_SERVERS,
        name = "MCP server deleted",
        comment = "Deleted MCP server #{#id}",
    )
    override fun delete(id: Long) {
        val ownerUserId = authenticatedUserIdResolver.requireUserId()
        write {
            val entity = mcpServerRepository.findByIdAndOwnerUserId(id, ownerUserId)
                ?: throw McpServerNotFoundException()
            mcpServerRepository.delete(entity)
        }
    }

    @RecordSystemEvent(
        domain = SystemEventDomains.MCP_SERVERS,
        name = "MCP servers deleted",
        comment = "Deleted #{#ids.size()} MCP servers",
    )
    override fun deleteAll(ids: List<Long>) {
        val ownerUserId = authenticatedUserIdResolver.requireUserId()
        val distinct = BatchIds.requireDistinctOr(ids) { InvalidMcpServerRequestException(it) }
        write {
            val entities = distinct.map { id ->
                mcpServerRepository.findByIdAndOwnerUserId(id, ownerUserId)
                    ?: throw McpServerNotFoundException()
            }
            entities.forEach(mcpServerRepository::delete)
        }
    }

    override fun probe(id: Long): McpServerResponse {
        val ownerUserId = authenticatedUserIdResolver.requireUserId()
        val entity = read {
            mcpServerRepository.findByIdAndOwnerUserId(id, ownerUserId)
                ?: throw McpServerNotFoundException()
        }
        return try {
            val probeResult = mcpStreamableHttpClient.probe(entity)
            write {
                val managed = mcpServerRepository.findByIdAndOwnerUserId(id, ownerUserId)
                    ?: throw McpServerNotFoundException()
                managed.status = McpServerStatus.CONNECTED
                managed.protocolVersion = probeResult.protocolVersion
                managed.remoteServerName = probeResult.remoteServerName
                managed.remoteServerVersion = probeResult.remoteServerVersion
                managed.serverCapabilities = probeResult.serverCapabilities
                managed.toolsCache = probeResult.tools
                managed.lastConnectedAt = Instant.now()
                managed.lastError = null
                val saved = mcpServerRepository.save(managed)
                mcpServerRepository.flush()
                mcpServerMapper.toResponse(saved)
            }
        } catch (ex: McpProbeFailedException) {
            write {
                val managed = mcpServerRepository.findByIdAndOwnerUserId(id, ownerUserId)
                    ?: throw McpServerNotFoundException()
                managed.status = McpServerStatus.ERROR
                managed.lastError = ex.message.take(2000)
                mcpServerRepository.save(managed)
                mcpServerRepository.flush()
            }
            throw ex
        }
    }

    private fun normalizeCreate(request: McpServerCreateRequest): McpServerCreateRequest =
        McpServerCreateRequest().apply {
            groupName = normalizeGroup(request.groupName)
            name = requireText(request.name, "name", 255)
            transport = requireTransport(request.transport)
            endpointUrl = requireEndpointUrl(request.endpointUrl)
            authType = requireAuthType(request.authType)
            authToken = normalizeOptionalText(request.authToken, "authToken", 4000)
            authHeaderName = normalizeOptionalText(request.authHeaderName, "authHeaderName", 255)
            customHeaders = normalizeHeaders(request.customHeaders)
            protocolVersion = normalizeOptionalText(request.protocolVersion, "protocolVersion", 32)
                ?: McpStreamableHttpClient.DEFAULT_PROTOCOL_VERSION
            enabled = request.enabled ?: true
            description = normalizeDescription(request.description)
            validateAuth(authType!!, authToken, authHeaderName)
        }

    private fun normalizeUpdate(request: McpServerUpdateRequest): McpServerUpdateRequest =
        McpServerUpdateRequest().apply {
            groupName = normalizeGroup(request.groupName)
            name = requireText(request.name, "name", 255)
            transport = requireTransport(request.transport)
            endpointUrl = requireEndpointUrl(request.endpointUrl)
            authType = requireAuthType(request.authType)
            authToken = normalizeOptionalText(request.authToken, "authToken", 4000)
            authHeaderName = normalizeOptionalText(request.authHeaderName, "authHeaderName", 255)
            customHeaders = normalizeHeaders(request.customHeaders)
            protocolVersion = normalizeOptionalText(request.protocolVersion, "protocolVersion", 32)
                ?: McpStreamableHttpClient.DEFAULT_PROTOCOL_VERSION
            enabled = request.enabled ?: true
            description = normalizeDescription(request.description)
            validateAuth(authType!!, authToken, authHeaderName)
        }

    private fun requireTransport(transport: McpTransport?): McpTransport {
        val value = transport ?: McpTransport.STREAMABLE_HTTP
        if (value != McpTransport.STREAMABLE_HTTP) {
            throw InvalidMcpServerRequestException(
                "Only STREAMABLE_HTTP transport is supported; local/stdio MCP servers are not allowed.",
            )
        }
        return value
    }

    private fun requireAuthType(authType: McpAuthType?): McpAuthType =
        authType ?: McpAuthType.NONE

    private fun requireEndpointUrl(value: String): String {
        val trimmed = requireText(value, "endpointUrl", 2048)
        val uri = try {
            URI.create(trimmed)
        } catch (_: Exception) {
            throw InvalidMcpServerRequestException("The MCP endpointUrl must be a valid absolute HTTP(S) URL.")
        }
        val scheme = uri.scheme?.lowercase()
        if (scheme != "http" && scheme != "https") {
            throw InvalidMcpServerRequestException("The MCP endpointUrl must use http or https.")
        }
        if (uri.host.isNullOrBlank()) {
            throw InvalidMcpServerRequestException("The MCP endpointUrl must include a host.")
        }
        return trimmed
    }

    private fun validateAuth(authType: McpAuthType, authToken: String?, authHeaderName: String?) {
        when (authType) {
            McpAuthType.NONE -> Unit
            McpAuthType.BEARER -> {
                if (authToken.isNullOrBlank()) {
                    throw InvalidMcpServerRequestException("authToken is required when authType is BEARER.")
                }
            }
            McpAuthType.HEADER -> {
                if (authHeaderName.isNullOrBlank()) {
                    throw InvalidMcpServerRequestException("authHeaderName is required when authType is HEADER.")
                }
                if (authToken.isNullOrBlank()) {
                    throw InvalidMcpServerRequestException("authToken is required when authType is HEADER.")
                }
            }
        }
    }

    private fun normalizeHeaders(headers: Map<String, String>): Map<String, String> {
        if (headers.size > 32) {
            throw InvalidMcpServerRequestException("customHeaders supports at most 32 entries.")
        }
        return headers.mapKeys { (key, _) ->
            val trimmed = key.trim()
            if (trimmed.isEmpty() || trimmed.length > 255) {
                throw InvalidMcpServerRequestException("customHeaders keys must be 1..255 characters.")
            }
            trimmed
        }.mapValues { (_, value) ->
            if (value.length > 2000) {
                throw InvalidMcpServerRequestException("customHeaders values must be at most 2000 characters.")
            }
            value
        }
    }

    private fun normalizeGroup(value: String): String {
        val trimmed = value.trim()
        if (trimmed.isEmpty()) {
            return "default"
        }
        if (trimmed.length > 255) {
            throw InvalidMcpServerRequestException("The MCP server groupName must be at most 255 characters.")
        }
        return trimmed
    }

    private fun requireText(value: String, fieldName: String, maxLength: Int): String {
        val trimmed = value.trim()
        if (trimmed.isEmpty()) {
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
