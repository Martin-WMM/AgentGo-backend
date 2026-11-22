package com.agentgo.core.event

/**
 * Speaker of a streamed transcript message.
 */
enum class AgentMessageRole {
    USER,
    ASSISTANT,
    /** Incremental user-visible answer fragment from the LLM node. */
    ASSISTANT_DELTA,
    TOOL,
}
