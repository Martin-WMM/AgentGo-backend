package com.agentgo.commons.dto.http.response.status.error

import com.agentgo.commons.dto.http.response.status.ResponseCodeDescription

/** Registry for database failure codes. */
object DatabaseResponseCodeRegistry {
    val codeDescriptions = mapOf(
        "DATABASE-001" to ResponseCodeDescription("Database operation failed.", "DATABASE", "The database operation could not be completed."),
        "DATABASE-002" to ResponseCodeDescription("Database transaction could not be completed.", "DATABASE", "The database transaction failed."),
        "DATABASE-003" to ResponseCodeDescription("Database constraint was violated.", "DATABASE", "The requested change violates a data constraint."),
        "DATABASE-004" to ResponseCodeDescription("Database is unavailable.", "DATABASE", "The database is temporarily unavailable."),
    )
    val responseTypesByCode = (6001..6004).associateWith { "DATABASE-%03d".format(it - 6000) }
}
