package com.agentgo.chat.mapper

import com.agentgo.chat.entity.ChatMessageEntity
import com.agentgo.chat.entity.ChatSessionEntity
import com.agentgo.dto.chat.ChatMessageResponse
import com.agentgo.dto.chat.ChatSessionResponse

/** Maps chat persistence entities onto API response DTOs. */
interface ChatMapper {
    /**
     * Maps a persisted session into an API response.
     *
     * @param entity persisted session
     * @return session response
     */
    fun toSessionResponse(entity: ChatSessionEntity): ChatSessionResponse

    /**
     * Maps a persisted message into an API response.
     *
     * @param entity persisted message
     * @return message response
     */
    fun toMessageResponse(entity: ChatMessageEntity): ChatMessageResponse
}
