package com.agentgo.commons.dto.http.response.status.error

import com.agentgo.commons.dto.http.response.status.ResponseCodeDescription

/** Registry for external-integration failure codes. */
object IntegrationResponseCodeRegistry {
    val codeDescriptions = mapOf(
        "INTEGRATION-001" to ResponseCodeDescription("External service returned an unexpected failure.", "INTEGRATION", "The external service could not complete the request."),
        "INTEGRATION-002" to ResponseCodeDescription("External service rejected the request.", "INTEGRATION", "The external service rejected the request."),
        "INTEGRATION-003" to ResponseCodeDescription("External service returned an invalid response.", "INTEGRATION", "The external service returned an invalid response."),
        "INTEGRATION-004" to ResponseCodeDescription("External service is unavailable.", "INTEGRATION", "The external service is temporarily unavailable."),
        "INTEGRATION-005" to ResponseCodeDescription("External service call timed out.", "INTEGRATION", "The external service did not respond in time."),
        "INTEGRATION-006" to ResponseCodeDescription("External service communication failed.", "INTEGRATION", "Could not communicate with the external service."),
    )
    val responseTypesByCode = (5001..5006).associateWith { "INTEGRATION-%03d".format(it - 5000) }
}
