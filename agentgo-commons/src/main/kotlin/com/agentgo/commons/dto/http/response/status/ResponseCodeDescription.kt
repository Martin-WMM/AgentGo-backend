package com.agentgo.commons.dto.http.response.status

/**
 * Documentation for a stable system or business response code.
 *
 * @property meaning explanation of the condition or successful outcome represented by the code
 * @property category response category used for grouping and reporting
 * @property exampleMessage safe example message suitable for an API response
 */
data class ResponseCodeDescription(
    val meaning: String,
    val category: String,
    val exampleMessage: String,
)
