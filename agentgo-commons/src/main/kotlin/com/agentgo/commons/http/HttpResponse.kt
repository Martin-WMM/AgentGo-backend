package com.agentgo.commons.http

/**
 * Standard response envelope for successful HTTP operations.
 *
 * @param T type of the response payload
 * @property code HTTP status code or an application-specific response code
 * @property message human-readable response message
 * @property responseType semantic type of the response
 * @property data response payload; it may be absent for responses without a body
 * @property links related resources or follow-up actions
 * @property retryable whether the client may retry the request
 */
data class HttpResponse<T>(
    val code: Int,
    val message: String,
    val responseType: HttpResponseType = HttpResponseType.SUCCESS,
    val data: T? = null,
    val links: List<HttpLink> = emptyList(),
    val retryable: Boolean = false,
)
