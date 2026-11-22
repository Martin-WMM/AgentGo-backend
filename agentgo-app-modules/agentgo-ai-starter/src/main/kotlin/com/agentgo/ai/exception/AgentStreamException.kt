package com.agentgo.ai.exception

import org.springframework.http.HttpStatus

/**
 * Base exception for agent stream failures that map onto stable response codes.
 *
 * @property code numeric business code exposed to clients
 * @property responseType stable string response type
 * @property status HTTP status associated with the failure
 * @property message safe client-facing message
 */
open class AgentStreamException(
    val code: Int,
    val responseType: String,
    val status: HttpStatus,
    override val message: String,
) : RuntimeException(message)
