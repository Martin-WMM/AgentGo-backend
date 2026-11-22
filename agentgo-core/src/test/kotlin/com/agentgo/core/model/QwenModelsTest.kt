package com.agentgo.core.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class QwenModelsTest {
    @Test
    fun `default model is the balanced function calling model`() {
        assertEquals("qwen3.8-27b", QwenModels.DEFAULT_ID)
        assertEquals(QwenModels.DEFAULT_ID, QwenModels.default.id)
        assertTrue(QwenModels.default.supportsFunctionCalling)
        assertEquals(QwenModelTier.BALANCED, QwenModels.default.tier)
        assertEquals(
            "https://dashscope-intl.aliyuncs.com/compatible-mode/v1",
            QwenModels.DASHSCOPE_INTL_COMPATIBLE_BASE_URL,
        )
    }

    @Test
    fun `catalog keeps tool capable models and rejects specialized models`() {
        val capable = QwenModels.agentCapable().map { it.id }

        assertTrue(
            capable.containsAll(
                listOf("qwen4-max", "qwen3.8-flash", "qwen3.8-27b", "qwen3-coder-flash", "qwen-plus"),
            ),
        )
        assertEquals(QwenModelTier.BALANCED, QwenModels.require("qwen3.8-27b").tier)
        assertTrue("qwen-long" !in capable)
        assertTrue("qwen-mt-plus" !in capable)
        assertEquals(QwenModels.all().size, QwenModels.all().distinctBy { it.id }.size)
        assertTrue(QwenModels.all().all { it.displayName.isNotBlank() && it.contextTokens > 0 })
    }

    @Test
    fun `require rejects an unknown id and a model without function calling`() {
        assertFailsWith<IllegalArgumentException> { QwenModels.require("missing-model") }
        assertFailsWith<IllegalArgumentException> { QwenModels.requireAgentCapable("qwen-long") }
        assertEquals("qwen-long", QwenModels.find("qwen-long")?.id)
        assertEquals(null, QwenModels.find("missing-model"))
        assertEquals("qwen-flash", QwenModels.requireAgentCapable("qwen-flash").id)
    }
}
