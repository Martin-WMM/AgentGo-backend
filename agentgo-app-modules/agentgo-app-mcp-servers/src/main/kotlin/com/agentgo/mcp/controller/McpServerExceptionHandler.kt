package com.agentgo.mcp.controller

import com.agentgo.commons.dto.http.response.CommonHttpResponse
import com.agentgo.mcp.exception.McpErrorResponses
import com.agentgo.mcp.exception.McpException
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

/** Translates [McpException] failures into the shared HTTP response envelope. */
@RestControllerAdvice(basePackageClasses = [McpServerController::class])
class McpServerExceptionHandler {
    @ExceptionHandler(McpException::class)
    fun handleMcpException(exception: McpException): ResponseEntity<CommonHttpResponse<Nothing>> =
        ResponseEntity.status(exception.status).body(McpErrorResponses.of(exception))
}
