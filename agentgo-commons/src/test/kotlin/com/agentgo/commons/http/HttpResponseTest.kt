package com.agentgo.commons.http

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class HttpResponseTest {
    @Test
    fun createsSuccessfulResponseWithAllStandardFields() {
        val response = HttpResponse(
            code = 200,
            message = "OK",
            data = mapOf("id" to "agent-1"),
            links = listOf(HttpLink(rel = "self", href = "/api/agents/agent-1")),
        )

        assertEquals(200, response.code)
        assertEquals(HttpResponseType.SUCCESS, response.responseType)
        assertEquals(mapOf("id" to "agent-1"), response.data)
        assertEquals("self", response.links.single().rel)
        assertFalse(response.retryable)
    }

    @Test
    fun createsRetryableErrorResponseWithDetails() {
        val response = HttpErrorResponse(
            code = 503,
            message = "Service unavailable",
            errors = listOf(HttpErrorDetail(code = "UPSTREAM_UNAVAILABLE", message = "Try again later")),
            retryable = true,
            traceId = "trace-123",
        )

        assertEquals(HttpResponseType.ERROR, response.responseType)
        assertEquals("UPSTREAM_UNAVAILABLE", response.errors.single().code)
        assertTrue(response.retryable)
        assertEquals("trace-123", response.traceId)
    }
}
