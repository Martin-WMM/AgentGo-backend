package com.agentgo.commons.dto.http.response.status.error

/** Stable response status associated with a registered exception type. */
data class ExceptionResponseCode(
    val code: Int,
    val responseType: String,
)
