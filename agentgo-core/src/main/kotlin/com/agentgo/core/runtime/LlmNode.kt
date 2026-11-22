package com.agentgo.core.runtime

import com.agentgo.core.event.AgentEventDraft
import com.agentgo.core.event.AgentEventPublisher
import com.agentgo.core.event.AgentMessageRole
import com.agentgo.core.event.NodeLifecyclePhase
import com.agentgo.core.json.AgentJson
import com.agentgo.core.model.ChatMessage
import com.agentgo.core.model.ChatModel
import com.agentgo.core.model.ChatRequest
import com.agentgo.core.model.ChatRole
import com.agentgo.core.model.LlmDecision
import com.agentgo.core.model.LlmDecisionParser
import com.agentgo.core.model.StreamingAnswerProbe
import com.agentgo.core.prompt.AgentPrompts

/**
 * AgentLoop ReAct decision node: follow the structured plan, call one tool, or finish.
 *
 * Final answers are plain Markdown and stream to the UI as
 * [AgentMessageRole.ASSISTANT_DELTA] fragments while the model generates.
 * Tool decisions stay buffered until the tool JSON is complete.
 *
 * @param chatModel model used for the decision
 * @param options step budget and tool catalog shown to the model
 * @param events runtime publisher for this node's events
 */
class LlmNode(
    private val chatModel: ChatModel,
    private val options: AgentRunOptions,
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
     * Stores a final answer or a pending tool call.
     *
     * @param state current session
     * @return channel updates
     */
    fun apply(state: AgentSessionState): Map<String, Any> {
        publish(state, AgentEventDraft.node(AgentNodeIds.LLM, NodeLifecyclePhase.STARTED))
        val probe = StreamingAnswerProbe { delta ->
            publish(
                state,
                AgentEventDraft.message(AgentNodeIds.LLM, AgentMessageRole.ASSISTANT_DELTA, delta),
            )
        }
        val response = try {
            chatModel.stream(
                ChatRequest(
                    modelId = state.modelId().ifBlank { options.modelId },
                    messages = prompt(state),
                ),
                probe::accept,
            )
        } catch (ex: Exception) {
            return failure(state, ex.message ?: "Model request failed.")
        }
        val content = response.content.ifBlank { probe.snapshot() }.trim()
        if (content.isBlank()) {
            return failure(state, "Model returned an empty response.")
        }
        return when (val decision = LlmDecisionParser.parse(content)) {
            is LlmDecision.Final -> finish(state, decision.content)
            is LlmDecision.ToolCall -> {
                if (state.stepCount() >= options.maxSteps) {
                    finish(state, "Step budget of ${options.maxSteps} is exhausted.")
                } else {
                    publish(
                        state,
                        AgentEventDraft.message(
                            AgentNodeIds.LLM,
                            AgentMessageRole.ASSISTANT,
                            content,
                        ),
                    )
                    publish(state, AgentEventDraft.node(AgentNodeIds.LLM, NodeLifecyclePhase.COMPLETED))
                    mapOf(
                        AgentSessionState.ROUTE to AgentRoutes.EXECUTE,
                        AgentSessionState.PENDING_TOOL_NAME to decision.name,
                        AgentSessionState.PENDING_TOOL_ARGUMENTS to AgentJson.writeArguments(decision.arguments),
                        AgentSessionState.FINAL_ANSWER to "",
                        AgentSessionState.OUTPUT to "",
                        AgentSessionState.TRACE to listOf(AgentNodeIds.LLM),
                        AgentSessionState.MESSAGES to listOf(
                            AgentMessage(AgentMessage.ASSISTANT, content),
                        ),
                    )
                }
            }
        }
    }

    private fun prompt(state: AgentSessionState): List<ChatMessage> {
        val tools = if (options.tools.isEmpty()) {
            "none"
        } else {
            options.tools.joinToString(separator = "\n") { tool -> "- ${tool.name}: ${tool.description}" }
        }
        val planJson = state.plan().ifBlank {
            """{"goal":"Answer the user request","steps":[{"id":1,"title":"Answer directly","instruction":"Produce the final answer for the user request."}]}"""
        }
        val system = AgentPrompts.reactSystem(planJson = planJson, toolsCatalog = tools)
        val history = state.messages().map { message ->
            val role = when (message.role) {
                AgentMessage.USER -> ChatRole.USER
                AgentMessage.TOOL -> ChatRole.TOOL
                else -> ChatRole.ASSISTANT
            }
            ChatMessage(role, message.content)
        }
        return listOf(ChatMessage(ChatRole.SYSTEM, system)) + history
    }

    private fun finish(state: AgentSessionState, content: String): Map<String, Any> {
        publish(state, AgentEventDraft.message(AgentNodeIds.LLM, AgentMessageRole.ASSISTANT, content))
        publish(state, AgentEventDraft.node(AgentNodeIds.LLM, NodeLifecyclePhase.COMPLETED))
        return mapOf(
            AgentSessionState.ROUTE to AgentRoutes.FINISH,
            AgentSessionState.FINAL_ANSWER to content,
            AgentSessionState.OUTPUT to "",
            AgentSessionState.PENDING_TOOL_NAME to "",
            AgentSessionState.PENDING_TOOL_ARGUMENTS to "",
            AgentSessionState.TRACE to listOf(AgentNodeIds.LLM),
            AgentSessionState.MESSAGES to listOf(AgentMessage(AgentMessage.ASSISTANT, content)),
        )
    }

    private fun failure(state: AgentSessionState, message: String): Map<String, Any> {
        val text = "Model request failed: $message"
        publish(state, AgentEventDraft.node(AgentNodeIds.LLM, NodeLifecyclePhase.FAILED, text))
        return mapOf(
            AgentSessionState.ROUTE to AgentRoutes.FINISH,
            AgentSessionState.OUTPUT to text,
            AgentSessionState.TRACE to listOf(AgentNodeIds.LLM),
        )
    }
}
