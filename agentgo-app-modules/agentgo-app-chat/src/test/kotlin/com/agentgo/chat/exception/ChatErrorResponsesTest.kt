package com.agentgo.chat.exception

import com.agentgo.commons.dto.http.response.status.error.ChatResponseCodeRegistry
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class ChatErrorResponsesTest {
    @Test
    fun `maps chat exceptions onto the common response envelope`() {
        val response = ChatErrorResponses.of(ChatSessionNotFoundException())

        assertEquals(ChatResponseCodeRegistry.NOT_FOUND, response.code)
        assertEquals(ChatResponseCodeRegistry.NOT_FOUND_TYPE, response.responseType)
        assertFalse(response.success)
        assertFalse(response.retryable)
    }
}
