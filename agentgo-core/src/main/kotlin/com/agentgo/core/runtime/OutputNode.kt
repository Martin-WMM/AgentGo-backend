package com.agentgo.core.runtime

import com.agentgo.core.event.AgentEventDraft
import com.agentgo.core.event.AgentEventPublisher
import com.agentgo.core.event.NodeLifecyclePhase

/**
 * Copies the finished answer into the user-facing output channel.
 *
 * @param events runtime publisher for this node's events
 */
class OutputNode(
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
     * Selects the text that should be returned to the caller.
     *
     * @param state current session
     * @return channel updates
     */
    fun apply(state: AgentSessionState): Map<String, Any> {
        publish(state, AgentEventDraft.node(AgentNodeIds.OUTPUT, NodeLifecyclePhase.STARTED))
        val output = state.output().trim().ifBlank { state.finalAnswer().trim() }
            .ifBlank { "The agent produced no response." }
        publish(state, AgentEventDraft.node(AgentNodeIds.OUTPUT, NodeLifecyclePhase.COMPLETED, output))
        return mapOf(
            AgentSessionState.OUTPUT to output,
            AgentSessionState.TRACE to listOf(AgentNodeIds.OUTPUT),
        )
    }
}
