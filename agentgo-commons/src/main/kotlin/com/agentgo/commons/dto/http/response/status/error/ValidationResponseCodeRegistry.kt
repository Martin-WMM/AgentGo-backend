package com.agentgo.commons.dto.http.response.status.error

import com.agentgo.commons.dto.http.response.status.ResponseCodeDescription

/** Registry for request-validation failure codes. */
object ValidationResponseCodeRegistry {
    val codeDescriptions = mapOf(
        "VALIDATION-001" to ResponseCodeDescription("A required field is missing.", "VALIDATION", "A required field is missing."),
        "VALIDATION-002" to ResponseCodeDescription("A field has an invalid format.", "VALIDATION", "One or more fields have an invalid format."),
        "VALIDATION-003" to ResponseCodeDescription("A field value is outside its allowed range.", "VALIDATION", "One or more values are outside the allowed range."),
        "VALIDATION-004" to ResponseCodeDescription("A field value is not allowed.", "VALIDATION", "One or more values are not allowed."),
        "VALIDATION-005" to ResponseCodeDescription("Submitted data violates a uniqueness constraint.", "VALIDATION", "A record with this value already exists."),
        "VALIDATION-006" to ResponseCodeDescription("Submitted values conflict with each other.", "VALIDATION", "The submitted values cannot be used together."),
        "VALIDATION-007" to ResponseCodeDescription("Request body cannot be parsed or validated.", "VALIDATION", "The request body is invalid."),
        "VALIDATION-008" to ResponseCodeDescription("Request parameter is invalid.", "VALIDATION", "One or more request parameters are invalid."),
    )
    val responseTypesByCode = (3001..3008).associateWith { "VALIDATION-%03d".format(it - 3000) }
}
