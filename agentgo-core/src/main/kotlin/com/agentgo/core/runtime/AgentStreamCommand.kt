package com.agentgo.core.runtime

/**
 * Inputs for one streamed agent run.
 *
 * @property input raw user text
 * @property modelId model id written into the session
 * @property threadId conversation thread required by the caller
 * @property headers header parameters supplied with the run
 * @property metadata caller metadata supplied with the run
 */
data class AgentStreamCommand(
    val input: String,
    val modelId: String,
    val threadId: String,
    val headers: Map<String, String>,
    val metadata: Map<String, String>,
)
