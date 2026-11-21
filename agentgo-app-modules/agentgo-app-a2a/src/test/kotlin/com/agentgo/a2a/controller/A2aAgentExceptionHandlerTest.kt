package com.agentgo.a2a.controller

import com.agentgo.a2a.exception.A2aAgentNotFoundException
import com.agentgo.a2a.exception.A2aErrorResponses
import com.agentgo.a2a.exception.A2aException
import com.agentgo.a2a.exception.DuplicateA2aAgentNameException
import com.agentgo.a2a.exception.InvalidA2aAgentRequestException
import com.agentgo.commons.dto.http.response.status.ResponseCodeDescription
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.springframework.http.HttpStatus

class A2aAgentExceptionHandlerTest {
    private val handler = A2aAgentExceptionHandler()

    @Test
    fun `maps a missing agent onto the not found envelope`() {
        val response = handler.handleA2aException(A2aAgentNotFoundException())

        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
        assertEquals(9931, response.body?.code)
        assertEquals("A2A-001", response.body?.responseType)
        assertEquals("The requested A2A agent was not found.", response.body?.message)
        assertEquals("A2A", (response.body?.error as ResponseCodeDescription).category)
        assertFalse(response.body?.success == true)
        assertEquals(false, response.body?.retryable)
    }

    @Test
    fun `maps a duplicate name onto the conflict envelope`() {
        val response = handler.handleA2aException(DuplicateA2aAgentNameException())

        assertEquals(HttpStatus.CONFLICT, response.statusCode)
        assertEquals(9933, response.body?.code)
        assertEquals("A2A-003", response.body?.responseType)
        assertEquals("An A2A agent with this group and name already exists.", response.body?.message)
        assertEquals(false, response.body?.retryable)
    }

    @Test
    fun `uses the default invalid request message`() {
        val exception = InvalidA2aAgentRequestException()

        assertEquals(9932, exception.code)
        assertEquals("A2A-002", exception.responseType)
        assertEquals(HttpStatus.BAD_REQUEST, exception.status)
        assertEquals("The A2A agent request is invalid.", exception.message)
        assertEquals(false, A2aErrorResponses.of(exception).retryable)
    }

    @Test
    fun `marks server failures as retryable`() {
        val response = handler.handleA2aException(
            A2aException(
                code = 9931,
                responseType = "A2A-001",
                status = HttpStatus.BAD_GATEWAY,
                message = "downstream failed",
            ),
        )

        assertEquals(HttpStatus.BAD_GATEWAY, response.statusCode)
        assertEquals("downstream failed", response.body?.message)
        assertEquals("A2A-001", response.body?.responseType)
        assertTrue(response.body?.retryable == true)
        assertFalse(response.body?.success == true)
        assertEquals("A2A", (response.body?.error as ResponseCodeDescription).category)
    }
}
