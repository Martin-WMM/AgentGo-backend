package com.agentgo.cli.exception

import org.springframework.http.HttpStatus

/**
 * Base exception for CLI execution failures that map onto stable response codes.
 *
 * @property code numeric business code exposed to clients
 * @property responseType stable string response type such as `CLI-001`
 * @property status HTTP status associated with the failure
 * @property message safe client-facing message
 */
open class CliException(
    val code: Int,
    val responseType: String,
    val status: HttpStatus,
    override val message: String,
) : RuntimeException(message)