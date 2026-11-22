package com.agentgo.chat.service

import com.agentgo.ai.service.ChatSessionTurnWriter
import org.springframework.stereotype.Component

/**
 * Persists completed agent turns into chat message history.
 *
 * @property chatSessionService chat session application service
 */
@Component
class ChatSessionTurnWriterImpl(
    private val chatSessionService: ChatSessionService,
) : ChatSessionTurnWriter {
    override fun recordSuccessfulTurn(
        sessionId: Long,
        ownerUserId: String,
        userContent: String,
        assistantContent: String,
        plan: String?,
    ) {
        chatSessionService.appendTurn(
            sessionId = sessionId,
            ownerUserId = ownerUserId,
            userContent = userContent,
            assistantContent = assistantContent,
            plan = plan,
        )
    }
}
