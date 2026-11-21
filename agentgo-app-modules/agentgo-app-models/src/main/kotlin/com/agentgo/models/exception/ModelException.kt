package com.agentgo.models.exception

import org.springframework.http.HttpStatus

/**
 * Base exception for model-management failures that map onto stable response codes.
 *
 * @property code numeric business code exposed to clients
 * @property responseType stable string response type such as `MODEL-001`
 * @property status HTTP status associated with the failure
 * @property message safe client-facing message
 */
open class ModelException(
    val code: Int,
    val responseType: String,
    val status: HttpStatus,
    override val message: String,
) : RuntimeException(message)
