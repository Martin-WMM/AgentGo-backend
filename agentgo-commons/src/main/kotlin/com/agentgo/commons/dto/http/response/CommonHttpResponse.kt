package com.agentgo.commons.dto.http.response

import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant
import java.util.UUID

/**
 * Unified HTTP response envelope.
 *
 * [success] is `false` only when [error] provides error information safe for the caller.
 * [code] is a stable numeric system or business code, not an HTTP status code. [responseType]
 * is its corresponding stable string business identifier.
 *
 * @param T type of the optional response payload
 * @property code stable numeric system or business response code
 * @property message human-readable result summary
 * @property data optional successful response payload
 * @property responseType business response classification
 * @property retryable whether the caller may retry the request
 * @property success whether the operation completed successfully
 * @property timestamp instant at which the response was created, in UTC
 * @property requestId identifier used to correlate the response with its request
 * @property error error details; required for unsuccessful responses
 */
@Schema(description = "Unified HTTP response envelope")
data class CommonHttpResponse<T>(
    @field:Schema(description = "Stable numeric system or business response code; HTTP status is carried by the HTTP response status line", example = "2001", requiredMode = Schema.RequiredMode.REQUIRED)
    val code: Int,
    @field:Schema(description = "Human-readable result summary", example = "Request completed", requiredMode = Schema.RequiredMode.REQUIRED)
    val message: String,
    @field:Schema(description = "Optional successful response payload")
    val data: T? = null,
    @field:Schema(description = "Stable string business identifier corresponding to the numeric response code", example = "FILE-001", requiredMode = Schema.RequiredMode.REQUIRED)
    val responseType: String = "SUCCESS",
    @field:Schema(description = "Whether the caller may retry the request", example = "false", requiredMode = Schema.RequiredMode.REQUIRED)
    val retryable: Boolean = false,
    @field:Schema(description = "Whether the operation completed successfully", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
    val success: Boolean = true,
    @field:Schema(description = "UTC instant at which the response was created", format = "date-time", example = "2026-10-03T08:30:00Z", requiredMode = Schema.RequiredMode.REQUIRED)
    val timestamp: Instant = Instant.now(),
    @field:Schema(description = "Request correlation identifier", format = "uuid", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6", requiredMode = Schema.RequiredMode.REQUIRED)
    val requestId: String = UUID.randomUUID().toString(),
    @field:Schema(description = "Error details. This value is required when success is false.")
    val error: Any? = null,
) {
    init {
        require(success || error != null) { "Failed responses must contain error details." }
    }
}
