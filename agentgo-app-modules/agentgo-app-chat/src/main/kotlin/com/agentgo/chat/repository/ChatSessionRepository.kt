package com.agentgo.chat.repository

import com.agentgo.chat.entity.ChatSessionEntity
import org.springframework.data.jpa.repository.JpaRepository

/**
 * Persistence access for [ChatSessionEntity] rows.
 *
 * Query methods require an owner identifier so callers cannot load another user's
 * sessions by id alone.
 */
interface ChatSessionRepository : JpaRepository<ChatSessionEntity, Long> {
    /**
     * Finds a session owned by [ownerUserId].
     *
     * @param id session identifier
     * @param ownerUserId authenticated owner
     * @return matching entity, or `null` when absent
     */
    fun findByIdAndOwnerUserId(id: Long, ownerUserId: String): ChatSessionEntity?

    /**
     * Lists every session owned by [ownerUserId], newest updates first.
     *
     * @param ownerUserId authenticated owner
     * @return ordered session entities
     */
    fun findByOwnerUserIdOrderByUpdatedAtDesc(ownerUserId: String): List<ChatSessionEntity>
}
