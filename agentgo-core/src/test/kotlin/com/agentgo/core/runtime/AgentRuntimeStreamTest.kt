package com.agentgo.core.runtime

import com.agentgo.core.agent.AgentGoAgent
import com.agentgo.core.event.AgentEvent
import com.agentgo.core.event.AgentEventDraft
import com.agentgo.core.event.AgentEventKind
import com.agentgo.core.event.AgentLifecyclePhase
import com.agentgo.core.event.CliLifecyclePhase
import com.agentgo.core.event.NodeLifecyclePhase
import com.agentgo.core.model.ChatModel
import com.agentgo.core.model.ChatRequest
import com.agentgo.core.model.ChatResponse
import com.agentgo.core.tool.PendingToolCall
import com.agentgo.core.tool.ToolExecutor
import com.agentgo.core.tool.ToolKind
import com.agentgo.core.tool.ToolResult
import com.agentgo.core.tool.ToolRuntimeContext
import com.agentgo.core.tool.ToolSpec
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AgentRuntimeStreamTest {
    @Test
    fun `stream publishes lifecycle message tool and cli events for one run`() {
        val agent = AgentGoAgent(
            chatModel = ScriptedChatModel(
                listOf(
                    "1. Run the shell.",
                    """{"type":"tool","name":"shell","arguments":{"script":"echo ok"}}""",
                    """{"type":"final","content":"done"}""",
                ),
            ),
            toolExecutor = MapToolExecutor { call ->
                ToolResult(call.name, true, "ok", null)
            },
            options = AgentRunOptions(
                tools = listOf(ToolSpec("shell", "Run a shell command", ToolKind.CLI)),
            ),
        )
        val events = mutableListOf<AgentEvent>()

        agent.stream(command(), events::add)

        assertEquals(
            listOf(
                mark(AgentEventKind.AGENT_LIFECYCLE, AgentLifecyclePhase.STARTED.name, null),
                mark(AgentEventKind.NODE_LIFECYCLE, NodeLifecyclePhase.STARTED.name, AgentNodeIds.INPUT),
                mark(AgentEventKind.MESSAGE, "USER", AgentNodeIds.INPUT),
                mark(AgentEventKind.NODE_LIFECYCLE, NodeLifecyclePhase.COMPLETED.name, AgentNodeIds.INPUT),
                mark(AgentEventKind.NODE_LIFECYCLE, NodeLifecyclePhase.STARTED.name, AgentNodeIds.PLAN),
                mark(AgentEventKind.MESSAGE, "ASSISTANT", AgentNodeIds.PLAN),
                mark(AgentEventKind.NODE_LIFECYCLE, NodeLifecyclePhase.COMPLETED.name, AgentNodeIds.PLAN),
                mark(AgentEventKind.NODE_LIFECYCLE, NodeLifecyclePhase.STARTED.name, AgentNodeIds.LLM),
                mark(AgentEventKind.MESSAGE, "ASSISTANT", AgentNodeIds.LLM),
                mark(AgentEventKind.NODE_LIFECYCLE, NodeLifecyclePhase.COMPLETED.name, AgentNodeIds.LLM),
                mark(AgentEventKind.NODE_LIFECYCLE, NodeLifecyclePhase.STARTED.name, AgentNodeIds.EXECUTION),
                mark(AgentEventKind.TOOL_LIFECYCLE, "STARTED", AgentNodeIds.EXECUTION),
                mark(AgentEventKind.TOOL_LIFECYCLE, "RUNNING", AgentNodeIds.EXECUTION),
                mark(AgentEventKind.CLI_LIFECYCLE, CliLifecyclePhase.STARTED.name, AgentNodeIds.EXECUTION),
                mark(AgentEventKind.CLI_LIFECYCLE, CliLifecyclePhase.RUNNING.name, AgentNodeIds.EXECUTION),
                mark(AgentEventKind.TOOL_LIFECYCLE, "SUCCEEDED", AgentNodeIds.EXECUTION),
                mark(AgentEventKind.CLI_LIFECYCLE, CliLifecyclePhase.SUCCEEDED.name, AgentNodeIds.EXECUTION),
                mark(AgentEventKind.MESSAGE, "TOOL", AgentNodeIds.EXECUTION),
                mark(AgentEventKind.NODE_LIFECYCLE, NodeLifecyclePhase.COMPLETED.name, AgentNodeIds.EXECUTION),
                mark(AgentEventKind.NODE_LIFECYCLE, NodeLifecyclePhase.STARTED.name, AgentNodeIds.LLM),
                mark(AgentEventKind.MESSAGE, "ASSISTANT", AgentNodeIds.LLM),
                mark(AgentEventKind.NODE_LIFECYCLE, NodeLifecyclePhase.COMPLETED.name, AgentNodeIds.LLM),
                mark(AgentEventKind.NODE_LIFECYCLE, NodeLifecyclePhase.STARTED.name, AgentNodeIds.OUTPUT),
                mark(AgentEventKind.NODE_LIFECYCLE, NodeLifecyclePhase.COMPLETED.name, AgentNodeIds.OUTPUT),
                mark(AgentEventKind.AGENT_LIFECYCLE, AgentLifecyclePhase.COMPLETED.name, null),
            ),
            events.map { mark(it.kind, it.phase, it.nodeId) },
        )
        assertTrue(events.all { it.threadId == "thread-1" })
        assertTrue(events.all { it.headers["Accept-Language"] == "zh-CN" })
        assertTrue(events.all { it.metadata["source"] == "ui" })
        assertEquals("agent.lifecycle", events.first().name)
        assertEquals("done", events.last { it.nodeId == AgentNodeIds.OUTPUT }.content)
        assertTrue(events.last().finished)
        assertFalse(events.dropLast(1).any { it.finished })
        assertEquals("ok", events.last { it.kind == AgentEventKind.MESSAGE && it.phase == "TOOL" }.content)
        assertEquals(
            "echo ok",
            events.first { it.kind == AgentEventKind.TOOL_LIFECYCLE && it.phase == "STARTED" }.content,
        )
        assertEquals(
            "echo ok",
            events.first { it.kind == AgentEventKind.CLI_LIFECYCLE && it.phase == "STARTED" }.content,
        )
    }

    @Test
    fun `stream omits cli events for an ordinary tool`() {
        val agent = AgentGoAgent(
            chatModel = ScriptedChatModel(
                listOf(
                    "Look it up.",
                    """{"type":"tool","name":"lookup","arguments":{"key":"alpha"}}""",
                    """{"type":"final","content":"alpha is 7"}""",
                ),
            ),
            toolExecutor = MapToolExecutor { ToolResult(it.name, true, "7", null) },
            options = AgentRunOptions(tools = listOf(ToolSpec("lookup", "Read a value"))),
        )
        val events = mutableListOf<AgentEvent>()

        agent.stream(command(), events::add)

        assertTrue(events.none { it.kind == AgentEventKind.CLI_LIFECYCLE })
        assertEquals("alpha is 7", events.last().content)
    }

    @Test
    fun `stream finishes a blank input without calling the model`() {
        var called = false
        val agent = AgentGoAgent(
            chatModel = ChatModel {
                called = true
                ChatResponse("unused", it.modelId)
            },
        )
        val events = mutableListOf<AgentEvent>()

        agent.stream(command(input = "   "), events::add)

        assertFalse(called)
        assertTrue(events.none { it.kind == AgentEventKind.MESSAGE })
        assertEquals("Input must not be blank.", events.last().content)
        assertTrue(events.last().finished)
    }

    @Test
    fun `publish outside a run fails`() {
        val runtime = AgentRuntime()

        val error = assertFailsWith<IllegalStateException> {
            runtime.publish("missing-run", AgentEventDraft.agent(AgentLifecyclePhase.STARTED))
        }

        assertEquals("Agent event published outside a run.", error.message)
    }

    @Test
    fun `stream publishes agent failure when the graph is not attached`() {
        val events = mutableListOf<AgentEvent>()

        AgentRuntime().stream(command(), events::add)

        assertEquals(AgentLifecyclePhase.STARTED.name, events.first().phase)
        assertEquals(AgentLifecyclePhase.FAILED.name, events.last().phase)
        assertTrue(events.last().finished)
    }

    private fun command(input: String = "hello") = AgentStreamCommand(
        input = input,
        modelId = "qwen3.7-plus",
        threadId = "thread-1",
        headers = mapOf("Accept-Language" to "zh-CN"),
        metadata = mapOf("source" to "ui"),
    )

    private fun mark(kind: AgentEventKind, phase: String, nodeId: String?) = Triple(kind, phase, nodeId)

    private class ScriptedChatModel(
        private val replies: List<String>,
    ) : ChatModel {
        private var index = 0

        override fun complete(request: ChatRequest): ChatResponse {
            val content = replies[index.coerceAtMost(replies.lastIndex)]
            index += 1
            return ChatResponse(content, request.modelId)
        }
    }

    private class MapToolExecutor(
        private val handler: (PendingToolCall) -> ToolResult,
    ) : ToolExecutor {
        override fun execute(call: PendingToolCall, context: ToolRuntimeContext): ToolResult = handler(call)
    }
}
