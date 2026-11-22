package com.agentgo.core.agent

import com.agentgo.core.event.AgentEvent
import com.agentgo.core.event.AgentEventKind
import com.agentgo.core.event.NodeLifecyclePhase
import com.agentgo.core.event.ToolLifecyclePhase
import com.agentgo.core.model.ChatModel
import com.agentgo.core.model.QwenModels
import com.agentgo.core.runtime.AgentNodeIds
import com.agentgo.core.runtime.AgentRunOptions
import com.agentgo.core.runtime.AgentRunResult
import com.agentgo.core.runtime.AgentRuntime
import com.agentgo.core.runtime.AgentStreamCommand
import com.agentgo.core.tool.RejectingToolExecutor
import com.agentgo.core.tool.ToolExecutor
import java.util.UUID
import org.bsc.langgraph4j.checkpoint.BaseCheckpointSaver

/**
 * AgentGo agent.
 *
 * The run is a LangGraph: Input, Plan, then LLM and Execution until the model
 * returns a final answer or the step budget is exhausted, then Output.
 * [stream] emits agent, node, message, tool, and CLI lifecycle events.
 * Only a Qwen model with function calling can be selected.
 *
 * @param chatModel planner and decision model
 * @param toolExecutor side-effect boundary used by the execution node
 * @param options model, step budget, and allow-listed tools
 * @param checkpointSaver optional PostgreSQL or in-memory checkpoint saver
 */
class AgentGoAgent(
    chatModel: ChatModel,
    toolExecutor: ToolExecutor = RejectingToolExecutor(),
    private val options: AgentRunOptions = AgentRunOptions(),
    checkpointSaver: BaseCheckpointSaver? = null,
) {
    /** Runtime that owns the compiled graph and the shared event fields. */
    val runtime: AgentRuntime = AgentRuntime()

    init {
        QwenModels.requireAgentCapable(options.modelId)
        runtime.attach(
            AgentGoAgentGraph(
                chatModel = chatModel,
                toolExecutor = toolExecutor,
                options = options,
                events = runtime,
                checkpointSaver = checkpointSaver,
            ).compiled(),
        )
    }

    /**
     * Streams one user turn.
     *
     * @param command run input, thread, headers, and metadata
     * @param emit consumer invoked once per event
     */
    fun stream(command: AgentStreamCommand, emit: (AgentEvent) -> Unit) {
        runtime.stream(command, emit)
    }

    /**
     * Runs one user turn and returns the terminal output.
     *
     * @param input raw user text
     * @return output, plan, and the node trace
     */
    fun run(input: String): AgentRunResult {
        var output = ""
        var plan = ""
        var steps = 0
        val trace = mutableListOf<String>()
        stream(
            AgentStreamCommand(
                input = input,
                modelId = options.modelId,
                threadId = UUID.randomUUID().toString(),
                headers = emptyMap(),
                metadata = emptyMap(),
            ),
        ) { event ->
            if (
                event.kind == AgentEventKind.NODE_LIFECYCLE &&
                event.phase == NodeLifecyclePhase.COMPLETED.name
            ) {
                event.nodeId?.let { trace += it }
            }
            if (event.kind == AgentEventKind.MESSAGE && event.nodeId == AgentNodeIds.PLAN) {
                plan = event.content.orEmpty()
            }
            if (
                event.kind == AgentEventKind.TOOL_LIFECYCLE &&
                (
                    event.phase == ToolLifecyclePhase.SUCCEEDED.name ||
                        event.phase == ToolLifecyclePhase.FAILED.name
                    )
            ) {
                steps += 1
            }
            if (event.finished) {
                output = event.content.orEmpty()
            }
        }
        return AgentRunResult(
            output = output,
            plan = plan,
            trace = trace,
            stepCount = steps,
            modelId = options.modelId,
        )
    }
}
