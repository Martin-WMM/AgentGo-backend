package com.agentgo.starter

import com.agentgo.commons.dto.http.response.CommonHttpResponse
import com.agentgo.commons.dto.http.response.ValidationFieldError
import com.agentgo.commons.dto.http.response.status.error.ErrorResponseCodeRegistry
import com.agentgo.starter.i18n.AgentGoMessages
import jakarta.validation.ConstraintViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.BindException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

/**
 * Maps Jakarta Bean Validation failures onto the unified AgentGo HTTP envelope.
 *
 * Controllers should annotate request bodies with `@Valid` so Spring raises
 * [MethodArgumentNotValidException] before the service layer runs.
 *
 * @property messages localized API message resolver
 */
@RestControllerAdvice
class ValidationExceptionHandler(
    private val messages: AgentGoMessages,
) {
    /**
     * Handles `@RequestBody @Valid` failures.
     *
     * @param exception Spring MVC validation failure
     * @return HTTP 400 with field-level details
     */
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValid(
        exception: MethodArgumentNotValidException,
    ): ResponseEntity<CommonHttpResponse<Nothing>> =
        validationResponse(
            exception.bindingResult.fieldErrors.map { error ->
                ValidationFieldError(
                    field = error.field,
                    message = error.defaultMessage
                        ?: messages.get("validation.field.invalid", defaultMessage = "invalid value"),
                )
            },
        )

    /**
     * Handles `@Validated` / form-binding failures.
     *
     * @param exception Spring binding failure
     * @return HTTP 400 with field-level details
     */
    @ExceptionHandler(BindException::class)
    fun handleBindException(exception: BindException): ResponseEntity<CommonHttpResponse<Nothing>> =
        validationResponse(
            exception.bindingResult.fieldErrors.map { error ->
                ValidationFieldError(
                    field = error.field,
                    message = error.defaultMessage
                        ?: messages.get("validation.field.invalid", defaultMessage = "invalid value"),
                )
            },
        )

    /**
     * Handles direct [jakarta.validation.Validator] / JPA entity constraint failures.
     *
     * @param exception constraint violation set
     * @return HTTP 400 with field-level details
     */
    @ExceptionHandler(ConstraintViolationException::class)
    fun handleConstraintViolation(
        exception: ConstraintViolationException,
    ): ResponseEntity<CommonHttpResponse<Nothing>> =
        validationResponse(
            exception.constraintViolations.map { violation ->
                ValidationFieldError(
                    field = violation.propertyPath?.toString().orEmpty().ifBlank { "request" },
                    message = violation.message,
                )
            },
        )

    private fun validationResponse(
        fieldErrors: List<ValidationFieldError>,
    ): ResponseEntity<CommonHttpResponse<Nothing>> {
        val errors = fieldErrors.ifEmpty {
            listOf(
                ValidationFieldError(
                    field = "request",
                    message = messages.get(
                        "validation.request.invalid",
                        defaultMessage = "The request body is invalid.",
                    ),
                ),
            )
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            CommonHttpResponse(
                code = 3002,
                message = messages.get("VALIDATION-002", defaultMessage = errors.first().message),
                retryable = false,
                success = false,
                responseType = "VALIDATION-002",
                error = mapOf(
                    "description" to ErrorResponseCodeRegistry.description("VALIDATION-002"),
                    "fields" to errors,
                ),
            ),
        )
    }
}
