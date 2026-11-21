package com.agentgo.settings.controller

import com.agentgo.commons.dto.http.response.CommonHttpResponse
import com.agentgo.settings.exception.SettingsErrorResponses
import com.agentgo.settings.exception.SettingsException
import com.agentgo.starter.i18n.AgentGoMessages
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

/**
 * Translates [SettingsException] failures into the shared HTTP response envelope.
 *
 * @property messages localized API message resolver
 */
@RestControllerAdvice(basePackageClasses = [SettingsController::class])
class SettingsExceptionHandler(
    private val messages: AgentGoMessages,
) {
    /**
     * Handles domain settings failures raised by the settings module.
     *
     * @param exception domain failure
     * @return HTTP response with the mapped business code
     */
    @ExceptionHandler(SettingsException::class)
    fun handleSettingsException(exception: SettingsException): ResponseEntity<CommonHttpResponse<Nothing>> =
        ResponseEntity.status(exception.status).body(SettingsErrorResponses.of(exception, messages))
}
