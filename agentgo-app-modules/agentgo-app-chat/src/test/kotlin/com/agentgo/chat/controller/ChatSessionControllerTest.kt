package com.agentgo.chat.controller

import com.agentgo.chat.service.ChatSessionService
import com.agentgo.commons.dto.http.response.status.success.ChatResponseCodeRegistry
import com.agentgo.dto.chat.ChatMessageResponse
import com.agentgo.dto.chat.ChatSessionCreateRequest
import com.agentgo.dto.chat.ChatSessionResponse
import com.agentgo.dto.chat.ChatSessionUpdateRequest
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus

class ChatSessionControllerTest {
    private val service = mock<ChatSessionService>()
    private val controller = ChatSessionController(service)
    private val now = Instant.parse("2026-01-01T00:00:00Z")
    private val session = ChatSessionResponse(1L, "Chat", "thread", now, now)

    @Test
    fun `delegates crud operations`() {
        whenever(service.create(org.mockito.kotlin.any())).thenReturn(session)
        whenever(service.get(1L)).thenReturn(session)
        whenever(service.list()).thenReturn(listOf(session))
        whenever(service.update(org.mockito.kotlin.eq(1L), org.mockito.kotlin.any())).thenReturn(session)
        whenever(service.listMessages(1L)).thenReturn(
            listOf(ChatMessageResponse(2L, 1L, "user", "hi", null, now)),
        )

        assertEquals(ChatResponseCodeRegistry.CREATED, controller.create(ChatSessionCreateRequest()).code)
        assertEquals(ChatResponseCodeRegistry.RETRIEVED, controller.get(1L).code)
        assertEquals(ChatResponseCodeRegistry.LIST_RETRIEVED, controller.list().code)
        assertEquals(
            ChatResponseCodeRegistry.UPDATED,
            controller.update(1L, ChatSessionUpdateRequest().apply { title = "Renamed" }).code,
        )
        assertEquals(ChatResponseCodeRegistry.MESSAGES_RETRIEVED, controller.listMessages(1L).code)
        assertEquals(HttpStatus.NO_CONTENT, controller.delete(1L).statusCode)
        verify(service).delete(1L)
    }
}
