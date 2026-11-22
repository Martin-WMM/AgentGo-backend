package com.agentgo.core.runtime

import com.agentgo.core.model.QwenModels
import com.agentgo.core.tool.ToolSpec

/**
 * Limits for one AgentGo agent run.
 *
 * @property modelId Qwen model id stored on the run
 * @property maxSteps maximum tool executions before the loop stops
 * @property tools tools the model may request
 */
data class AgentRunOptions(
    val modelId: String = QwenModels.DEFAULT_ID,
    val maxSteps: Int = 8,
    val tools: List<ToolSpec> = emptyList(),
) {
    init {
        require(modelId.isNotBlank()) { "Model id must not be blank." }
        require(maxSteps >= 0) { "Max steps must be zero or greater." }
        require(tools.map { it.name }.distinct().size == tools.size) {
            "Tool names must be unique."
        }
    }
}
