package com.agentgo.events.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import org.springframework.http.HttpStatus

class InvalidSystemEventRequestExceptionTest {
    @Test
    fun `default message uses the stable client-facing text`() {
        val exception = InvalidSystemEventRequestException()

        assertEquals("The system event request is invalid.", exception.message)
        assertEquals(9702, exception.code)
        assertEquals("EVENT-002", exception.responseType)
        assertEquals(HttpStatus.BAD_REQUEST, exception.status)
    }
}
