package com.agentgo.dto.chat

import io.swagger.v3.oas.annotations.media.Schema

/**
 * Request body used to create a chat session.
 *
 * Mutable JavaBean properties let Spring Boot 4 / Jackson 3 deserialize the body
 * without a Jackson Kotlin module.
 */
@Schema(description = "Request body for creating a chat session")
class ChatSessionCreateRequest {
    @field:Schema(
        description = "Optional session title. Blank becomes the default new-conversation title.",
        example = "Project planning",
        maxLength = 255,
    )
    var title: String = ""
}
