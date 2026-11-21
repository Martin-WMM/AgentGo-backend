package com.agentgo.commons.dto.http.response.status.error

import com.agentgo.commons.dto.http.response.status.ResponseCodeDescription

/** Registry for global system-event failure codes. */
object EventsResponseCodeRegistry {
    val codeDescriptions = listOf(
        "EVENT-001" to "Requested system event does not exist." to
            "The requested system event was not found.",
        "EVENT-002" to "System event request parameters are invalid." to
            "The system event request is invalid.",
    ).associate { (pair, message) ->
        pair.first to ResponseCodeDescription(pair.second, "EVENT", message)
    }

    val responseTypesByCode = (9701..9702).associateWith { "EVENT-%03d".format(it - 9700) }
}
