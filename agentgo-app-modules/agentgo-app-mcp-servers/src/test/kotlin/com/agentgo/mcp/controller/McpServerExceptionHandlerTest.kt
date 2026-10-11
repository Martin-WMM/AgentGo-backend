package com.agentgo.mcp.controller

import com.agentgo.mcp.exception.McpErrorResponses
import com.agentgo.mcp.exception.McpException
import com.agentgo.mcp.exception.McpProbeFailedException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.springframework.http.HttpStatus

class McpServerExceptionHandlerTest {
    private val handler = McpServerExceptionHandler()

    @Test
    fun `maps mcp exception to the shared error envelope`() {
        val response = handler.handleMcpException(
            McpException(
                code = 9601,
                responseType = "MCP-001",
                status = HttpStatus.NOT_FOUND,
                message = "missing",
            ),
        )

        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
        assertEquals(9601, response.body?.code)
        assertEquals("MCP-001", response.body?.responseType)
        assertEquals("missing", response.body?.message)
        assertFalse(response.body?.success == true)
        assertEquals(false, response.body?.retryable)
    }

    @Test
    fun `marks server failures as retryable`() {
        val envelope = McpErrorResponses.of(
            McpProbeFailedException("probe failed"),
        )

        assertEquals(HttpStatus.BAD_GATEWAY.is5xxServerError, envelope.retryable)
        assertTrue(envelope.retryable)
        assertFalse(envelope.success)
    }
}
