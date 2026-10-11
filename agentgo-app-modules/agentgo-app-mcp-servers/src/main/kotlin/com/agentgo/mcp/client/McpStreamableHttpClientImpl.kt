package com.agentgo.mcp.client

import com.agentgo.mcp.entity.McpServerEntity
import com.agentgo.mcp.exception.McpProbeFailedException
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import java.util.concurrent.atomic.AtomicLong
import org.springframework.stereotype.Component

/**
 * Default Streamable HTTP MCP client used by the management probe endpoint.
 *
 * Flow:
 * 1. POST `initialize`
 * 2. POST `notifications/initialized` (with session id when present)
 * 3. POST `tools/list`
 */
@Component
class McpStreamableHttpClientImpl(
    private val objectMapper: ObjectMapper,
) : McpStreamableHttpClient {
    private val httpClient: HttpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(10))
        .followRedirects(HttpClient.Redirect.NEVER)
        .build()
    private val requestIds = AtomicLong(1)

    override fun probe(entity: McpServerEntity): McpProbeResult {
        val endpoint = URI.create(entity.endpointUrl)
        val protocolVersion = entity.protocolVersion?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?: McpStreamableHttpClient.DEFAULT_PROTOCOL_VERSION
        val baseHeaders = mcpRequestHeaders(
            authType = entity.authType,
