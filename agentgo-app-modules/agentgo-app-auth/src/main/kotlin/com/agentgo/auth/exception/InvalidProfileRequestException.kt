package com.agentgo.auth.exception

/**
 * Raised when the authenticated user submits a profile that fails field validation.
 *
 * @property messageCode i18n key resolved for the API `message` field
 * @param message English fallback used when the message bundle has no entry
 */
class InvalidProfileRequestException(
    val messageCode: String,
    message: String,
) : RuntimeException(message)
