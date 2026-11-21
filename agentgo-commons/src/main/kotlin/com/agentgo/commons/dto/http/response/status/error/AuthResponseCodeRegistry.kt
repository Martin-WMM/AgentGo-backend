package com.agentgo.commons.dto.http.response.status.error

import com.agentgo.commons.dto.http.response.status.ResponseCodeDescription

/** Registry for authentication and authorization failure codes. */
object AuthResponseCodeRegistry {
    val codeDescriptions = listOf(
        "AUTH-001" to "Credentials are invalid." to "The supplied credentials are invalid.", "AUTH-002" to "Authentication credentials are missing." to "Authentication is required.", "AUTH-003" to "Authentication token has expired." to "Your authentication token has expired.", "AUTH-004" to "Authenticated user lacks the required permission." to "You do not have permission to perform this action.", "AUTH-005" to "Account is locked or disabled." to "This account is currently unavailable.", "AUTH-006" to "Authentication input is invalid." to "The authentication request is invalid.", "AUTH-007" to "Authentication session is in an invalid state." to "The authentication session cannot be used.", "AUTH-008" to "Authentication provider is unavailable." to "Authentication is temporarily unavailable.", "AUTH-009" to "Security verification failed." to "Security verification failed.", "AUTH-010" to "Requested authentication flow is unsupported." to "This authentication flow is not supported.",
    ).associate { (pair, message) -> pair.first to ResponseCodeDescription(pair.second, "AUTHENTICATION", message) }
    val responseTypesByCode = (1001..1010).associateWith { "AUTH-%03d".format(it - 1000) }
}
