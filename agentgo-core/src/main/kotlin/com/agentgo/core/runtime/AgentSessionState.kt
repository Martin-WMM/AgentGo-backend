package com.agentgo.core.runtime

import com.agentgo.core.json.AgentJson
import com.agentgo.core.tool.PendingToolCall
import com.agentgo.core.tool.ToolResult
import com.agentgo.core.tool.ToolRuntimeContext
import java.util.ArrayList
import org.bsc.langgraph4j.state.AgentState
import org.bsc.langgraph4j.state.AgentStateFactory
import org.bsc.langgraph4j.state.Channel
import org.bsc.langgraph4j.state.Channels

/**
 * Mutable session state carried through the AgentGo graph.
 *
 * List channels append. Scalar channels keep the latest write.
 *
 * @param initData initial channel values
 */
class AgentSessionState(initData: Map<String, Any?>) : AgentState(initData) {
    /** Normalized user input. */
    fun input(): String = text(INPUT)

    /** Structured plan JSON from the Plan node. */
    fun plan(): String = text(PLAN)

    /** User-facing output, when a node has already chosen it. */
    fun output(): String = text(OUTPUT)

    /** Latest model answer that should be shown when [output] is blank. */
    fun finalAnswer(): String = text(FINAL_ANSWER)

    /** Next edge selector. */
    fun route(): String = text(ROUTE)

    /** Model id for this run. */
    fun modelId(): String = text(MODEL_ID)

    /** Id of the active runtime run. */
    fun runId(): String = text(RUN_ID)

    /** Conversation thread id for this run. */
    fun threadId(): String = text(THREAD_ID)

    /**
     * Rebuilds the tool runtime context from channels written at run start.
     *
     * @return headers, metadata, and ids for tool / CLI side effects
     */
    fun toolRuntimeContext(): ToolRuntimeContext =
        ToolRuntimeContext(
            runId = runId(),
            threadId = threadId(),
            headers = AgentJson.readArguments(text(HEADERS)),
            metadata = AgentJson.readArguments(text(METADATA)),
        )

    /** Completed tool executions. */
    fun stepCount(): Int = this.value(STEP_COUNT, 0)

    /** Conversation accumulated by the nodes. */
    fun messages(): List<AgentMessage> = items(MESSAGES)

    /** Node ids in visit order. */
    fun trace(): List<String> = items(TRACE)

    /** Tool results in execution order. */
    fun toolResults(): List<ToolResult> = items(TOOL_RESULTS)

    /**
     * Tool call waiting for the execution node.
     *
     * @return the call, or null when the model did not request a tool
     */
    fun pendingToolCall(): PendingToolCall? {
        val name = text(PENDING_TOOL_NAME)
        if (name.isBlank()) {
            return null
        }
        return PendingToolCall(name, AgentJson.readArguments(text(PENDING_TOOL_ARGUMENTS)))
    }

    private fun text(key: String): String = this.value(key, "")

    @Suppress("UNCHECKED_CAST")
    private fun <T> items(key: String): List<T> {
        val stored = this.value<List<*>>(key).orElse(emptyList<Any>())
        return stored.map { it as T }
    }

    companion object {
        const val INPUT: String = "input"
        const val PLAN: String = "plan"
        const val OUTPUT: String = "output"
        const val FINAL_ANSWER: String = "finalAnswer"
        const val ROUTE: String = "route"
        const val MODEL_ID: String = "modelId"
        const val RUN_ID: String = "runId"
        const val THREAD_ID: String = "threadId"
        const val HEADERS: String = "headers"
        const val METADATA: String = "metadata"
        const val STEP_COUNT: String = "stepCount"
        const val PENDING_TOOL_NAME: String = "pendingToolName"
        const val PENDING_TOOL_ARGUMENTS: String = "pendingToolArguments"
        const val MESSAGES: String = "messages"
        const val TRACE: String = "trace"
        const val TOOL_RESULTS: String = "toolResults"

        val SCHEMA: Map<String, Channel<*>> = mapOf(
            MESSAGES to Channels.appenderWithDuplicate<AgentMessage> { ArrayList() },
            TRACE to Channels.appenderWithDuplicate<String> { ArrayList() },
            TOOL_RESULTS to Channels.appenderWithDuplicate<ToolResult> { ArrayList() },
            INPUT to Channels.base { "" },
            PLAN to Channels.base { "" },
            OUTPUT to Channels.base { "" },
            FINAL_ANSWER to Channels.base { "" },
            ROUTE to Channels.base { "" },
            MODEL_ID to Channels.base { "" },
            RUN_ID to Channels.base { "" },
            THREAD_ID to Channels.base { "" },
            HEADERS to Channels.base { "" },
            METADATA to Channels.base { "" },
            PENDING_TOOL_NAME to Channels.base { "" },
            PENDING_TOOL_ARGUMENTS to Channels.base { "" },
            STEP_COUNT to Channels.base { 0 },
        )

        val FACTORY: AgentStateFactory<AgentSessionState> = AgentStateFactory { initData ->
            AgentSessionState(initData)
        }
    }
}
