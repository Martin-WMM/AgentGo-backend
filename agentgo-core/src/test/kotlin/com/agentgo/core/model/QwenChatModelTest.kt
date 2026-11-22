package com.agentgo.core.model

import com.sun.net.httpserver.HttpServer
import java.net.InetSocketAddress
import java.net.http.HttpClient
import java.time.Duration
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class QwenChatModelTest {
    @Test
    fun `complete reads assistant text and uses the configured model when the request is blank`() {
        withServer("""{"choices":[{"message":{"content":"pong"}}]}""") { model, seen ->
            val response = model.complete(
                ChatRequest(
                    modelId = " ",
                    messages = listOf(ChatMessage(ChatRole.USER, "ping")),
                    temperature = 0.0,
                ),
            )

            assertEquals("pong", response.content)
            assertEquals(QwenModels.DEFAULT_ID, response.modelId)
            assertTrue(seen.single().contains(""""model":"${QwenModels.DEFAULT_ID}""""))
            assertTrue(seen.single().contains(""""role":"user""""))
            assertTrue(seen.single().contains(""""enable_thinking":false"""))
        }
    }

    @Test
    fun `complete joins array content`() {
        withServer("""{"choices":[{"message":{"content":["hello",{"text":" world"},{"type":"skip"}]}}]}""") { model, _ ->
            val response = model.complete(ChatRequest("qwen-plus", listOf(ChatMessage(ChatRole.SYSTEM, "sys"))))

            assertEquals("hello world", response.content)
            assertEquals("qwen-plus", response.modelId)
        }
    }

    @Test
    fun `complete reports an http failure invalid json and missing content`() {
        withServer("""{"error":"no"}""", status = 500) { model, _ ->
            assertTrue(assertFailsWith<QwenChatException> { model.complete(request()) }.message!!.contains("500"))
        }
        withServer("not-json") { model, _ ->
            assertEquals(
                "Qwen response was not valid JSON.",
                assertFailsWith<QwenChatException> { model.complete(request()) }.message,
            )
        }
        withServer("""{"choices":[{"message":{"content":null}}]}""") { model, _ ->
            assertEquals(
                "Qwen response did not include assistant content.",
                assertFailsWith<QwenChatException> { model.complete(request()) }.message,
            )
        }
        withServer("""{"choices":[{"message":{"content":{"text":"x"}}}]}""") { model, _ ->
            assertEquals(
                "Qwen response did not include assistant content.",
                assertFailsWith<QwenChatException> { model.complete(request()) }.message,
            )
        }
    }

    @Test
    fun `stream reads sse deltas`() {
        val sse = """
            data: {"choices":[{"delta":{"content":"Hel"}}]}

            data: {"choices":[{"delta":{"content":"lo"}}]}

            data: [DONE]

        """.trimIndent()
        withServer(sse) { model, seen ->
            val deltas = mutableListOf<String>()
            val response = model.stream(request(), deltas::add)

            assertEquals("Hello", response.content)
            assertEquals(listOf("Hel", "lo"), deltas)
            assertTrue(seen.single().contains(""""stream":true"""))
        }
    }

    @Test
    fun `complete wraps a connection failure`() {
        val model = QwenChatModel(
            QwenClientConfig(
                apiKey = "test-key",
                baseUrl = "http://127.0.0.1:1/v1",
                requestTimeout = Duration.ofSeconds(2),
            ),
            HttpClient.newBuilder().connectTimeout(Duration.ofMillis(200)).build(),
        )

        val error = assertFailsWith<QwenChatException> { model.complete(request()) }

        assertEquals("Qwen request failed.", error.message)
        assertTrue(error.cause != null)
    }

    @Test
    fun `default client is created when none is supplied`() {
        val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        server.createContext("/v1/chat/completions") { exchange ->
            val body = """{"choices":[{"message":{"content":"ok"}}]}"""
            exchange.sendResponseHeaders(200, body.toByteArray().size.toLong())
            exchange.responseBody.use { it.write(body.toByteArray()) }
        }
        server.start()
        try {
            val model = QwenChatModel(
                QwenClientConfig(
                    apiKey = "test-key",
                    baseUrl = "http://127.0.0.1:${server.address.port}/v1/",
                    requestTimeout = Duration.ofSeconds(5),
                ),
            )

            assertEquals("ok", model.complete(request()).content)
        } finally {
            server.stop(0)
        }
    }

    private fun request() = ChatRequest("qwen3.7-plus", listOf(ChatMessage(ChatRole.USER, "ping")))

    private fun withServer(
        response: String,
        status: Int = 200,
        check: (QwenChatModel, List<String>) -> Unit,
    ) {
        val seen = mutableListOf<String>()
        val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        server.createContext("/chat/completions") { exchange ->
            seen += exchange.requestBody.readBytes().decodeToString()
            exchange.sendResponseHeaders(status, response.toByteArray().size.toLong())
            exchange.responseBody.use { it.write(response.toByteArray()) }
        }
        server.start()
        try {
            val model = QwenChatModel(
                QwenClientConfig(
                    apiKey = "test-key",
                    baseUrl = "http://127.0.0.1:${server.address.port}",
                    requestTimeout = Duration.ofSeconds(5),
                ),
                HttpClient.newBuilder().build(),
            )
            check(model, seen)
        } finally {
            server.stop(0)
        }
    }
}
