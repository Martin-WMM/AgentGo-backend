package com.agentgo.cli.exception

import com.agentgo.commons.dto.http.response.CommonHttpResponse
import com.agentgo.commons.dto.http.response.status.error.ErrorResponseCodeRegistry
import com.agentgo.starter.i18n.AgentGoMessages

/** Maps CLI execution failures onto the shared HTTP response envelope. */
object CliErrorResponses {
    /**
     * Converts a [CliException] into the response returned to API clients.
     *
     * @param exception domain failure
     * @param messages localized message resolver
     * @return failed [CommonHttpResponse]
     */
    fun of(exception: CliException, messages: AgentGoMessages): CommonHttpResponse<Nothing> =
        CommonHttpResponse(
            code = exception.code,
            message = messages.get(
                exception.responseType,
                defaultMessage = exception.message,
            ),
            retryable = exception.status.is5xxServerError,
            success = false,
            responseType = exception.responseType,
            error = ErrorResponseCodeRegistry.description(exception.responseType),
        )
}