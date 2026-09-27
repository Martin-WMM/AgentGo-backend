package com.agentgo.commons.http

/** A machine-readable detail describing one part of an HTTP error. */
data class HttpErrorDetail(
    val code: String,
    val message: String,
    val field: String? = null,
)
