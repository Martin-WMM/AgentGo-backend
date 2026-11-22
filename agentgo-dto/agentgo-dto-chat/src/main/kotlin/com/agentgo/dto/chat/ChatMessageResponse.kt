package com.agentgo.dto.chat

import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant

/**
 * API representation of one persisted chat message.
 */
@Schema(description = "Chat message returned by chatSessions message APIs")
data class ChatMessageResponse(
    @field:Schema(description = "Persistent message identifier", format = "int64", requiredMode = Schema.RequiredMode.REQUIRED)
    val id: Long,
    @field:Schema(description = "Owning session identifier", format = "int64", requiredMode = Schema.RequiredMode.REQUIRED)
    val sessionId: Long,
    @field:Schema(description = "Message role: user, assistant, or system", example = "user", requiredMode = Schema.RequiredMode.REQUIRED)
    val role: String,
    @field:Schema(description = "Message text shown to the user", requiredMode = Schema.RequiredMode.REQUIRED)
    val content: String,
    @field:Schema(description = "Optional plan text stored with an assistant turn")
    val plan: String? = null,
    @field:Schema(description = "Creation timestamp", format = "date-time", requiredMode = Schema.RequiredMode.REQUIRED)
    val createdAt: Instant,
)
