package com.agentgo.commons.dto.http.response.status.error

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ExceptionResponseCodeRegistryTest {
    private class TestFileException : RuntimeException()

    @Test
    fun registersExceptionTypeWithCanonicalResponseStatus() {
        ExceptionResponseCodeRegistry.register<TestFileException>(2001, "FILE-001")

        assertEquals(
            ExceptionResponseCode(2001, "FILE-001"),
            ExceptionResponseCodeRegistry.responseCode(TestFileException::class),
        )
    }

    @Test
    fun rejectsUnregisteredCodeAndResponseTypePairs() {
        assertFailsWith<IllegalArgumentException> {
            ExceptionResponseCodeRegistry.register<IllegalArgumentException>(2001, "AUTH-001")
        }
    }
}
