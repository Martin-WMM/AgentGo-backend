package com.agentgo.dto.cli

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CliExecutionDtoTest {
    @Test
    fun `request stores script and environment`() {
        val request = CliExecutionRequest().apply {
            script = "echo ok"
            environment = mapOf("FOO" to "bar")
        }

        assertEquals("echo ok", request.script)
        assertEquals(mapOf("FOO" to "bar"), request.environment)
    }

    @Test
    fun `request defaults environment to null`() {
        val request = CliExecutionRequest().apply { script = "pwd" }

        assertEquals("pwd", request.script)
        assertNull(request.environment)
    }

    @Test
    fun `request starts with empty script by default`() {
        assertEquals("", CliExecutionRequest().script)
    }

    @Test
    fun `response equality checks every field`() {
        val response = sampleResponse()
        val same = sampleResponse()

        assertEquals(response, same)
        assertEquals(response.hashCode(), same.hashCode())
        assertNotEquals(response, response.copy(exitCode = 1))
        assertNotEquals(response, response.copy(stdout = "other"))
        assertNotEquals(response, response.copy(stderr = "other"))
        assertNotEquals(response, response.copy(durationMs = 99))
        assertNotEquals(response, response.copy(status = CliExecutionStatus.TIMED_OUT))
        assertFalse(response.equals(null))
        assertFalse(response.equals("not-a-response"))
    }

    @Test
    fun `response exposes component accessors and toString`() {
        val response = sampleResponse()

        assertEquals(0, response.component1())
        assertEquals("ok\n", response.component2())
        assertEquals("", response.component3())
        assertEquals(12, response.component4())
        assertEquals(CliExecutionStatus.COMPLETED, response.component5())
        assertTrue(response.toString().contains("exitCode=0"))
        assertTrue(response.toString().contains("COMPLETED"))
    }

    @Test
    fun `response copy can switch to failed to start`() {
        val response = sampleResponse().copy(
            exitCode = -1,
            stderr = "boom",
            status = CliExecutionStatus.FAILED_TO_START,
        )

        assertEquals(-1, response.exitCode)
        assertEquals("boom", response.stderr)
        assertEquals(CliExecutionStatus.FAILED_TO_START, response.status)
    }

    @Test
    fun `status enum exposes every outcome`() {
        assertEquals(
            listOf(
                CliExecutionStatus.COMPLETED,
                CliExecutionStatus.TIMED_OUT,
                CliExecutionStatus.FAILED_TO_START,
                CliExecutionStatus.REJECTED,
            ),
            CliExecutionStatus.entries,
        )
        assertEquals(CliExecutionStatus.TIMED_OUT, CliExecutionStatus.valueOf("TIMED_OUT"))
    }

    private fun sampleResponse() = CliExecutionResponse(
        exitCode = 0,
        stdout = "ok\n",
        stderr = "",
        durationMs = 12,
        status = CliExecutionStatus.COMPLETED,
    )
}
