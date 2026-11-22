package com.agentgo.dto.chat

import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ChatDtoTest {
    @Test
    fun `create request defaults title to blank and stores a client value`() {
        val empty = ChatSessionCreateRequest()
        assertEquals("", empty.title)

        val request = ChatSessionCreateRequest().apply {
            title = "Project planning"
        }

        assertEquals("Project planning", request.title)
    }

    @Test
    fun `update request defaults title to blank and stores a rename`() {
        val empty = ChatSessionUpdateRequest()
        assertEquals("", empty.title)

        val request = ChatSessionUpdateRequest().apply {
            title = "Sprint review notes"
        }

        assertEquals("Sprint review notes", request.title)
    }

    @Test
    fun `session response keeps persistence fields and generated members`() {
        val createdAt = Instant.parse("2026-11-22T08:00:00Z")
        val updatedAt = Instant.parse("2026-11-22T09:00:00Z")
        val response = ChatSessionResponse(
            id = 1L,
            title = "New conversation",
            threadId = "thread-1",
            createdAt = createdAt,
            updatedAt = updatedAt,
        )

        assertEquals(1L, response.id)
        assertEquals("New conversation", response.title)
        assertEquals("thread-1", response.threadId)
        assertEquals(createdAt, response.createdAt)
        assertEquals(updatedAt, response.updatedAt)
        assertEquals(response, response.copy())
        assertEquals(response.hashCode(), response.copy().hashCode())
        assertNotEquals(response, response.copy(title = "Renamed"))
        assertNotEquals(response.hashCode(), response.copy(threadId = "other").hashCode())
        assertTrue(response.toString().contains("thread-1"))
    }

    @Test
    fun `message response keeps turn fields and optional plan`() {
        val createdAt = Instant.parse("2026-11-22T08:05:00Z")
        val response = ChatMessageResponse(
            id = 10L,
            sessionId = 1L,
            role = "assistant",
            content = "Here is a summary.",
            plan = "1. Gather context\n2. Reply",
            createdAt = createdAt,
        )

        assertEquals(10L, response.id)
        assertEquals(1L, response.sessionId)
        assertEquals("assistant", response.role)
        assertEquals("Here is a summary.", response.content)
        assertEquals("1. Gather context\n2. Reply", response.plan)
        assertEquals(createdAt, response.createdAt)
        assertEquals(response, response.copy())
        assertNull(response.copy(plan = null).plan)
        assertNotEquals(response, response.copy(role = "user"))
        assertNotEquals(response.hashCode(), response.copy(content = "other").hashCode())
        assertTrue(response.toString().contains("assistant"))
    }
}
