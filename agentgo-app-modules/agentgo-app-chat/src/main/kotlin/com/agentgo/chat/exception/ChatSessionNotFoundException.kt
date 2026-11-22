package com.agentgo.chat.exception

import com.agentgo.commons.dto.http.response.status.error.ChatResponseCodeRegistry
import org.springframework.http.HttpStatus

/** Raised when a chat session id is unknown for the authenticated owner. */
class ChatSessionNotFoundException : ChatException(
    code = ChatResponseCodeRegistry.NOT_FOUND,
    responseType = ChatResponseCodeRegistry.NOT_FOUND_TYPE,
    status = HttpStatus.NOT_FOUND,
    message = "The requested chat session was not found.",
)
