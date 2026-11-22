package com.agentgo.core.tool

/**
 * Per-run context available to tool executors.
 *
 * Values come from the agent stream command (thread, HTTP-derived headers,
 * and caller metadata), not from model-authored tool arguments.
 *
 * @property runId active runtime run id
 * @property threadId conversation thread id
 * @property headers header parameters associated with the run
 * @property metadata caller metadata associated with the run
 */
data class ToolRuntimeContext(
    val runId: String = "",
    val threadId: String = "",
    val headers: Map<String, String> = emptyMap(),
    val metadata: Map<String, String> = emptyMap(),
) {
    companion object {
        /** Empty context used by tests and rejecting executors. */
        val EMPTY: ToolRuntimeContext = ToolRuntimeContext()
    }
}
