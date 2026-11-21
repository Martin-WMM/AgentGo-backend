package com.agentgo.events.controller

import com.agentgo.commons.dto.http.response.CommonHttpResponse
import com.agentgo.events.exception.SystemEventErrorResponses
import com.agentgo.events.exception.SystemEventException
import com.agentgo.starter.i18n.AgentGoMessages
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

/**
 * Translates [SystemEventException] failures into the shared HTTP response envelope.
 *
 * @property messages localized API message resolver
 */
@RestControllerAdvice(basePackageClasses = [SystemEventController::class])
class SystemEventExceptionHandler(
    private val messages: AgentGoMessages,
) {
    /**
     * Handles domain system-event failures raised by this module.
     *
     * @param exception domain failure
     * @return HTTP response with the mapped business code
     */
    @ExceptionHandler(SystemEventException::class)
    fun handleSystemEventException(
        exception: SystemEventException,
    ): ResponseEntity<CommonHttpResponse<Nothing>> =
        ResponseEntity.status(exception.status).body(SystemEventErrorResponses.of(exception, messages))
}
