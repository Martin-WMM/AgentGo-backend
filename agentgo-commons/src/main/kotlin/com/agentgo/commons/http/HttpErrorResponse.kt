package com.agentgo.commons.http

/**
 * Standard error response envelope returned when an HTTP operation cannot be completed.
 *
 * @property code HTTP status code or an application-specific error code
 * @property message safe, human-readable summary of the failure
 * @property responseType semantic response type, fixed to [HttpResponseType.ERROR]
 * @property errors structured error details, such as validation failures
 * @property links related help or remediation links
 * @property retryable whether the client may retry the request
 * @property traceId identifier used to correlate the response with server logs
 */
data class HttpErrorResponse(
    val code: Int,
    val message: String,
    val responseType: HttpResponseType = HttpResponseType.ERROR,
    val errors: List<HttpErrorDetail> = emptyList(),
    val links: List<HttpLink> = emptyList(),
    val retryable: Boolean = false,
    val traceId: String? = null,
)
