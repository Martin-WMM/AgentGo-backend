package com.agentgo.a2a.controller

import com.agentgo.a2a.exception.A2aErrorResponses
import com.agentgo.a2a.exception.A2aException
import com.agentgo.commons.dto.http.response.CommonHttpResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

/** Translates [A2aException] failures into the shared HTTP response envelope. */
@RestControllerAdvice(basePackageClasses = [A2aAgentController::class])
class A2aAgentExceptionHandler {
    /**
     * Maps a domain failure onto the standard response envelope.
     *
     * @param exception domain failure
     * @return HTTP response whose status matches the exception
     */
    @ExceptionHandler(A2aException::class)
    fun handleA2aException(exception: A2aException): ResponseEntity<CommonHttpResponse<Nothing>> =
        ResponseEntity.status(exception.status).body(A2aErrorResponses.of(exception))
}
