package com.agentgo.commons.dto.http.response.status.error

import com.agentgo.commons.dto.http.response.status.ResponseCodeDescription

/** Registry for system failure codes. */
object SystemResponseCodeRegistry {
    val codeDescriptions = mapOf(
        "SYSTEM-001" to ResponseCodeDescription("Unexpected internal system failure occurred.", "SYSTEM", "An unexpected internal error occurred."),
        "SYSTEM-002" to ResponseCodeDescription("System dependency is unavailable.", "SYSTEM", "A required system component is temporarily unavailable."),
        "SYSTEM-003" to ResponseCodeDescription("Requested system operation is unsupported.", "SYSTEM", "This system operation is not supported."),
    )
    val responseTypesByCode = (7001..7003).associateWith { "SYSTEM-%03d".format(it - 7000) }
}
