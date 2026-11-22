package com.agentgo.core.model

import com.agentgo.core.json.AgentJson
import com.fasterxml.jackson.databind.JsonNode
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets
import java.time.Duration

/**
 * Chat model that calls a Qwen model through the DashScope compatible-mode API.
 *
 * The client sends HTTP on the caller thread and does not follow redirects,
 * so the bearer token stays on the configured host. [stream] reads SSE deltas
 * from `stream: true` completions.
 *
 * @param config endpoint, credential, and default model
 * @param httpClient HTTP client
 */
class QwenChatModel(
    private val config: QwenClientConfig,
    private val httpClient: HttpClient = defaultClient(),
) : ChatModel {
    override fun complete(request: ChatRequest): ChatResponse {
        val modelId = request.modelId.ifBlank { config.modelId }
        val response = sendString(requestBody(modelId, request, stream = false))
        if (response.statusCode() !in 200..299) {
            throw QwenChatException("Qwen request failed with status ${response.statusCode()}.")
        }
        return ChatResponse(content = readContent(response.body()), modelId = modelId)
    }

    override fun stream(
        request: ChatRequest,
        onDelta: (String) -> Unit,
    ): ChatResponse {
        val modelId = request.modelId.ifBlank { config.modelId }
        val httpRequest = httpRequest(requestBody(modelId, request, stream = true))
        val response = try {
            httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofInputStream())
        } catch (ex: IOException) {
            throw QwenChatException("Qwen request failed.", ex)
        } catch (ex: InterruptedException) {
            Thread.currentThread().interrupt()
            throw QwenChatException("Qwen request was interrupted.", ex)
        }
        if (response.statusCode() !in 200..299) {
            response.body().close()
            throw QwenChatException("Qwen request failed with status ${response.statusCode()}.")
        }
        val full = StringBuilder()
        try {
            // Small buffer so SSE lines are not held back waiting for a large fill.
            BufferedReader(InputStreamReader(response.body(), StandardCharsets.UTF_8), 256).use { reader ->
                while (true) {
                    val line = reader.readLine() ?: break
                    if (!line.startsWith("data:")) {
                        continue
                    }
                    val data = line.substring(5).trim()
                    if (data.isEmpty()) {
                        continue
                    }
                    if (data == "[DONE]") {
                        break
                    }
                    val delta = readDelta(data) ?: continue
                    if (delta.isEmpty()) {
                        continue
                    }
                    full.append(delta)
                    onDelta(delta)
                }
            }
        } catch (ex: IOException) {
            throw QwenChatException("Qwen request failed.", ex)
        }
        if (full.isEmpty()) {
            throw QwenChatException("Qwen response did not include assistant content.")
        }
        return ChatResponse(content = full.toString(), modelId = modelId)
    }

    private fun completionsUri(): String = config.baseUrl.trimEnd('/') + "/chat/completions"

    private fun httpRequest(body: String): HttpRequest =
        HttpRequest.newBuilder()
            .uri(URI.create(completionsUri()))
            .timeout(config.requestTimeout)
            .header("Authorization", "Bearer ${config.apiKey}")
            .header("Content-Type", "application/json")
            .header("Accept", "text/event-stream")
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build()

    private fun requestBody(
        modelId: String,
        request: ChatRequest,
        stream: Boolean,
    ): String {
        val root = AgentJson.mapper.createObjectNode()
        root.put("model", modelId)
        root.put("temperature", request.temperature)
        root.put("stream", stream)
        // Thinking models may spend a long time on hidden reasoning before any
        // assistant content token. Agent chat needs visible tokens immediately.
        if (QwenModels.find(modelId)?.supportsThinking == true) {
            root.put("enable_thinking", false)
        }
        val messages = root.putArray("messages")
        request.messages.forEach { message ->
            val node = messages.addObject()
            node.put("role", message.role.name.lowercase())
            node.put("content", message.content)
        }
        return AgentJson.mapper.writeValueAsString(root)
    }

    private fun sendString(body: String): HttpResponse<String> =
        try {
            httpClient.send(httpRequest(body), HttpResponse.BodyHandlers.ofString())
        } catch (ex: IOException) {
            throw QwenChatException("Qwen request failed.", ex)
        } catch (ex: InterruptedException) {
            Thread.currentThread().interrupt()
            throw QwenChatException("Qwen request was interrupted.", ex)
        }

    private fun readContent(body: String): String {
        val root = try {
            AgentJson.mapper.readTree(body)
        } catch (ex: IOException) {
            throw QwenChatException("Qwen response was not valid JSON.", ex)
        }
        val content = root.path("choices").path(0).path("message").path("content")
        val text = contentText(content)
        if (text.isNullOrBlank()) {
            throw QwenChatException("Qwen response did not include assistant content.")
        }
        return text
    }

    private fun readDelta(data: String): String? {
        val root = try {
            AgentJson.mapper.readTree(data)
        } catch (ignored: IOException) {
            return null
        }
        val delta = root.path("choices").path(0).path("delta").path("content")
        return contentText(delta)
    }

    private fun contentText(content: JsonNode): String? {
        if (content.isMissingNode || content.isNull) {
            return null
        }
        if (content.isTextual) {
            return content.asText()
        }
        if (!content.isArray) {
            return null
        }
        return content.joinToString(separator = "") { element ->
            when {
                element.isTextual -> element.asText()
                else -> element.path("text").asText("")
            }
        }
    }

    companion object {
        private fun defaultClient(): HttpClient =
            HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NEVER)
                .connectTimeout(Duration.ofSeconds(10))
                .build()
    }
}
