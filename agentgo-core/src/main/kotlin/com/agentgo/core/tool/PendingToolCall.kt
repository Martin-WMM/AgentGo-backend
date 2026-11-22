package com.agentgo.core.tool

/**
 * A tool call the execution node is about to dispatch.
 *
 * @property name tool name requested by the model
 * @property arguments string arguments parsed from the model decision
 */
data class PendingToolCall(
    val name: String,
    val arguments: Map<String, String>,
)
