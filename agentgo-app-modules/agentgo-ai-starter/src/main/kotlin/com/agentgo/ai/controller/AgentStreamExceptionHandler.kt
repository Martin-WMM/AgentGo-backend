package com.agentgo.ai.controller

import com.agentgo.ai.exception.AgentStreamErrorResponses
import com.agentgo.ai.exception.AgentStreamException
import com.agentgo.commons.dto.http.response.CommonHttpResponse
import com.agentgo.starter.i18n.AgentGoMessages
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

/**
 * Translates [AgentStreamException] failures into the shared HTTP response envelope.
 *
 * @property messages localized API message resolver
 */
@RestControllerAdvice(basePackageClasses = [AgentStreamController::class])
class AgentStreamExceptionHandler(
    private val messages: AgentGoMessages,
) {
    /**
     * Handles domain agent-stream failures raised before the SSE response starts.
     *
     * @param exception domain failure
     * @return HTTP response with the mapped business code
     */
    @ExceptionHandler(AgentStreamException::class)
    fun handleAgentStreamException(exception: AgentStreamException): ResponseEntity<CommonHttpResponse<Nothing>> =
        ResponseEntity.status(exception.status).body(AgentStreamErrorResponses.of(exception, messages))
}
