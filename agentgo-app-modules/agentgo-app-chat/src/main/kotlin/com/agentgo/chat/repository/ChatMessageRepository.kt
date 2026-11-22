package com.agentgo.chat.repository

import com.agentgo.chat.entity.ChatMessageEntity
import org.springframework.data.jpa.repository.JpaRepository

/**
 * Persistence access for [ChatMessageEntity] rows.
 */
interface ChatMessageRepository : JpaRepository<ChatMessageEntity, Long> {
    /**
     * Lists messages for [sessionId] in chronological order.
     *
     * @param sessionId owning session identifier
     * @return ordered message entities
     */
    fun findBySessionIdOrderByCreatedAtAsc(sessionId: Long): List<ChatMessageEntity>

    /**
     * Deletes every message belonging to [sessionId].
     *
     * @param sessionId owning session identifier
     */
    fun deleteBySessionId(sessionId: Long)
}
