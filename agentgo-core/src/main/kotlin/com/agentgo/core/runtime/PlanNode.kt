package com.agentgo.core.runtime

import com.agentgo.core.event.AgentEventDraft
import com.agentgo.core.event.AgentEventPublisher
import com.agentgo.core.event.AgentMessageRole
import com.agentgo.core.event.NodeLifecyclePhase
import com.agentgo.core.model.ChatMessage
import com.agentgo.core.model.ChatModel
import com.agentgo.core.model.ChatRequest
import com.agentgo.core.model.ChatRole
import com.agentgo.core.model.PlanParser
import com.agentgo.core.prompt.AgentPrompts

/**
 * Asks the model for a structured JSON plan and stores it before the ReAct loop.
 *
 * @param chatModel model used to write the plan
 * @param events runtime publisher for this node's events
 */
class PlanNode(
    private val chatModel: ChatModel,
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
     * Writes the plan, or finishes when the model call fails.
     *
     * @param state current session
     * @return channel updates
     */
    fun apply(state: AgentSessionState): Map<String, Any> {
        publish(state, AgentEventDraft.node(AgentNodeIds.PLAN, NodeLifecyclePhase.STARTED))
        val response = try {
            chatModel.complete(
                ChatRequest(
                    modelId = state.modelId(),
                    messages = listOf(
                        ChatMessage(ChatRole.SYSTEM, AgentPrompts.planSystem()),
                        ChatMessage(ChatRole.USER, state.input()),
                    ),
                ),
            )
        } catch (ex: Exception) {
            return failure(state, ex.message ?: "Model request failed.")
        }
        val plan = PlanParser.normalize(response.content)
        publish(state, AgentEventDraft.message(AgentNodeIds.PLAN, AgentMessageRole.ASSISTANT, plan))
        publish(state, AgentEventDraft.node(AgentNodeIds.PLAN, NodeLifecyclePhase.COMPLETED))
        return mapOf(
            AgentSessionState.PLAN to plan,
            AgentSessionState.ROUTE to "",
            AgentSessionState.TRACE to listOf(AgentNodeIds.PLAN),
            AgentSessionState.MESSAGES to listOf(
                AgentMessage(AgentMessage.ASSISTANT, "Structured plan ready:\n$plan"),
            ),
        )
    }

    private fun failure(state: AgentSessionState, message: String): Map<String, Any> {
        val text = "Model request failed: $message"
        publish(state, AgentEventDraft.node(AgentNodeIds.PLAN, NodeLifecyclePhase.FAILED, text))
        return mapOf(
            AgentSessionState.ROUTE to AgentRoutes.FINISH,
            AgentSessionState.OUTPUT to text,
            AgentSessionState.TRACE to listOf(AgentNodeIds.PLAN),
        )
    }
}
