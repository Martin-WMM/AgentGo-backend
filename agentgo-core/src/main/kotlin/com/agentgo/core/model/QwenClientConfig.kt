package com.agentgo.core.model

import java.time.Duration

/**
 * Connection settings for the DashScope compatible-mode API.
 *
 * @property apiKey DashScope API key sent as a bearer token
 * @property modelId default Qwen model id
 * @property baseUrl compatible-mode root, without a trailing path segment
 * @property temperature default sampling temperature
 * @property requestTimeout overall HTTP call timeout
 */
data class QwenClientConfig(
    val apiKey: String,
    val modelId: String = QwenModels.DEFAULT_ID,
    val baseUrl: String = QwenModels.DASHSCOPE_COMPATIBLE_BASE_URL,
    val temperature: Double = 0.2,
    val requestTimeout: Duration = Duration.ofSeconds(60),
) {
    init {
        require(apiKey.isNotBlank()) { "Qwen API key must not be blank." }
        require(modelId.isNotBlank()) { "Qwen model id must not be blank." }
        require(baseUrl.startsWith("https://") || baseUrl.startsWith("http://")) {
            "Qwen base URL must be an absolute HTTP(S) URL."
        }
        require(!requestTimeout.isNegative && !requestTimeout.isZero) {
            "Qwen request timeout must be positive."
        }
    }
}
