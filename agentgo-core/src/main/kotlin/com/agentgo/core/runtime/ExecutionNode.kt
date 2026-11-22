package com.agentgo.core.runtime

import com.agentgo.core.event.AgentEventDraft
import com.agentgo.core.event.AgentEventPublisher
import com.agentgo.core.event.AgentMessageRole
import com.agentgo.core.event.CliLifecyclePhase
import com.agentgo.core.event.NodeLifecyclePhase
import com.agentgo.core.event.ToolLifecyclePhase
import com.agentgo.core.tool.PendingToolCall
import com.agentgo.core.tool.ToolExecutor
import com.agentgo.core.tool.ToolKind
import com.agentgo.core.tool.ToolResult
import com.agentgo.core.tool.ToolRuntimeContext
import com.agentgo.core.tool.ToolSpec

/**
 * Runs one allow-listed tool call and writes the result back into the transcript.
 *
 * @param toolExecutor side-effect boundary
 * @param tools tools the model may execute
 * @param events runtime publisher for this node's events
 */
class ExecutionNode(
    private val toolExecutor: ToolExecutor,
    private val tools: List<ToolSpec>,
    private val events: AgentEventPublisher,
) {
    /**
     * Publishes one event for this node.
     *
     * @param draft node-authored event
     */
    fun publish(state: AgentSessionState, draft: AgentEventDraft) {
        events.publish(state.runId(), draft)
    }

    /**
     * Executes the pending tool call, or records a refusal.
     *
     * @param state current session
     * @return channel updates
     */
    fun apply(state: AgentSessionState): Map<String, Any> {
        publish(state, AgentEventDraft.node(AgentNodeIds.EXECUTION, NodeLifecyclePhase.STARTED))
        val call = state.pendingToolCall()
        if (call == null) {
            publish(state, 
                AgentEventDraft.tool(
                    AgentNodeIds.EXECUTION,
                    "",
                    ToolLifecyclePhase.FAILED,
                    "No tool call was pending.",
                ),
            )
            publish(state, 
                AgentEventDraft.node(
                    AgentNodeIds.EXECUTION,
                    NodeLifecyclePhase.FAILED,
                    "No tool call was pending.",
                ),
            )
            return resultUpdate(
                state,
                ToolResult(name = "", success = false, output = "", error = "No tool call was pending."),
            )
        }
        val spec = tools.firstOrNull { it.name == call.name }
        val allowed = spec != null
        val input = formatToolInput(call)
        publish(
            state,
            AgentEventDraft.tool(AgentNodeIds.EXECUTION, call.name, ToolLifecyclePhase.STARTED, input),
        )
        publish(state, AgentEventDraft.tool(AgentNodeIds.EXECUTION, call.name, ToolLifecyclePhase.RUNNING))
        if (spec?.kind == ToolKind.CLI) {
            publish(
                state,
                AgentEventDraft.cli(AgentNodeIds.EXECUTION, call.name, CliLifecyclePhase.STARTED, input),
            )
            publish(state, AgentEventDraft.cli(AgentNodeIds.EXECUTION, call.name, CliLifecyclePhase.RUNNING))
        }
        val result = execute(call, allowed, state.toolRuntimeContext())
        val summary = if (result.success) result.output else result.error ?: "Tool failed."
        val toolPhase = if (result.success) ToolLifecyclePhase.SUCCEEDED else ToolLifecyclePhase.FAILED
        publish(state, AgentEventDraft.tool(AgentNodeIds.EXECUTION, call.name, toolPhase, summary))
        if (spec?.kind == ToolKind.CLI) {
            val cliPhase = if (result.success) CliLifecyclePhase.SUCCEEDED else CliLifecyclePhase.FAILED
            publish(state, AgentEventDraft.cli(AgentNodeIds.EXECUTION, call.name, cliPhase, summary))
        }
        publish(state, AgentEventDraft.message(AgentNodeIds.EXECUTION, AgentMessageRole.TOOL, summary))
        publish(state, AgentEventDraft.node(AgentNodeIds.EXECUTION, NodeLifecyclePhase.COMPLETED))
        return resultUpdate(state, result)
    }

    /**
     * Builds a user-visible preview of the tool invocation for STARTED events.
     *
     * Prefer the bash/CLI script line, then optional Python source, then remaining
     * arguments (excluding environment blobs).
     */
    private fun formatToolInput(call: PendingToolCall): String {
        val script =
            call.arguments["script"]?.trim().orEmpty().ifBlank {
                call.arguments["command"]?.trim().orEmpty()
            }
        val code =
            call.arguments["code"]?.trim().orEmpty().ifBlank {
                call.arguments["source"]?.trim().orEmpty()
            }
        if (script.isNotEmpty() || code.isNotEmpty()) {
            return buildString {
                if (script.isNotEmpty()) {
                    append(script)
                }
                if (code.isNotEmpty()) {
                    if (isNotEmpty()) append('\n')
                    append(code)
                }
            }
        }
        return call.arguments
            .filterKeys { key ->
                key != "environment" && key.isNotBlank()
            }
            .entries
            .joinToString(separator = "\n") { (key, value) -> "$key=$value" }
    }

    private fun execute(
        call: PendingToolCall,
        allowed: Boolean,
        context: ToolRuntimeContext,
    ): ToolResult {
        if (!allowed) {
            return ToolResult(
                name = call.name,
                success = false,
                output = "",
                error = "Tool is not allowed: ${call.name}",
            )
        }
        return try {
            toolExecutor.execute(call, context)
        } catch (ex: Exception) {
            ToolResult(
                name = call.name,
                success = false,
                output = "",
                error = ex.message ?: "Tool execution failed.",
            )
        }
    }

    private fun resultUpdate(state: AgentSessionState, result: ToolResult): Map<String, Any> {
        val summary = if (result.success) result.output else result.error ?: "Tool failed."
        val label = result.name.ifBlank { "tool" }
        return mapOf(
            AgentSessionState.TRACE to listOf(AgentNodeIds.EXECUTION),
            AgentSessionState.STEP_COUNT to state.stepCount() + 1,
            AgentSessionState.PENDING_TOOL_NAME to "",
            AgentSessionState.PENDING_TOOL_ARGUMENTS to "",
            AgentSessionState.ROUTE to "",
            AgentSessionState.TOOL_RESULTS to listOf(result),
            AgentSessionState.MESSAGES to listOf(AgentMessage(AgentMessage.TOOL, "[$label] $summary")),
        )
    }
}
