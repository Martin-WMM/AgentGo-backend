package com.agentgo.mcp.client

import com.agentgo.dto.mcp.McpAuthType
import kotlin.test.Test
import kotlin.test.assertEquals

class McpRequestHeadersTest {
    @Test
    fun `builds bearer authorization header`() {
        val headers = mcpRequestHeaders(
            authType = McpAuthType.BEARER,
            authToken = " secret ",
            authHeaderName = null,
            customHeaders = mapOf("X-Tenant" to "demo"),
        )

        assertEquals("Bearer secret", headers["Authorization"])
        assertEquals("demo", headers["X-Tenant"])
    }

    @Test
    fun `builds custom auth header`() {
        val headers = mcpRequestHeaders(
            authType = McpAuthType.HEADER,
            authToken = "key",
            authHeaderName = "X-API-Key",
            customHeaders = emptyMap(),
        )

        assertEquals("key", headers["X-API-Key"])
    }
}
