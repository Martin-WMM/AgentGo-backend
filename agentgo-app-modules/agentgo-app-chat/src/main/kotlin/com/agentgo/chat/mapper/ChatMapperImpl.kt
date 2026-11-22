package com.agentgo.chat.mapper

import com.agentgo.chat.entity.ChatMessageEntity
import com.agentgo.chat.entity.ChatSessionEntity
import com.agentgo.dto.chat.ChatMessageResponse
import com.agentgo.dto.chat.ChatSessionResponse
import org.springframework.stereotype.Component

/** Default [ChatMapper] implementation used by the chat service layer. */
@Component
class ChatMapperImpl : ChatMapper {
    override fun toSessionResponse(entity: ChatSessionEntity): ChatSessionResponse =
        ChatSessionResponse(
            id = requireNotNull(entity.id) { "Persisted chat session must have an identifier." },
            title = entity.title,
            threadId = entity.threadId,
            createdAt = requireNotNull(entity.createdAt) { "Persisted chat session must have a creation timestamp." },
            updatedAt = requireNotNull(entity.updatedAt) { "Persisted chat session must have an update timestamp." },
        )

    override fun toMessageResponse(entity: ChatMessageEntity): ChatMessageResponse =
        ChatMessageResponse(
            id = requireNotNull(entity.id) { "Persisted chat message must have an identifier." },
            sessionId = entity.sessionId,
            role = entity.role,
            content = entity.content,
            plan = entity.plan,
            createdAt = requireNotNull(entity.createdAt) { "Persisted chat message must have a creation timestamp." },
        )
}
