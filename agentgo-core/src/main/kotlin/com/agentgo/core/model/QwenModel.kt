package com.agentgo.core.model

/**
 * One Qwen model the AgentGo agent can select.
 *
 * Values follow the Alibaba Cloud Model Studio text-generation catalog.
 *
 * @property id DashScope model id
 * @property displayName human-readable name
 * @property contextTokens advertised context window
 * @property supportsFunctionCalling whether the model can request tools
 * @property supportsThinking whether the model exposes a thinking mode
 * @property supportsStructuredOutput whether the model can emit structured JSON
 * @property tier catalog grouping
 */
data class QwenModel(
    val id: String,
    val displayName: String,
    val contextTokens: Int,
    val supportsFunctionCalling: Boolean,
    val supportsThinking: Boolean,
    val supportsStructuredOutput: Boolean,
    val tier: QwenModelTier,
)
