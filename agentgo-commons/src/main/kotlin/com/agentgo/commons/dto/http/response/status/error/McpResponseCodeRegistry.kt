package com.agentgo.commons.dto.http.response.status.error

import com.agentgo.commons.dto.http.response.status.ResponseCodeDescription

/** Registry for MCP server management failure codes. */
object McpResponseCodeRegistry {
    val codeDescriptions = listOf(
        "MCP-001" to "Requested MCP server does not exist for the authenticated user." to
            "The requested MCP server was not found.",
        "MCP-002" to "MCP server request parameters are invalid." to
            "The MCP server request is invalid.",
        "MCP-003" to "An MCP server with the same name already exists for the authenticated user." to
            "An MCP server with this name already exists.",
        "MCP-004" to "MCP server operation is not authorized." to
            "You do not have access to this MCP server.",
        "MCP-005" to "MCP server metadata is in an invalid state." to
            "The MCP server metadata cannot be processed.",
        "MCP-006" to "MCP Streamable HTTP probe failed." to
            "Unable to connect to the MCP server.",
    ).associate { (pair, message) ->
        pair.first to ResponseCodeDescription(pair.second, "MCP", message)
    }

    val responseTypesByCode = (9501..9506).associateWith { "MCP-%03d".format(it - 9500) }
}
