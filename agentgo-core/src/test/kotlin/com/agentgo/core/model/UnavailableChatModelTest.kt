package com.agentgo.core.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class UnavailableChatModelTest {
    @Test
    fun `complete explains that the api key is missing`() {
        val error = assertFailsWith<IllegalStateException> {
            UnavailableChatModel().complete(ChatRequest(modelId = "qwen3.7-plus", messages = emptyList()))
        }

        assertEquals("Qwen API key is not configured.", error.message)
    }
}
