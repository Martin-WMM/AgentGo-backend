package com.agentgo.dto.chat

import io.swagger.v3.oas.annotations.media.Schema

/**
 * Request body used to rename a chat session.
 */
@Schema(description = "Request body for renaming a chat session")
class ChatSessionUpdateRequest {
    @field:Schema(
        description = "New session title",
        example = "Sprint review notes",
        requiredMode = Schema.RequiredMode.REQUIRED,
        maxLength = 255,
    )
    var title: String = ""
}
