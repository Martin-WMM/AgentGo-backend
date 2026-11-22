package com.agentgo.ai.properties

import com.agentgo.core.model.QwenModels
import java.time.Duration
import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * Settings for the agent SSE entry.
 *
 * @property modelId Qwen model id used for planning and decisions
 * @property qwenApiKey DashScope API key; blank keeps the process up and fails model calls
 * @property qwenBaseUrl DashScope compatible-mode root
 * @property timeout maximum time one SSE response may stay open
 * @property poolSize number of stream worker threads
 * @property queueCapacity number of stream tasks that may wait for a worker
 */
@ConfigurationProperties(prefix = "agentgo.agent")
class AgentStreamProperties {
    var modelId: String = QwenModels.DEFAULT_ID
    var qwenApiKey: String = ""
    var qwenBaseUrl: String = QwenModels.DASHSCOPE_COMPATIBLE_BASE_URL
    var timeout: Duration = Duration.ofMinutes(5)
    var poolSize: Int = 4
    var queueCapacity: Int = 32
}
