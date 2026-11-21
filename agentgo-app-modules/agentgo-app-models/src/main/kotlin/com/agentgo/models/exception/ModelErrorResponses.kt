package com.agentgo.models.exception

import com.agentgo.commons.dto.http.response.CommonHttpResponse
import com.agentgo.commons.dto.http.response.status.error.ErrorResponseCodeRegistry

/** Maps model failures onto the shared HTTP response envelope. */
object ModelErrorResponses {
    /**
     * Converts a [ModelException] into the response returned to API clients.
     *
     * @param exception domain failure
     * @return failed [CommonHttpResponse]
     */
    fun of(exception: ModelException): CommonHttpResponse<Nothing> =
        CommonHttpResponse(
            code = exception.code,
            message = exception.message,
            retryable = exception.status.is5xxServerError,
            success = false,
            responseType = exception.responseType,
            error = ErrorResponseCodeRegistry.description(exception.responseType),
        )
}
