package com.agentgo.commons.dto.http.response.status.error

import com.agentgo.commons.dto.http.response.status.ResponseCodeDescription

/** Registry for file-workspace failure codes. */
object WorkspaceResponseCodeRegistry {
    val codeDescriptions = listOf(
        "WORKSPACE-001" to "Requested workspace does not exist for the authenticated user." to
            "The requested workspace was not found.",
        "WORKSPACE-002" to "Workspace request parameters are invalid." to
            "The workspace request is invalid.",
        "WORKSPACE-003" to "A workspace with the same name already exists for the authenticated user." to
            "A workspace with this name already exists.",
        "WORKSPACE-004" to "Workspace still contains files and cannot be deleted." to
            "Delete all files in the workspace before removing it.",
    ).associate { (pair, message) ->
        pair.first to ResponseCodeDescription(pair.second, "WORKSPACE", message)
    }

    val responseTypesByCode = (2301..2304).associateWith { "WORKSPACE-%03d".format(it - 2300) }
}
