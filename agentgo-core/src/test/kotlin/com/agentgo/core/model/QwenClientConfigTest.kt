package com.agentgo.core.model

import java.time.Duration
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class QwenClientConfigTest {
    @Test
    fun `config keeps the default compatible mode endpoint`() {
        val config = QwenClientConfig(apiKey = "test-key", requestTimeout = Duration.ofSeconds(5))

        assertEquals(QwenModels.DASHSCOPE_COMPATIBLE_BASE_URL, config.baseUrl)
        assertEquals(QwenModels.DEFAULT_ID, config.modelId)
        assertEquals(0.2, config.temperature)
    }

    @Test
    fun `config accepts an http base url`() {
        val config = QwenClientConfig(apiKey = "test-key", baseUrl = "http://127.0.0.1:9/v1")

        assertEquals("http://127.0.0.1:9/v1", config.baseUrl)
    }

    @Test
    fun `config rejects a blank key model url or timeout`() {
        assertFailsWith<IllegalArgumentException> { QwenClientConfig(apiKey = " ") }
        assertFailsWith<IllegalArgumentException> { QwenClientConfig(apiKey = "test-key", modelId = " ") }
        assertFailsWith<IllegalArgumentException> { QwenClientConfig(apiKey = "test-key", baseUrl = "dashscope") }
        assertFailsWith<IllegalArgumentException> {
            QwenClientConfig(apiKey = "test-key", requestTimeout = Duration.ZERO)
        }
        assertFailsWith<IllegalArgumentException> {
            QwenClientConfig(apiKey = "test-key", requestTimeout = Duration.ofSeconds(-1))
        }
    }
}
