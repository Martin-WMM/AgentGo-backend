package com.agentgo.core.runtime

import com.agentgo.core.event.AgentEventDraft
import com.agentgo.core.event.AgentEventPublisher
import com.agentgo.core.event.AgentMessageRole
import com.agentgo.core.event.NodeLifecyclePhase

/**
 * Normalizes user input and opens the transcript.
 *
 * @param events runtime publisher for this node's events
 */
class InputNode(
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
     * Stores trimmed input, or finishes immediately when the input is blank.
     *
     * @param state current session
     * @return channel updates
     */
    fun apply(state: AgentSessionState): Map<String, Any> {
        publish(state, AgentEventDraft.node(AgentNodeIds.INPUT, NodeLifecyclePhase.STARTED))
        val normalized = state.input().trim()
        if (normalized.isEmpty()) {
            publish(state, 
                AgentEventDraft.node(
                    AgentNodeIds.INPUT,
                    NodeLifecyclePhase.COMPLETED,
                    "Input must not be blank.",
                ),
            )
            return mapOf(
                AgentSessionState.INPUT to "",
                AgentSessionState.ROUTE to AgentRoutes.FINISH,
                AgentSessionState.OUTPUT to "Input must not be blank.",
                AgentSessionState.TRACE to listOf(AgentNodeIds.INPUT),
            )
        }
        publish(state, AgentEventDraft.message(AgentNodeIds.INPUT, AgentMessageRole.USER, normalized))
        publish(state, AgentEventDraft.node(AgentNodeIds.INPUT, NodeLifecyclePhase.COMPLETED))
        return mapOf(
            AgentSessionState.INPUT to normalized,
            AgentSessionState.ROUTE to "",
            AgentSessionState.TRACE to listOf(AgentNodeIds.INPUT),
            AgentSessionState.MESSAGES to listOf(AgentMessage(AgentMessage.USER, normalized)),
        )
    }
}
