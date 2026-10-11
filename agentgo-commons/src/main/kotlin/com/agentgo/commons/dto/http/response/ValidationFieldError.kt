package com.agentgo.commons.dto.http.response

import io.swagger.v3.oas.annotations.media.Schema

/**
 * One field-level Bean Validation failure exposed in the unified error envelope.
 *
 * @property field invalid property path
 * @property message client-safe validation message
 * @property code stable validation response type
 */
@Schema(description = "Field-level validation failure")
data class ValidationFieldError(
    @field:Schema(description = "Invalid property path", example = "name", requiredMode = Schema.RequiredMode.REQUIRED)
    val field: String,
    @field:Schema(description = "Client-safe validation message", example = "must not be blank after trimming", requiredMode = Schema.RequiredMode.REQUIRED)
    val message: String,
    @field:Schema(description = "Stable validation response type", example = "VALIDATION-002", requiredMode = Schema.RequiredMode.REQUIRED)
    val code: String = "VALIDATION-002",
)
