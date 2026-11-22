package com.agentgo.chat.service.impl

import com.agentgo.chat.entity.ChatMessageEntity
import com.agentgo.chat.entity.ChatSessionEntity
import com.agentgo.chat.exception.ChatSessionNotFoundException
import com.agentgo.chat.exception.InvalidChatRequestException
import com.agentgo.chat.mapper.ChatMapper
import com.agentgo.chat.repository.ChatMessageRepository
import com.agentgo.chat.repository.ChatSessionRepository
import com.agentgo.chat.security.AuthenticatedUserIdResolver
import com.agentgo.dto.chat.ChatMessageResponse
import com.agentgo.dto.chat.ChatSessionCreateRequest
import com.agentgo.dto.chat.ChatSessionResponse
import com.agentgo.dto.chat.ChatSessionUpdateRequest
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.TransactionDefinition
import org.springframework.transaction.TransactionStatus
import org.springframework.transaction.support.SimpleTransactionStatus

class ChatSessionServiceImplTest {
    private val sessionRepository = mock<ChatSessionRepository>()
    private val messageRepository = mock<ChatMessageRepository>()
    private val chatMapper = mock<ChatMapper>()
    private val authenticatedUserIdResolver = mock<AuthenticatedUserIdResolver>()
    private val service = ChatSessionServiceImpl(
        sessionRepository,
        messageRepository,
        chatMapper,
        authenticatedUserIdResolver,
        PassThroughTransactionManager(),
    )

    @Test
    fun `create mints a thread and default title`() {
        whenever(authenticatedUserIdResolver.requireUserId()).thenReturn("ada")
        whenever(sessionRepository.save(any())).thenAnswer { invocation ->
            val entity = invocation.getArgument<ChatSessionEntity>(0)
            entity.id = 1L
            entity.createdAt = Instant.parse("2026-01-01T00:00:00Z")
            entity.updatedAt = entity.createdAt
            entity
        }
        whenever(chatMapper.toSessionResponse(any())).thenAnswer { invocation ->
            val entity = invocation.getArgument<ChatSessionEntity>(0)
            ChatSessionResponse(
                id = entity.id!!,
                title = entity.title,
                threadId = entity.threadId,
                createdAt = entity.createdAt!!,
                updatedAt = entity.updatedAt!!,
            )
        }

        val response = service.create(ChatSessionCreateRequest())

        assertEquals("New conversation", response.title)
        assertTrue(response.threadId.isNotBlank())
        verify(sessionRepository).save(any())
    }

    @Test
    fun `get rejects another owners session`() {
        whenever(authenticatedUserIdResolver.requireUserId()).thenReturn("ada")
        whenever(sessionRepository.findByIdAndOwnerUserId(9L, "ada")).thenReturn(null)

        assertFailsWith<ChatSessionNotFoundException> { service.get(9L) }
    }

    @Test
    fun `update rejects a blank title`() {
        whenever(authenticatedUserIdResolver.requireUserId()).thenReturn("ada")

        assertFailsWith<InvalidChatRequestException> {
            service.update(1L, ChatSessionUpdateRequest().apply { title = "  " })
        }
        verify(sessionRepository, never()).save(any())
    }

    @Test
    fun `appendTurn writes user and assistant messages for the owner`() {
        val session = ChatSessionEntity(
            id = 4L,
            ownerUserId = "ada",
            title = "Chat",
            threadId = "thread-4",
            createdAt = Instant.parse("2026-01-01T00:00:00Z"),
            updatedAt = Instant.parse("2026-01-01T00:00:00Z"),
        )
        whenever(sessionRepository.findByIdAndOwnerUserId(4L, "ada")).thenReturn(session)
        whenever(messageRepository.save(any())).thenAnswer { it.getArgument(0) }
        whenever(sessionRepository.save(session)).thenReturn(session)

        service.appendTurn(4L, "ada", "hello", "world", "1. Reply")

        val captor = argumentCaptor<ChatMessageEntity>()
        verify(messageRepository, org.mockito.kotlin.times(2)).save(captor.capture())
        assertEquals(listOf("user", "assistant"), captor.allValues.map { it.role })
        assertEquals("hello", captor.firstValue.content)
        assertEquals("world", captor.secondValue.content)
        assertEquals("1. Reply", captor.secondValue.plan)
    }

    @Test
    fun `listMessages requires ownership`() {
        whenever(authenticatedUserIdResolver.requireUserId()).thenReturn("ada")
        whenever(sessionRepository.findByIdAndOwnerUserId(5L, "ada")).thenReturn(null)

        assertFailsWith<ChatSessionNotFoundException> { service.listMessages(5L) }
        verify(messageRepository, never()).findBySessionIdOrderByCreatedAtAsc(any())
    }

    @Test
    fun `listMessages returns chronological history`() {
        val session = ChatSessionEntity(id = 5L, ownerUserId = "ada", title = "Chat", threadId = "t")
        val message = ChatMessageEntity(
            id = 1L,
            sessionId = 5L,
            role = "user",
            content = "hi",
            createdAt = Instant.parse("2026-01-01T00:00:00Z"),
        )
        val response = ChatMessageResponse(
            id = 1L,
            sessionId = 5L,
            role = "user",
            content = "hi",
            createdAt = message.createdAt!!,
        )
        whenever(authenticatedUserIdResolver.requireUserId()).thenReturn("ada")
        whenever(sessionRepository.findByIdAndOwnerUserId(5L, "ada")).thenReturn(session)
        whenever(messageRepository.findBySessionIdOrderByCreatedAtAsc(5L)).thenReturn(listOf(message))
        whenever(chatMapper.toMessageResponse(message)).thenReturn(response)

        assertEquals(listOf(response), service.listMessages(5L))
    }

    @Test
    fun `list update and delete cover ownership paths`() {
        val now = Instant.parse("2026-01-01T00:00:00Z")
        val entity = ChatSessionEntity(
            id = 8L,
            ownerUserId = "ada",
            title = "Old",
            threadId = "thread-8",
            createdAt = now,
            updatedAt = now,
        )
        val renamed = ChatSessionResponse(8L, "Renamed", "thread-8", now, now)
        whenever(authenticatedUserIdResolver.requireUserId()).thenReturn("ada")
        whenever(sessionRepository.findByOwnerUserIdOrderByUpdatedAtDesc("ada")).thenReturn(listOf(entity))
        whenever(chatMapper.toSessionResponse(entity)).thenReturn(
            ChatSessionResponse(8L, "Old", "thread-8", now, now),
        )
        whenever(sessionRepository.findByIdAndOwnerUserId(8L, "ada")).thenReturn(entity)
        whenever(sessionRepository.save(entity)).thenReturn(entity)
        whenever(chatMapper.toSessionResponse(entity)).thenReturn(renamed)

        assertEquals(listOf(renamed), service.list())
        assertEquals(renamed, service.update(8L, ChatSessionUpdateRequest().apply { title = "Renamed" }))
        service.delete(8L)
        verify(messageRepository).deleteBySessionId(8L)
        verify(sessionRepository).delete(entity)
    }

    @Test
    fun `create rejects an oversized title`() {
        whenever(authenticatedUserIdResolver.requireUserId()).thenReturn("ada")

        assertFailsWith<InvalidChatRequestException> {
            service.create(ChatSessionCreateRequest().apply { title = "x".repeat(256) })
        }
    }

    @Test
    fun `appendTurn rejects another owners session`() {
        whenever(sessionRepository.findByIdAndOwnerUserId(4L, "ada")).thenReturn(null)

        assertFailsWith<ChatSessionNotFoundException> {
            service.appendTurn(4L, "ada", "hi", "hello", null)
        }
    }

    private class PassThroughTransactionManager : PlatformTransactionManager {
        override fun getTransaction(definition: TransactionDefinition?): TransactionStatus =
            SimpleTransactionStatus()

        override fun commit(status: TransactionStatus) = Unit

        override fun rollback(status: TransactionStatus) = Unit
    }
}
