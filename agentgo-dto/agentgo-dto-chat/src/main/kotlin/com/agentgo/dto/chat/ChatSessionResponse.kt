package com.agentgo.dto.chat

import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant

/**
 * API representation of one user-owned chat session.
 */
@Schema(description = "Chat session returned by chatSessions APIs")
data class ChatSessionResponse(
    @field:Schema(description = "Persistent session identifier", format = "int64", requiredMode = Schema.RequiredMode.REQUIRED)
    val id: Long,
    @field:Schema(description = "Session title", example = "New conversation", requiredMode = Schema.RequiredMode.REQUIRED)
    val title: String,
    @field:Schema(description = "LangGraph thread id used by the agent stream", requiredMode = Schema.RequiredMode.REQUIRED)
    val threadId: String,
    @field:Schema(description = "Creation timestamp", format = "date-time", requiredMode = Schema.RequiredMode.REQUIRED)
    val createdAt: Instant,
    @field:Schema(description = "Last update timestamp", format = "date-time", requiredMode = Schema.RequiredMode.REQUIRED)
    val updatedAt: Instant,
)
