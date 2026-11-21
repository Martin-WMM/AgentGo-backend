package com.agentgo.commons.dto.http.response.status.error

import com.agentgo.commons.dto.http.response.status.ResponseCodeDescription

/** Registry for A2A agent management failure codes. */
object A2aResponseCodeRegistry {
    val codeDescriptions = listOf(
        "A2A-001" to "Requested A2A agent does not exist for the authenticated user." to
            "The requested A2A agent was not found.",
        "A2A-002" to "A2A agent request parameters are invalid." to
            "The A2A agent request is invalid.",
        "A2A-003" to "An A2A agent with the same group, name, and creator already exists." to
            "An A2A agent with this group and name already exists.",
    ).associate { (pair, message) ->
        pair.first to ResponseCodeDescription(pair.second, "A2A", message)
    }

    val responseTypesByCode = mapOf(
        9931 to "A2A-001",
        9932 to "A2A-002",
        9933 to "A2A-003",
    )
}
