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
            authToken = entity.authToken,
            authHeaderName = entity.authHeaderName,
            customHeaders = entity.customHeaders,
        )

        val initializeBody = mapOf(
            "jsonrpc" to "2.0",
            "id" to nextId(),
            "method" to "initialize",
            "params" to mapOf(
                "protocolVersion" to protocolVersion,
                "capabilities" to emptyMap<String, Any?>(),
                "clientInfo" to mapOf(
                    "name" to "agentgo",
                    "version" to "1.0.0",
                ),
            ),
        )
        val initializeResponse = postJsonRpc(endpoint, baseHeaders, initializeBody, sessionId = null)
        val initializeResult = requireResult(initializeResponse.json, "initialize")
        val negotiatedVersion = initializeResult.path("protocolVersion").asText(null) ?: protocolVersion
        val serverInfo = initializeResult.path("serverInfo")
        val capabilitiesNode = initializeResult.path("capabilities")
        val capabilities = if (capabilitiesNode.isObject) {
            objectMapper.convertValue(capabilitiesNode, object : TypeReference<Map<String, Any?>>() {})
        } else {
            emptyMap()
        }

        val sessionHeaders = baseHeaders.toMutableMap()
        initializeResponse.sessionId?.let { sessionHeaders["Mcp-Session-Id"] = it }
        sessionHeaders["MCP-Protocol-Version"] = negotiatedVersion

        val initializedNotification = mapOf(
            "jsonrpc" to "2.0",
            "method" to "notifications/initialized",
        )
        postJsonRpc(
            endpoint = endpoint,
            headers = sessionHeaders,
            body = initializedNotification,
            sessionId = initializeResponse.sessionId,
            expectResult = false,
        )

        val toolsBody = mapOf(
            "jsonrpc" to "2.0",
            "id" to nextId(),
            "method" to "tools/list",
            "params" to emptyMap<String, Any?>(),
        )
        val toolsResponse = postJsonRpc(
            endpoint = endpoint,
            headers = sessionHeaders,
            body = toolsBody,
            sessionId = initializeResponse.sessionId,
        )
        val toolsResult = requireResult(toolsResponse.json, "tools/list")
        val tools = parseTools(toolsResult.path("tools"))

        return McpProbeResult(
            protocolVersion = negotiatedVersion,
            remoteServerName = serverInfo.path("name").asText(null),
            remoteServerVersion = serverInfo.path("version").asText(null),
            serverCapabilities = capabilities,
            tools = tools,
            sessionId = initializeResponse.sessionId,
        )
    }

    private fun postJsonRpc(
        endpoint: URI,
        headers: Map<String, String>,
        body: Map<String, Any?>,
        sessionId: String?,
        expectResult: Boolean = true,
    ): JsonRpcHttpResponse {
        val requestBuilder = HttpRequest.newBuilder(endpoint)
            .timeout(Duration.ofSeconds(30))
            .header("Accept", "application/json, text/event-stream")
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)))
        headers.forEach { (key, value) -> requestBuilder.header(key, value) }
        sessionId?.let { requestBuilder.header("Mcp-Session-Id", it) }

        val response = try {
            httpClient.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString())
        } catch (ex: Exception) {
            throw McpProbeFailedException("MCP endpoint request failed: ${ex.message ?: ex.javaClass.simpleName}")
        }

        if (!expectResult && response.statusCode() in setOf(202, 200, 204)) {
            return JsonRpcHttpResponse(
                json = objectMapper.createObjectNode(),
                sessionId = response.headers().firstValue("Mcp-Session-Id").orElse(sessionId),
            )
        }

        if (response.statusCode() !in 200..299) {
            throw McpProbeFailedException("MCP endpoint returned HTTP ${response.statusCode()}.")
        }

        val contentType = response.headers().firstValue("Content-Type").orElse("")
        val jsonText = if (contentType.contains("text/event-stream", ignoreCase = true)) {
            extractSseJson(response.body())
        } else {
            response.body().trim()
        }
        if (jsonText.isEmpty()) {
            if (!expectResult) {
                return JsonRpcHttpResponse(
                    json = objectMapper.createObjectNode(),
                    sessionId = response.headers().firstValue("Mcp-Session-Id").orElse(sessionId),
                )
            }
            throw McpProbeFailedException("MCP endpoint returned an empty body.")
        }

        val json = try {
            objectMapper.readTree(jsonText)
        } catch (_: Exception) {
            throw McpProbeFailedException("MCP endpoint returned non-JSON content.")
        }

        return JsonRpcHttpResponse(
            json = json,
            sessionId = response.headers().firstValue("Mcp-Session-Id").orElse(sessionId),
        )
    }

    private fun requireResult(json: JsonNode, method: String): JsonNode {
        if (json.has("error") && !json.get("error").isNull) {
            val message = json.path("error").path("message").asText("JSON-RPC error")
            throw McpProbeFailedException("MCP $method failed: $message")
        }
        val result = json.get("result")
            ?: throw McpProbeFailedException("MCP $method response did not include a result.")
        return result
    }

    private fun parseTools(toolsNode: JsonNode): List<Map<String, Any?>> {
        if (!toolsNode.isArray) {
            return emptyList()
        }
        return toolsNode.mapNotNull { tool ->
            val name = tool.path("name").asText("").trim()
            if (name.isEmpty()) {
                null
            } else {
                linkedMapOf<String, Any?>(
                    "name" to name,
                    "description" to tool.path("description").asText(null),
                )
            }
        }
    }

    private fun extractSseJson(body: String): String {
        val dataLines = body.lineSequence()
            .map { it.trimEnd() }
            .filter { it.startsWith("data:") }
            .map { it.removePrefix("data:").trim() }
            .filter { it.isNotEmpty() && it != "[DONE]" }
            .toList()
        return dataLines.lastOrNull { candidate ->
            candidate.startsWith("{") && candidate.contains("\"result\"")
        } ?: dataLines.lastOrNull { it.startsWith("{") }.orEmpty()
    }

    private fun nextId(): Long = requestIds.getAndIncrement()

    private data class JsonRpcHttpResponse(
        val json: JsonNode,
        val sessionId: String?,
    )
}
