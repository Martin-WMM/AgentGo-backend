package com.agentgo.commons.dto.http.response.status.error

import com.agentgo.commons.dto.http.response.status.ResponseCodeRegistry
import kotlin.reflect.KClass

/**
 * Thread-safe registry of exception types and their canonical error response statuses.
 *
 * Modules register their dedicated exception classes during module initialization. Registration
 * is idempotent only when the existing and requested response statuses are identical.
 */
object ExceptionResponseCodeRegistry {
    private val registrations = mutableMapOf<KClass<out Throwable>, ExceptionResponseCode>()

    /** Immutable snapshot of registered exception type to response-status mappings. */
    val exceptionTypeToResponseCode: Map<KClass<out Throwable>, ExceptionResponseCode>
        get() = synchronized(registrations) { registrations.toMap() }

    /**
     * Registers a dedicated exception type with its numeric code and string response type.
     *
     * @throws IllegalArgumentException if [code] and [responseType] are not a registered pair,
     * or if the exception type was already registered with a different response status
     */
    fun register(
        exceptionType: KClass<out Throwable>,
        code: Int,
        responseType: String,
    ) {
        require(ResponseCodeRegistry.responseType(code) == responseType) {
            "Response code $code must correspond to registered response type $responseType."
        }
        val responseCode = ExceptionResponseCode(code, responseType)
        synchronized(registrations) {
            val existing = registrations[exceptionType]
            require(existing == null || existing == responseCode) {
                "Exception type ${exceptionType.qualifiedName} is already registered with $existing."
            }
            registrations[exceptionType] = responseCode
        }
    }

    /** Registers an exception type using a reified Kotlin type argument. */
    inline fun <reified T : Throwable> register(code: Int, responseType: String) =
        register(T::class, code, responseType)

    /** Returns the registered status for an exception type, or `null` when it is not registered. */
    fun responseCode(exceptionType: KClass<out Throwable>): ExceptionResponseCode? =
        synchronized(registrations) { registrations[exceptionType] }
}
