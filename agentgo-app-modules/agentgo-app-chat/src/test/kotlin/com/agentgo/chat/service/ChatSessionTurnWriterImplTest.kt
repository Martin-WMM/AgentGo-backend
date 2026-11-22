package com.agentgo.chat.service

import kotlin.test.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class ChatSessionTurnWriterImplTest {
    @Test
    fun `delegates turn persistence to the chat session service`() {
        val chatSessionService = mock<ChatSessionService>()
        val writer = ChatSessionTurnWriterImpl(chatSessionService)

        writer.recordSuccessfulTurn(3L, "ada", "hi", "hello", "plan")

        verify(chatSessionService).appendTurn(3L, "ada", "hi", "hello", "plan")
    }
}
