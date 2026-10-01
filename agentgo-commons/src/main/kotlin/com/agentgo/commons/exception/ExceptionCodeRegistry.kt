package com.agentgo.commons.exception

import java.nio.file.NoSuchFileException
import kotlin.reflect.KClass

/**
 * Central registry that maps stable AgentGo error codes to their exception types.
 *
 * Keep codes stable once they are exposed through an API. Add a new entry instead
 * of reusing an existing code for a different failure category.
 */
object ExceptionCodeRegistry {
    /** The complete code-to-exception mapping used by shared error handling. */
    val codeToException: Map<String, KClass<out Throwable>> = linkedMapOf(
        "AUTH-001" to SecurityException::class,
        "AUTH-002" to SecurityException::class,
        "AUTH-003" to SecurityException::class,
        "AUTH-004" to SecurityException::class,
        "AUTH-005" to SecurityException::class,
        "AUTH-006" to IllegalArgumentException::class,
        "AUTH-007" to IllegalStateException::class,
        "AUTH-008" to IllegalStateException::class,
        "AUTH-009" to SecurityException::class,
        "AUTH-010" to UnsupportedOperationException::class,
        "FILE-001" to IllegalArgumentException::class,
        "FILE-002" to IllegalArgumentException::class,
        "FILE-003" to IllegalStateException::class,
        "FILE-004" to IllegalStateException::class,
        "FILE-005" to NoSuchFileException::class,
        "FILE-006" to SecurityException::class,
        "FILE-007" to IllegalStateException::class,
        "FILE-008" to IllegalArgumentException::class,
        "FILE-009" to IllegalStateException::class,
        "FILE-010" to UnsupportedOperationException::class,
        "VALIDATION-001" to IllegalArgumentException::class,
        "VALIDATION-002" to IllegalArgumentException::class,
        "VALIDATION-003" to IllegalArgumentException::class,
        "VALIDATION-004" to IllegalArgumentException::class,
        "VALIDATION-005" to IllegalArgumentException::class,
        "VALIDATION-006" to IllegalArgumentException::class,
        "VALIDATION-007" to IllegalArgumentException::class,
        "VALIDATION-008" to IllegalArgumentException::class,
        "RESOURCE-001" to NoSuchElementException::class,
        "RESOURCE-002" to IllegalStateException::class,
        "RESOURCE-003" to IllegalStateException::class,
        "RESOURCE-004" to UnsupportedOperationException::class,
        "INTEGRATION-001" to IllegalStateException::class,
        "INTEGRATION-002" to IllegalStateException::class,
        "INTEGRATION-003" to IllegalStateException::class,
        "INTEGRATION-004" to IllegalStateException::class,
        "INTEGRATION-005" to java.util.concurrent.TimeoutException::class,
        "INTEGRATION-006" to java.io.IOException::class,
        "DATABASE-001" to IllegalStateException::class,
        "DATABASE-002" to IllegalStateException::class,
        "DATABASE-003" to IllegalStateException::class,
        "DATABASE-004" to IllegalStateException::class,
        "SYSTEM-001" to IllegalStateException::class,
        "SYSTEM-002" to IllegalStateException::class,
        "SYSTEM-003" to UnsupportedOperationException::class,
    )

    /** Numeric error-code mapping for protocols that cannot transport string codes. */
    val intCodeToException: Map<Int, KClass<out Throwable>> = linkedMapOf(
        1001 to SecurityException::class,
        1002 to SecurityException::class,
        1003 to SecurityException::class,
        1004 to SecurityException::class,
        1005 to SecurityException::class,
        1006 to IllegalArgumentException::class,
        1007 to IllegalStateException::class,
        1008 to IllegalStateException::class,
        1009 to SecurityException::class,
        1010 to UnsupportedOperationException::class,
        2001 to IllegalArgumentException::class,
        2002 to IllegalArgumentException::class,
        2003 to IllegalStateException::class,
        2004 to IllegalStateException::class,
        2005 to NoSuchFileException::class,
        2006 to SecurityException::class,
        2007 to IllegalStateException::class,
        2008 to IllegalArgumentException::class,
        2009 to IllegalStateException::class,
        2010 to UnsupportedOperationException::class,
        3001 to IllegalArgumentException::class,
        3002 to IllegalArgumentException::class,
        3003 to IllegalArgumentException::class,
        3004 to IllegalArgumentException::class,
        3005 to IllegalArgumentException::class,
        3006 to IllegalArgumentException::class,
        3007 to IllegalArgumentException::class,
        3008 to IllegalArgumentException::class,
        4001 to NoSuchElementException::class,
        4002 to IllegalStateException::class,
        4003 to IllegalStateException::class,
        4004 to UnsupportedOperationException::class,
        5001 to IllegalStateException::class,
        5002 to IllegalStateException::class,
        5003 to IllegalStateException::class,
        5004 to IllegalStateException::class,
        5005 to java.util.concurrent.TimeoutException::class,
        5006 to java.io.IOException::class,
        6001 to IllegalStateException::class,
        6002 to IllegalStateException::class,
        6003 to IllegalStateException::class,
        6004 to IllegalStateException::class,
        7001 to IllegalStateException::class,
        7002 to IllegalStateException::class,
        7003 to UnsupportedOperationException::class,
    )

    /** Resolves a registered code, falling back to a generic runtime exception type. */
    fun exceptionType(code: String): KClass<out Throwable> =
        codeToException[code] ?: IllegalStateException::class

    /** Resolves a numeric code, falling back to a generic runtime exception type. */
    fun exceptionType(code: Int): KClass<out Throwable> =
        intCodeToException[code] ?: IllegalStateException::class

    /** Returns whether the code is explicitly registered in the shared catalog. */
    fun contains(code: String): Boolean = codeToException.containsKey(code)

    /** Returns whether the numeric code is explicitly registered in the shared catalog. */
    fun contains(code: Int): Boolean = intCodeToException.containsKey(code)
}
