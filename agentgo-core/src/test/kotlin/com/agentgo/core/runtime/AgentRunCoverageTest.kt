package com.agentgo.core.runtime

import com.agentgo.core.agent.AgentGoAgent
import com.agentgo.core.event.AgentEventKind
import com.agentgo.core.event.AgentLifecyclePhase
import com.agentgo.core.model.ChatModel
import com.agentgo.core.model.ChatResponse
import com.agentgo.core.model.PlanParser
import com.agentgo.core.model.QwenModels
import com.agentgo.core.prompt.AgentPrompts
import com.agentgo.core.tool.PendingToolCall
import com.agentgo.core.tool.RejectingToolExecutor
import com.agentgo.core.tool.ToolKind
import com.agentgo.core.tool.ToolResult
import com.agentgo.core.tool.ToolRuntimeContext
import com.agentgo.core.tool.ToolSpec
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class AgentRunCoverageTest {
    @Test
    fun `run folds the plan and the final answer`() {
        val agent = AgentGoAgent(
            chatModel = ChatModel { request ->
                val planning = request.messages.any { it.content.contains(AgentPrompts.PLANNER_MARKER) }
                ChatResponse(if (planning) "Answer directly." else """{"type":"final","content":"hello"}""", request.modelId)
            },
        )

        val result = agent.run("hello")

        assertEquals("hello", result.output)
        assertEquals(PlanParser.normalize("Answer directly."), result.plan)
        assertEquals(listOf(AgentNodeIds.INPUT, AgentNodeIds.PLAN, AgentNodeIds.LLM, AgentNodeIds.OUTPUT), result.trace)
        assertEquals(0, result.stepCount)
        assertEquals(QwenModels.DEFAULT_ID, result.modelId)
    }

    @Test
    fun `a failing planner finishes the agent`() {
        val events = mutableListOf<com.agentgo.core.event.AgentEvent>()
        AgentGoAgent(ChatModel { throw IllegalStateException("down") }).stream(command(), events::add)

        assertEquals(AgentLifecyclePhase.COMPLETED.name, events.last().phase)
        assertTrue(events.last().content!!.contains("down"))
    }

    @Test
    fun `an empty model response finishes the agent`() {
        val events = mutableListOf<com.agentgo.core.event.AgentEvent>()
        AgentGoAgent(
            ChatModel { request ->
                val planning = request.messages.any { it.content.contains(AgentPrompts.PLANNER_MARKER) }
                ChatResponse(if (planning) "plan" else " ", request.modelId)
            },
        ).stream(command(), events::add)

        assertTrue(events.last().content!!.contains("empty response"))
    }

    @Test
    fun `a disallowed tool and a throwing tool both fail the tool lifecycle`() {
        val disallowed = eventsFor(
            """{"type":"tool","name":"missing","arguments":{}}""",
            tools = emptyList(),
        )
        assertTrue(disallowed.any { it.kind == AgentEventKind.TOOL_LIFECYCLE && it.phase == "FAILED" })

        val thrown = eventsFor(
            """{"type":"tool","name":"lookup","arguments":{"nested":{"n":1}}}""",
            tools = listOf(ToolSpec("lookup", "Read a value")),
            executor = { throw IllegalStateException("boom") },
        )
        assertTrue(thrown.any { it.content?.contains("boom") == true })
    }

    @Test
    fun `cli failure and the step budget are reported`() {
        val failed = eventsFor(
            """{"type":"tool","name":"shell","arguments":{}}""",
            tools = listOf(ToolSpec("shell", "Run a shell", ToolKind.CLI)),
            executor = { ToolResult(it.name, false, "", "cli down") },
        )
        assertTrue(failed.any { it.kind == AgentEventKind.CLI_LIFECYCLE && it.phase == "FAILED" })

        val budget = eventsFor(
            """{"type":"tool","name":"lookup","arguments":{}}""",
            tools = listOf(ToolSpec("lookup", "Read a value")),
            maxSteps = 0,
        )
        assertTrue(budget.last().content!!.contains("Step budget"))
    }

    @Test
    fun `the rejecting executor and invalid run options fail closed`() {
        val result =
            RejectingToolExecutor().execute(PendingToolCall("lookup", emptyMap()), ToolRuntimeContext.EMPTY)

        assertEquals("Tool execution is not configured: lookup", result.error)
        assertFailsWith<IllegalArgumentException> { AgentRunOptions(modelId = " ") }
        assertFailsWith<IllegalArgumentException> { AgentRunOptions(maxSteps = -1) }
        assertFailsWith<IllegalArgumentException> {
            AgentRunOptions(tools = listOf(ToolSpec("same", "one"), ToolSpec("same", "two")))
        }
        assertFailsWith<IllegalArgumentException> { ToolSpec(" ", "blank") }
        assertFailsWith<IllegalArgumentException> {
            AgentRuntime().stream(command(threadId = " "), mutableListOf<com.agentgo.core.event.AgentEvent>()::add)
        }
    }

    private fun eventsFor(
        decision: String,
        tools: List<ToolSpec>,
        maxSteps: Int = 8,
        executor: (PendingToolCall) -> ToolResult = { ToolResult(it.name, true, "ok", null) },
    ): List<com.agentgo.core.event.AgentEvent> {
        val events = mutableListOf<com.agentgo.core.event.AgentEvent>()
        AgentGoAgent(
            chatModel = ChatModel { request ->
                val planning = request.messages.any { it.content.contains(AgentPrompts.PLANNER_MARKER) }
                val content = if (planning) "plan" else decision
                ChatResponse(content, request.modelId)
            },
            toolExecutor = { call, _ -> executor(call) },
            options = AgentRunOptions(maxSteps = maxSteps, tools = tools),
        ).stream(command(), events::add)
        return events
    }

    private fun command(threadId: String = "thread-1") = AgentStreamCommand(
        input = "hello",
        modelId = "qwen3.7-plus",
        threadId = threadId,
        headers = emptyMap(),
        metadata = emptyMap(),
    )
}
