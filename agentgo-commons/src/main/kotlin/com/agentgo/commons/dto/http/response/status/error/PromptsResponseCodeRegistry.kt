package com.agentgo.commons.dto.http.response.status.error

import com.agentgo.commons.dto.http.response.status.ResponseCodeDescription

/** Registry for prompt-template failure codes. */
object PromptsResponseCodeRegistry {
    val codeDescriptions = listOf(
        "PROMPT-001" to "Requested prompt template does not exist for the authenticated user." to
            "The requested prompt template was not found.",
        "PROMPT-002" to "Prompt template request parameters are invalid." to
            "The prompt template request is invalid.",
        "PROMPT-003" to "A prompt template with the same name already exists for this user." to
            "A prompt template with this name already exists for this user.",
        "PROMPT-004" to "Prompt template could not be rendered with the supplied variables." to
            "The prompt template could not be rendered.",
        "PROMPT-005" to "Prompt template operation is not authorized." to
            "You do not have access to this prompt template.",
    ).associate { (pair, message) ->
        pair.first to ResponseCodeDescription(pair.second, "PROMPT", message)
    }

    val responseTypesByCode = (9001..9005).associateWith { "PROMPT-%03d".format(it - 9000) }
}
