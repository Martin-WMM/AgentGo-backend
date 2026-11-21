package com.agentgo.a2a.exception

import org.springframework.http.HttpStatus

/**
 * Base exception for A2A agent management failures that map onto stable response codes.
 *
 * @property code numeric business code exposed to clients
 * @property responseType stable string response type such as `A2A-001`
 * @property status HTTP status associated with the failure
 * @property message safe client-facing message
 */
open class A2aException(
    val code: Int,
    val responseType: String,
    val status: HttpStatus,
    override val message: String,
) : RuntimeException(message)
