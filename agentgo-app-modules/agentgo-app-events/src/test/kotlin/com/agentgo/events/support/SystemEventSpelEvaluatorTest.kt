package com.agentgo.events.support

import java.lang.reflect.Proxy
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.reflect.MethodSignature
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SystemEventSpelEvaluatorTest {
    private val evaluator = SystemEventSpelEvaluator()

    @Test
    fun `renderTemplate keeps plain text and interpolates placeholders`() {
        val context = evaluator.context(
            joinPoint(
                SampleService::class.java.getDeclaredMethod("create", SampleRequest::class.java),
                arrayOf(SampleRequest("alpha")),
            ),
            result = SampleResult("alpha", "id-1"),
            error = null,
        )

        assertEquals(
            "Created model alpha (id-1)",
            evaluator.renderTemplate("Created model #{#result.name} (#{#result.id})", context),
        )
        assertEquals("Model created", evaluator.renderTemplate("Model created", context))
        assertNull(evaluator.renderTemplate("   ", context))
    }

    @Test
    fun `matches treats blank condition as true and evaluates SpEL booleans`() {
        val context = evaluator.context(
            joinPoint(
                SampleService::class.java.getDeclaredMethod("create", SampleRequest::class.java),
                arrayOf(SampleRequest("alpha")),
            ),
            result = SampleResult("alpha", "id-1"),
            error = null,
        )

        assertTrue(evaluator.matches("", context))
        assertTrue(evaluator.matches("#result != null", context))
        assertFalse(evaluator.matches("#result == null", context))
    }

    @Test
    fun `context exposes named parameters result and error`() {
        val error = IllegalStateException("boom")
        val context = evaluator.context(
            joinPoint(
                SampleService::class.java.getDeclaredMethod("delete", String::class.java),
                arrayOf("resource-1"),
            ),
            result = null,
            error = error,
        )

        assertEquals("resource-1", evaluator.renderTemplate("#{ #id }", context))
        assertEquals("boom", evaluator.renderTemplate("#{ #error.message }", context))
    }

    private fun joinPoint(
        method: java.lang.reflect.Method,
        args: Array<Any?>,
    ): ProceedingJoinPoint {
        val signature = Proxy.newProxyInstance(
            javaClass.classLoader,
            arrayOf(MethodSignature::class.java),
        ) { _, invoked, _ ->
            when (invoked.name) {
                "getMethod" -> method
                "getName" -> method.name
                "getDeclaringTypeName" -> SampleService::class.java.name
                else -> null
            }
        } as MethodSignature

        return Proxy.newProxyInstance(
            javaClass.classLoader,
            arrayOf(ProceedingJoinPoint::class.java),
        ) { _, invoked, _ ->
            when (invoked.name) {
                "getSignature" -> signature
                "getArgs" -> args
                "getTarget" -> SampleService()
                else -> null
            }
        } as ProceedingJoinPoint
    }

    private class SampleService {
        fun create(request: SampleRequest): SampleResult = SampleResult(request.name, "id-1")
        fun delete(id: String) = Unit
    }

    private data class SampleRequest(val name: String)

    private data class SampleResult(val name: String, val id: String)
}
