package com.agentgo.settings.exception

import org.springframework.http.HttpStatus

/**
 * Base exception for system-settings failures that map onto stable response codes.
 *
 * @property code numeric business code exposed to clients
 * @property responseType stable string response type such as `AUTH-002`
 * @property status HTTP status associated with the failure
 * @property message safe client-facing message
 */
open class SettingsException(
    val code: Int,
    val responseType: String,
    val status: HttpStatus,
    override val message: String,
) : RuntimeException(message)
