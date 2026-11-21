package com.agentgo.a2a.exception

import com.agentgo.commons.dto.http.response.CommonHttpResponse
import com.agentgo.commons.dto.http.response.status.error.ErrorResponseCodeRegistry

/** Maps A2A failures onto the shared HTTP response envelope. */
object A2aErrorResponses {
    /**
     * Converts an [A2aException] into the response returned to API clients.
     *
     * @param exception domain failure
     * @return failed [CommonHttpResponse]
     */
    fun of(exception: A2aException): CommonHttpResponse<Nothing> =
        CommonHttpResponse(
            code = exception.code,
            message = exception.message,
            retryable = exception.status.is5xxServerError,
            success = false,
            responseType = exception.responseType,
            error = ErrorResponseCodeRegistry.description(exception.responseType),
        )
}
