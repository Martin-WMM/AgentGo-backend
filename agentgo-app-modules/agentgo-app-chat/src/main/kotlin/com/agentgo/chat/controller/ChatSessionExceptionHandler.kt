package com.agentgo.chat.controller

import com.agentgo.chat.exception.ChatErrorResponses
import com.agentgo.chat.exception.ChatException
import com.agentgo.commons.dto.http.response.CommonHttpResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

/**
 * Translates [ChatException] failures into the shared HTTP response envelope.
 */
@RestControllerAdvice(basePackageClasses = [ChatSessionController::class])
class ChatSessionExceptionHandler {
    /**
     * Handles domain chat failures raised by the chat module.
     *
     * @param exception domain failure
     * @return HTTP response with the mapped business code
     */
    @ExceptionHandler(ChatException::class)
    fun handleChatException(exception: ChatException): ResponseEntity<CommonHttpResponse<Nothing>> =
        ResponseEntity.status(exception.status).body(ChatErrorResponses.of(exception))
}
