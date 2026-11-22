package com.agentgo.ai.service.impl

import com.agentgo.ai.exception.InvalidAgentStreamRequestException
import com.agentgo.ai.service.ChatSessionTurnWriter
import com.agentgo.core.agent.AgentGoAgent
import com.agentgo.core.model.ChatModel
import com.agentgo.core.model.ChatResponse
import com.agentgo.core.model.PlanParser
import com.agentgo.core.model.QwenModels
import com.agentgo.core.prompt.AgentPrompts
import com.agentgo.dto.agent.AgentStreamConfig
import com.agentgo.dto.agent.AgentStreamRequest
import com.agentgo.dto.agent.AgentStreamState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class AgentStreamServiceImplTest {
    @Test
    fun `stream maps runtime events into common sse payloads`() {
        val runtime = AgentGoAgent(
            chatModel = ChatModel { request ->
                val planning = request.messages.any { it.content.contains(AgentPrompts.PLANNER_MARKER) }
                val content = if (planning) {
                    "Answer directly."
                } else {
                    """{"type":"final","content":"hello"}"""
                }
                ChatResponse(content, request.modelId)
            },
        ).runtime
        val service = AgentStreamServiceImpl(runtime, QwenModels.DEFAULT_ID)
        val events = mutableListOf<com.agentgo.commons.dto.http.response.CommonEventResponse<com.agentgo.dto.agent.AgentStreamEvent>>()

        service.stream(request(), "req-1", events::add)

        assertTrue(events.all { it.namespace == AgentStreamServiceImpl.NAMESPACE })
        assertTrue(events.all { it.requestId == "req-1" })
        assertEquals("agent.lifecycle", events.first().eventName)
        assertEquals("AGENT_LIFECYCLE", events.first().eventType)
        assertEquals("thread-1", events.first().data?.threadId)
        assertEquals("zh-CN", events.first().data?.headers?.get("Accept-Language"))
        assertEquals("ui", events.first().data?.metadata?.get("source"))
        assertTrue(events.last().finished)
        assertEquals("hello", events.last().data?.content)
    }

    @Test
    fun `stream rejects a blank header name`() {
        val service = AgentStreamServiceImpl(AgentGoAgent(ChatModel { ChatResponse("", it.modelId) }).runtime, QwenModels.DEFAULT_ID)
        val request = request().apply { headers = mapOf(" " to "zh-CN") }

        assertFailsWith<InvalidAgentStreamRequestException> {
            service.stream(request, "req-1") { }
        }
    }

    @Test
    fun `stream persists a successful turn when session metadata is present`() {
        val runtime = AgentGoAgent(
            chatModel = ChatModel { request ->
                val planning = request.messages.any { it.content.contains(AgentPrompts.PLANNER_MARKER) }
                val content = if (planning) {
                    "Answer directly."
                } else {
                    """{"type":"final","content":"hello"}"""
                }
                ChatResponse(content, request.modelId)
            },
        ).runtime
        val turnWriter = mock<ChatSessionTurnWriter>()
        val service = AgentStreamServiceImpl(runtime, QwenModels.DEFAULT_ID, turnWriter)

        service.stream(
            request().apply {
                metadata = mapOf(
                    "source" to "ui",
                    AgentStreamServiceImpl.SESSION_ID_METADATA to "42",
                    AgentStreamServiceImpl.OWNER_USER_ID_METADATA to "ada",
                )
            },
            "req-2",
        ) { }

        verify(turnWriter).recordSuccessfulTurn(
            42L,
            "ada",
            "hello",
            "hello",
            PlanParser.normalize("Answer directly."),
        )
    }

    private fun request() = AgentStreamRequest().apply {
        state = AgentStreamState().apply { input = "hello" }
        config = AgentStreamConfig().apply { threadId = "thread-1" }
        headers = mapOf("Accept-Language" to "zh-CN")
        metadata = mapOf("source" to "ui")
    }
}
