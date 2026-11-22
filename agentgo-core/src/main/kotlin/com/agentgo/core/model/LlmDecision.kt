package com.agentgo.core.model

/**
 * Decision parsed from an LLM node reply.
 */
sealed class LlmDecision {
    /**
     * The model finished the task.
     *
     * @property content user-facing answer
     */
    data class Final(val content: String) : LlmDecision()

    /**
     * The model asked the execution node to run a tool.
     *
     * @property name tool name
     * @property arguments string arguments
     */
    data class ToolCall(
        val name: String,
        val arguments: Map<String, String>,
    ) : LlmDecision()
}
