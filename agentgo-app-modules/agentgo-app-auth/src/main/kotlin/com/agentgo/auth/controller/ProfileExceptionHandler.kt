package com.agentgo.auth.controller

import com.agentgo.auth.exception.InvalidProfileRequestException
import com.agentgo.commons.dto.http.response.CommonHttpResponse
import com.agentgo.commons.dto.http.response.status.error.ErrorResponseCodeRegistry
import com.agentgo.starter.i18n.AgentGoMessages
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

/**
 * Translates invalid profile submissions into the shared HTTP error envelope.
 *
 * Successful profile reads and updates still return [com.agentgo.dto.auth.ProfileResponse]
 * directly so existing clients keep their current contract.
 *
 * @property messages localized API message resolver
 */
@RestControllerAdvice(basePackageClasses = [AuthController::class])
class ProfileExceptionHandler(
    private val messages: AgentGoMessages,
) {
    /**
     * Handles profile field validation failures.
     *
     * @param exception invalid profile submission
     * @return HTTP 400 with a localized message
     */
    @ExceptionHandler(InvalidProfileRequestException::class)
    fun handleInvalidProfile(
        exception: InvalidProfileRequestException,
    ): ResponseEntity<CommonHttpResponse<Nothing>> =
        ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            CommonHttpResponse(
                code = 1006,
                message = messages.get(
                    exception.messageCode,
                    defaultMessage = exception.message ?: "The profile request is invalid.",
                ),
                responseType = "AUTH-006",
                success = false,
                error = ErrorResponseCodeRegistry.description("AUTH-006"),
            ),
        )
}
