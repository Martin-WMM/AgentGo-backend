package com.agentgo.chat.service.impl

import com.agentgo.chat.entity.ChatMessageEntity
import com.agentgo.chat.entity.ChatSessionEntity
import com.agentgo.chat.exception.ChatSessionNotFoundException
import com.agentgo.chat.exception.InvalidChatRequestException
import com.agentgo.chat.mapper.ChatMapper
import com.agentgo.chat.repository.ChatMessageRepository
import com.agentgo.chat.repository.ChatSessionRepository
import com.agentgo.chat.security.AuthenticatedUserIdResolver
import com.agentgo.chat.service.ChatSessionService
import com.agentgo.dto.chat.ChatMessageResponse
import com.agentgo.dto.chat.ChatSessionCreateRequest
import com.agentgo.dto.chat.ChatSessionResponse
import com.agentgo.dto.chat.ChatSessionUpdateRequest
import com.agentgo.events.annotation.RecordSystemEvent
import com.agentgo.events.domain.SystemEventDomains
import java.util.UUID
import org.springframework.stereotype.Service
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate

/**
 * Default [ChatSessionService] implementation backed by JPA persistence.
 *
 * @property sessionRepository session persistence access
 * @property messageRepository message persistence access
 * @property chatMapper entity/DTO mapper
 * @property authenticatedUserIdResolver security-context owner resolver
 */
@Service
class ChatSessionServiceImpl(
    private val sessionRepository: ChatSessionRepository,
    private val messageRepository: ChatMessageRepository,
    private val chatMapper: ChatMapper,
    private val authenticatedUserIdResolver: AuthenticatedUserIdResolver,
    transactionManager: PlatformTransactionManager,
) : ChatSessionService {
    private val writeTransaction = TransactionTemplate(transactionManager)
    private val readTransaction = TransactionTemplate(transactionManager).apply {
        isReadOnly = true
    }

    @RecordSystemEvent(
        domain = SystemEventDomains.CHAT,
        name = "Chat session created",
        comment = "Created chat session #{#result.title} (#{#result.id})",
    )
    override fun create(request: ChatSessionCreateRequest): ChatSessionResponse {
        val ownerUserId = authenticatedUserIdResolver.requireUserId()
        val title = normalizeTitle(request.title, allowBlank = true)
        return write {
            val saved = sessionRepository.save(
                ChatSessionEntity(
                    ownerUserId = ownerUserId,
                    title = title,
                    threadId = UUID.randomUUID().toString(),
                ),
            )
            sessionRepository.flush()
            chatMapper.toSessionResponse(saved)
        }
    }

    override fun get(id: Long): ChatSessionResponse {
        val ownerUserId = authenticatedUserIdResolver.requireUserId()
        return read {
            chatMapper.toSessionResponse(requireOwnedSession(id, ownerUserId))
        }
    }

    override fun list(): List<ChatSessionResponse> {
        val ownerUserId = authenticatedUserIdResolver.requireUserId()
        return read {
            sessionRepository.findByOwnerUserIdOrderByUpdatedAtDesc(ownerUserId)
                .map(chatMapper::toSessionResponse)
        }
    }

    @RecordSystemEvent(
        domain = SystemEventDomains.CHAT,
        name = "Chat session updated",
        comment = "Renamed chat session #{#id}",
    )
    override fun update(id: Long, request: ChatSessionUpdateRequest): ChatSessionResponse {
        val ownerUserId = authenticatedUserIdResolver.requireUserId()
        val title = normalizeTitle(request.title, allowBlank = false)
        return write {
            val entity = requireOwnedSession(id, ownerUserId)
            entity.title = title
            val saved = sessionRepository.save(entity)
            sessionRepository.flush()
            chatMapper.toSessionResponse(saved)
        }
    }

    @RecordSystemEvent(
        domain = SystemEventDomains.CHAT,
        name = "Chat session deleted",
        comment = "Deleted chat session #{#id}",
    )
    override fun delete(id: Long) {
        val ownerUserId = authenticatedUserIdResolver.requireUserId()
        write {
            val entity = requireOwnedSession(id, ownerUserId)
            messageRepository.deleteBySessionId(requireNotNull(entity.id))
            sessionRepository.delete(entity)
        }
    }

    override fun listMessages(id: Long): List<ChatMessageResponse> {
        val ownerUserId = authenticatedUserIdResolver.requireUserId()
        return read {
            val session = requireOwnedSession(id, ownerUserId)
            messageRepository.findBySessionIdOrderByCreatedAtAsc(requireNotNull(session.id))
                .map(chatMapper::toMessageResponse)
        }
    }

    override fun appendTurn(
        sessionId: Long,
        ownerUserId: String,
        userContent: String,
        assistantContent: String,
        plan: String?,
    ) {
        write {
            val session = requireOwnedSession(sessionId, ownerUserId)
            val id = requireNotNull(session.id)
            messageRepository.save(
                ChatMessageEntity(
                    sessionId = id,
                    role = ROLE_USER,
                    content = userContent,
                ),
            )
            messageRepository.save(
                ChatMessageEntity(
                    sessionId = id,
                    role = ROLE_ASSISTANT,
                    content = assistantContent,
                    plan = plan?.takeIf { it.isNotBlank() },
                ),
            )
            sessionRepository.save(session)
            sessionRepository.flush()
        }
    }

    private fun requireOwnedSession(id: Long, ownerUserId: String): ChatSessionEntity =
        sessionRepository.findByIdAndOwnerUserId(id, ownerUserId) ?: throw ChatSessionNotFoundException()

    private fun normalizeTitle(value: String, allowBlank: Boolean): String {
        val trimmed = value.trim()
        if (trimmed.isEmpty()) {
            if (allowBlank) {
                return DEFAULT_TITLE
            }
            throw InvalidChatRequestException("The chat session title must not be blank.")
        }
        if (trimmed.length > 255) {
            throw InvalidChatRequestException("The chat session title must be at most 255 characters.")
        }
        return trimmed
    }

    private fun <T> read(action: () -> T): T =
        readTransaction.execute { action() } ?: error("Chat read transaction returned null")

    private fun <T> write(action: () -> T): T =
        writeTransaction.execute { action() } ?: error("Chat write transaction returned null")

    companion object {
        const val DEFAULT_TITLE: String = "New conversation"
        const val ROLE_USER: String = "user"
        const val ROLE_ASSISTANT: String = "assistant"
    }
}
