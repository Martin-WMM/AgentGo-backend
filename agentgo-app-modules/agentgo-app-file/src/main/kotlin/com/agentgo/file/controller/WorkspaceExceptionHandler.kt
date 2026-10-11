package com.agentgo.file.controller

import com.agentgo.commons.dto.http.response.CommonHttpResponse
import com.agentgo.file.exception.WorkspaceErrorResponses
import com.agentgo.file.exception.WorkspaceException
import com.agentgo.starter.i18n.AgentGoMessages
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

/**
 * Translates [WorkspaceException] failures into the shared HTTP response envelope.
 *
 * @property messages localized API message resolver
 */
@RestControllerAdvice(basePackageClasses = [WorkspaceController::class])
class WorkspaceExceptionHandler(
    private val messages: AgentGoMessages,
) {
    /**
     * Handles domain workspace failures raised by the file module.
     *
     * @param exception domain failure
     * @return HTTP response with the mapped business code
     */
    @ExceptionHandler(WorkspaceException::class)
    fun handleWorkspaceException(exception: WorkspaceException): ResponseEntity<CommonHttpResponse<Nothing>> =
        ResponseEntity.status(exception.status).body(WorkspaceErrorResponses.of(exception, messages))
}
