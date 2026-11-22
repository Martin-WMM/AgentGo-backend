package com.agentgo.core.runtime

import java.util.concurrent.TimeUnit
import org.bsc.langgraph4j.action.AsyncNodeAction
import org.slf4j.LoggerFactory

/**
 * Builds LangGraph node actions that record start, completion, and failure logs.
 */
object AgentNodeActions {
    private val log = LoggerFactory.getLogger(AgentNodeActions::class.java)

    /**
     * Wraps a synchronous node body with structured execution logs.
     *
     * @param nodeId stable graph node id
     * @param apply node body that returns channel updates
     * @return async node action used by the compiled graph
     */
    fun of(
        nodeId: String,
        apply: (AgentSessionState) -> Map<String, Any>,
    ): AsyncNodeAction<AgentSessionState> =
        AsyncNodeAction.node_async { state: AgentSessionState ->
            val startedAt = System.nanoTime()
            log.info(
                "agent.node.start nodeId={} runId={}",
                nodeId,
                state.runId(),
            )
            try {
                val updates = apply(state)
                log.info(
                    "agent.node.completed nodeId={} runId={} durationMs={}",
                    nodeId,
                    state.runId(),
                    TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startedAt),
                )
                updates
            } catch (ex: Exception) {
                log.error(
                    "agent.node.failed nodeId={} runId={} durationMs={}",
                    nodeId,
                    state.runId(),
                    TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startedAt),
                    ex,
                )
                throw ex
            }
        }
}
