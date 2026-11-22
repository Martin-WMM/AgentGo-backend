package com.agentgo.chat.config

import com.agentgo.chat.exception.ChatSessionNotFoundException
import com.agentgo.chat.exception.InvalidChatRequestException
import com.agentgo.commons.dto.http.response.status.error.ExceptionResponseCodeRegistry
import kotlin.test.Test
import kotlin.test.assertEquals

class ChatAutoConfigurationTest {
    @Test
    fun `configuration registers chat exception codes`() {
        ChatAutoConfiguration()

        assertEquals(10101, ExceptionResponseCodeRegistry.responseCode(ChatSessionNotFoundException::class)?.code)
        assertEquals(
            "CHAT-002",
            ExceptionResponseCodeRegistry.responseCode(InvalidChatRequestException::class)?.responseType,
        )
    }
}
