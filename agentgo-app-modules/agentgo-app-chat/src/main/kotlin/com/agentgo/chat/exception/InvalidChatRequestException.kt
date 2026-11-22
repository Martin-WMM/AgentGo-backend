package com.agentgo.chat.exception

import com.agentgo.commons.dto.http.response.status.error.ChatResponseCodeRegistry
import org.springframework.http.HttpStatus

/**
 * Raised when create or update payloads fail domain validation.
 *
 * @param message safe client-facing validation message
 */
class InvalidChatRequestException(
    message: String = "The chat session request is invalid.",
) : ChatException(
    code = ChatResponseCodeRegistry.INVALID,
    responseType = ChatResponseCodeRegistry.INVALID_TYPE,
    status = HttpStatus.BAD_REQUEST,
    message = message,
)
