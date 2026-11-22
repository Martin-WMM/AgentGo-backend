package com.agentgo.chat.controller

import com.agentgo.chat.exception.ChatSessionNotFoundException
import com.agentgo.chat.exception.InvalidChatRequestException
import com.agentgo.commons.dto.http.response.status.error.ChatResponseCodeRegistry
import kotlin.test.Test
import kotlin.test.assertEquals
import org.springframework.http.HttpStatus

class ChatSessionExceptionHandlerTest {
    private val handler = ChatSessionExceptionHandler()

    @Test
    fun `maps not found and invalid chat failures`() {
        val notFound = handler.handleChatException(ChatSessionNotFoundException())
        val invalid = handler.handleChatException(InvalidChatRequestException("bad title"))

        assertEquals(HttpStatus.NOT_FOUND, notFound.statusCode)
        assertEquals(ChatResponseCodeRegistry.NOT_FOUND, notFound.body?.code)
        assertEquals(HttpStatus.BAD_REQUEST, invalid.statusCode)
        assertEquals(ChatResponseCodeRegistry.INVALID, invalid.body?.code)
        assertEquals("bad title", invalid.body?.message)
    }
}
