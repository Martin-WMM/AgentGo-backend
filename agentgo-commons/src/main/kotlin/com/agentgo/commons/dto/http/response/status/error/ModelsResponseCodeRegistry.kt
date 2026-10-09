package com.agentgo.commons.dto.http.response.status.error

import com.agentgo.commons.dto.http.response.status.ResponseCodeDescription

/** Registry for model-management failure codes. */
object ModelsResponseCodeRegistry {
    val codeDescriptions = listOf(
        "MODEL-001" to "Requested model does not exist for the authenticated user." to
            "The requested model was not found.",
        "MODEL-002" to "Model request parameters are invalid." to
            "The model request is invalid.",
        "MODEL-003" to "A model with the same name already exists for the authenticated user." to
            "A model with this name already exists.",
        "MODEL-004" to "Model operation is not authorized." to
            "You do not have access to this model.",
        "MODEL-005" to "Model metadata is in an invalid state." to
            "The model metadata cannot be processed.",
    ).associate { (pair, message) ->
        pair.first to ResponseCodeDescription(pair.second, "MODEL", message)
    }

    val responseTypesByCode = (8001..8005).associateWith { "MODEL-%03d".format(it - 8000) }
}
