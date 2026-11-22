package com.agentgo.chat.mapper

import com.agentgo.chat.entity.ChatMessageEntity
import com.agentgo.chat.entity.ChatSessionEntity
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class ChatMapperImplTest {
    private val mapper = ChatMapperImpl()

    @Test
    fun `maps session and message entities`() {
        val created = Instant.parse("2026-01-01T00:00:00Z")
        val session = ChatSessionEntity(
            id = 1L,
            ownerUserId = "ada",
            title = "Hello",
            threadId = "thread-1",
            createdAt = created,
            updatedAt = created,
        )
        val message = ChatMessageEntity(
            id = 2L,
            sessionId = 1L,
            role = "assistant",
            content = "hi",
            plan = "1. Reply",
            createdAt = created,
        )

        val sessionResponse = mapper.toSessionResponse(session)
        val messageResponse = mapper.toMessageResponse(message)

        assertEquals(1L, sessionResponse.id)
        assertEquals("Hello", sessionResponse.title)
        assertEquals("thread-1", sessionResponse.threadId)
        assertEquals(2L, messageResponse.id)
        assertEquals("1. Reply", messageResponse.plan)
    }

    @Test
    fun `rejects incomplete entities`() {
        assertFailsWith<IllegalArgumentException> {
            mapper.toSessionResponse(ChatSessionEntity(title = "x", threadId = "t"))
        }
        assertFailsWith<IllegalArgumentException> {
            mapper.toMessageResponse(ChatMessageEntity(sessionId = 1L, role = "user", content = "x"))
        }
        assertNull(
            runCatching {
                mapper.toMessageResponse(
                    ChatMessageEntity(
                        id = 1L,
                        sessionId = 1L,
                        role = "user",
                        content = "x",
                        createdAt = Instant.parse("2026-01-01T00:00:00Z"),
                    ),
                ).plan
            }.getOrNull(),
        )
    }
}
