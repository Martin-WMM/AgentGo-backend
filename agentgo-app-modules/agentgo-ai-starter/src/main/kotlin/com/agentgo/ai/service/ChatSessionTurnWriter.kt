package com.agentgo.ai.service

/**
 * Optional hook that persists a completed agent turn into chat history.
 *
 * Implementations live in the chat module. The agent stream calls this only when
 * `metadata.sessionId` is present and the run finishes successfully.
 */
interface ChatSessionTurnWriter {
    /**
     * Stores the user prompt and the assistant answer for one successful turn.
     *
     * @param sessionId chat session identifier from request metadata
     * @param ownerUserId authenticated owner that started the stream
     * @param userContent user input for this turn
     * @param assistantContent final assistant answer
     * @param plan optional plan text produced during the turn
     */
    fun recordSuccessfulTurn(
        sessionId: Long,
        ownerUserId: String,
        userContent: String,
        assistantContent: String,
        plan: String?,
    )
}
