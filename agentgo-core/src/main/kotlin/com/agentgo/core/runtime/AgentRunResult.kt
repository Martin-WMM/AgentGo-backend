package com.agentgo.core.runtime

/**
 * Outcome of one agent graph run.
 *
 * @property output user-facing text
 * @property plan structured plan JSON, empty when planning did not run
 * @property trace node ids in visit order
 * @property stepCount tool executions performed
 * @property modelId model id used for the run
 */
data class AgentRunResult(
    val output: String,
    val plan: String,
    val trace: List<String>,
    val stepCount: Int,
    val modelId: String,
)
