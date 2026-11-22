package com.agentgo.chat.service

import com.agentgo.dto.chat.ChatMessageResponse
import com.agentgo.dto.chat.ChatSessionCreateRequest
import com.agentgo.dto.chat.ChatSessionResponse
import com.agentgo.dto.chat.ChatSessionUpdateRequest

/** Application service for user-owned chat sessions and messages. */
interface ChatSessionService {
    /**
     * Creates a session for the authenticated user.
     *
     * @param request optional title; blank becomes the default title
     * @return created session
     */
    fun create(request: ChatSessionCreateRequest): ChatSessionResponse

    /**
     * Returns one session owned by the authenticated user.
     *
     * @param id session identifier
     * @return session detail
     */
    fun get(id: Long): ChatSessionResponse

    /**
     * Lists sessions owned by the authenticated user, newest updates first.
     *
     * @return ordered sessions
     */
    fun list(): List<ChatSessionResponse>

    /**
     * Renames a session owned by the authenticated user.
     *
     * @param id session identifier
     * @param request new title
     * @return updated session
     */
    fun update(id: Long, request: ChatSessionUpdateRequest): ChatSessionResponse

    /**
     * Deletes a session and its messages for the authenticated user.
     *
     * @param id session identifier
     */
    fun delete(id: Long)

    /**
     * Lists messages for a session owned by the authenticated user.
     *
     * @param id session identifier
     * @return chronological messages
     */
    fun listMessages(id: Long): List<ChatMessageResponse>

    /**
     * Appends a completed user/assistant turn for an owned session.
     *
     * @param sessionId session identifier
     * @param ownerUserId authenticated owner
     * @param userContent user message text
     * @param assistantContent assistant answer text
     * @param plan optional plan text
     */
    fun appendTurn(
        sessionId: Long,
        ownerUserId: String,
        userContent: String,
        assistantContent: String,
        plan: String?,
    )
}
