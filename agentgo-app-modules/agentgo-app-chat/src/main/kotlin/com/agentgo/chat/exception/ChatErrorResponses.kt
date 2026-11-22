package com.agentgo.chat.exception

import com.agentgo.commons.dto.http.response.CommonHttpResponse
import com.agentgo.commons.dto.http.response.status.error.ErrorResponseCodeRegistry

/** Maps chat failures onto the shared HTTP response envelope. */
object ChatErrorResponses {
    /**
     * Converts a [ChatException] into the response returned to API clients.
     *
     * @param exception domain failure
     * @return failed [CommonHttpResponse]
     */
    fun of(exception: ChatException): CommonHttpResponse<Nothing> =
        CommonHttpResponse(
            code = exception.code,
            message = exception.message,
            retryable = exception.status.is5xxServerError,
            success = false,
            responseType = exception.responseType,
            error = ErrorResponseCodeRegistry.description(exception.responseType),
        )
}
