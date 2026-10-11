package com.agentgo.commons.dto.http.response.status.success

import com.agentgo.commons.dto.http.response.status.ResponseCodeDescription

/** Registry for successful MCP server management response codes. */
object McpResponseCodeRegistry {
    const val LIST_RETRIEVED = 9600
    const val LIST_RETRIEVED_TYPE = "MCP-LIST-RETRIEVED"
    const val CREATED = 9601
    const val CREATED_TYPE = "MCP-CREATED"
    const val RETRIEVED = 9602
    const val RETRIEVED_TYPE = "MCP-RETRIEVED"
    const val UPDATED = 9603
    const val UPDATED_TYPE = "MCP-UPDATED"
    const val DELETED = 9604
    const val DELETED_TYPE = "MCP-DELETED"
    const val PROBED = 9605
    const val PROBED_TYPE = "MCP-PROBED"

    val codeDescriptions = mapOf(
        LIST_RETRIEVED_TYPE to ResponseCodeDescription(
            "User-owned MCP servers were listed successfully.",
            "MCP",
            "MCP servers listed.",
        ),
        CREATED_TYPE to ResponseCodeDescription(
            "A user-owned MCP server was created successfully.",
            "MCP",
            "MCP server created.",
        ),
        RETRIEVED_TYPE to ResponseCodeDescription(
            "A user-owned MCP server was retrieved successfully.",
            "MCP",
            "MCP server retrieved.",
        ),
        UPDATED_TYPE to ResponseCodeDescription(
            "A user-owned MCP server was updated successfully.",
            "MCP",
            "MCP server updated.",
        ),
        DELETED_TYPE to ResponseCodeDescription(
            "A user-owned MCP server was deleted successfully.",
            "MCP",
            "MCP server deleted.",
        ),
        PROBED_TYPE to ResponseCodeDescription(
            "An MCP Streamable HTTP probe completed and refreshed discovery cache.",
            "MCP",
            "MCP server probed.",
        ),
    )

    val responseTypesByCode = mapOf(
        LIST_RETRIEVED to LIST_RETRIEVED_TYPE,
        CREATED to CREATED_TYPE,
        RETRIEVED to RETRIEVED_TYPE,
        UPDATED to UPDATED_TYPE,
        DELETED to DELETED_TYPE,
        PROBED to PROBED_TYPE,
    )
}
