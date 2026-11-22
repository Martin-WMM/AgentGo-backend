package com.agentgo.core.runtime

/**
 * One transcript entry stored in the agent state.
 *
 * @property role [USER], [ASSISTANT], or [TOOL]
 * @property content message text
 */
data class AgentMessage(
    val role: String,
    val content: String,
) : java.io.Serializable {
    companion object {
        const val USER: String = "user"
        const val ASSISTANT: String = "assistant"
        const val TOOL: String = "tool"
    }
}
