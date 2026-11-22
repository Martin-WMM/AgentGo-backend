package com.agentgo.core.event

/**
 * Publishes one node-authored event for an active run.
 *
 * Implementations attach the run's shared fields before the event leaves the runtime.
 */
fun interface AgentEventPublisher {
    /**
     * Publishes [draft] for [runId].
     *
     * @param runId id of the active run stored in graph state
     * @param draft node-authored event
     */
    fun publish(runId: String, draft: AgentEventDraft)
}
