package com.agentgo.commons.dto.http.response.status.error

import com.agentgo.commons.dto.http.response.status.ResponseCodeDescription

/** Registry for CLI execution failure codes. */
object CliResponseCodeRegistry {
    val codeDescriptions = listOf(
        "CLI-001" to "CLI script or request parameters are invalid." to
            "The CLI script is invalid.",
        "CLI-002" to "CLI process failed to start." to
            "The CLI process could not be started.",
        "CLI-003" to "All CLI workers are busy." to
            "All CLI workers are busy. Retry later.",
        "CLI-004" to "CLI command is forbidden by policy." to
            "This CLI command is not allowed.",
    ).associate { (pair, message) ->
        pair.first to ResponseCodeDescription(pair.second, "CLI", message)
    }

    val responseTypesByCode = (9301..9304).associateWith { "CLI-%03d".format(it - 9300) }
}