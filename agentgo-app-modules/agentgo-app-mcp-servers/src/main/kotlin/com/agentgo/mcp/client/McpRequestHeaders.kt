package com.agentgo.mcp.client

import com.agentgo.dto.mcp.McpAuthType

/**
 * Builds HTTP auth/custom headers for an MCP Streamable HTTP request.
 *
 * @param authType authentication mode
 * @param authToken bearer token or custom header value
 * @param authHeaderName custom header name when [authType] is [McpAuthType.HEADER]
 * @param customHeaders additional headers
 * @return header map excluding Accept/Content-Type
 */
fun mcpRequestHeaders(
    authType: McpAuthType,
    authToken: String?,
    authHeaderName: String?,
    customHeaders: Map<String, String>,
): Map<String, String> {
    val headers = LinkedHashMap<String, String>()
    customHeaders.forEach { (key, value) ->
        val trimmedKey = key.trim()
        if (trimmedKey.isNotEmpty()) {
            headers[trimmedKey] = value
        }
    }
    when (authType) {
        McpAuthType.NONE -> Unit
        McpAuthType.BEARER -> {
            val token = authToken?.trim().orEmpty()
            if (token.isNotEmpty()) {
                headers["Authorization"] = "Bearer $token"
            }
        }
        McpAuthType.HEADER -> {
            val headerName = authHeaderName?.trim().orEmpty()
            val token = authToken?.trim().orEmpty()
            if (headerName.isNotEmpty() && token.isNotEmpty()) {
                headers[headerName] = token
            }
        }
    }
    return headers
}
