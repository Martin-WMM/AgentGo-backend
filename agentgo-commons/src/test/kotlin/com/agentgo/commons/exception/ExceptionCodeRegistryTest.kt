package com.agentgo.commons.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ExceptionCodeRegistryTest {
    @Test
    fun resolvesStringExceptionCode() {
        assertEquals(SecurityException::class, ExceptionCodeRegistry.exceptionType("AUTH-001"))
        assertTrue(ExceptionCodeRegistry.contains("AUTH-001"))
    }

    @Test
    fun resolvesIntegerExceptionCode() {
        assertEquals(SecurityException::class, ExceptionCodeRegistry.exceptionType(1001))
        assertTrue(ExceptionCodeRegistry.contains(1001))
    }

    @Test
    fun fallsBackForUnknownCodes() {
        assertEquals(IllegalStateException::class, ExceptionCodeRegistry.exceptionType("UNKNOWN"))
        assertEquals(IllegalStateException::class, ExceptionCodeRegistry.exceptionType(-1))
    }
}
