package com.agentgo.prompts.controller

import com.agentgo.commons.dto.http.response.CommonHttpResponse
import com.agentgo.prompts.exception.PromptTemplateErrorResponses
import com.agentgo.prompts.exception.PromptTemplateException
import com.agentgo.starter.i18n.AgentGoMessages
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

/**
 * Translates [PromptTemplateException] failures into the shared HTTP response envelope.
 *
 * @property messages localized API message resolver
 */
@RestControllerAdvice(basePackageClasses = [PromptTemplateController::class])
class PromptTemplateExceptionHandler(
    private val messages: AgentGoMessages,
) {
    /**
     * Handles domain prompt-template failures raised by this module.
     *
     * @param exception domain failure
     * @return HTTP response with the mapped business code
     */
    @ExceptionHandler(PromptTemplateException::class)
    fun handlePromptTemplateException(
        exception: PromptTemplateException,
    ): ResponseEntity<CommonHttpResponse<Nothing>> =
        ResponseEntity.status(exception.status).body(PromptTemplateErrorResponses.of(exception, messages))
}
