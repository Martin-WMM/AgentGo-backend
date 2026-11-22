package com.agentgo.cli.controller

import com.agentgo.cli.exception.CliErrorResponses
import com.agentgo.cli.exception.CliException
import com.agentgo.commons.dto.http.response.CommonHttpResponse
import com.agentgo.starter.i18n.AgentGoMessages
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

/**
 * Translates [CliException] failures into the shared HTTP response envelope.
 *
 * @property messages localized API message resolver
 */
@RestControllerAdvice(basePackageClasses = [CliExecutionController::class])
class CliExceptionHandler(
    private val messages: AgentGoMessages,
) {
    /**
     * Handles domain CLI failures raised by the CLI module.
     *
     * @param exception domain failure
     * @return HTTP response with the mapped business code
     */
    @ExceptionHandler(CliException::class)
    fun handleCliException(exception: CliException): ResponseEntity<CommonHttpResponse<Nothing>> =
        ResponseEntity.status(exception.status).body(CliErrorResponses.of(exception, messages))
}