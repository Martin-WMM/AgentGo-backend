package com.agentgo.events.aspect

import com.agentgo.events.annotation.RecordSystemEvent
import com.agentgo.events.domain.SystemEventDomains
import com.agentgo.events.service.SystemEventRecorder
import com.agentgo.events.support.SystemEventSpelEvaluator
import java.lang.reflect.Proxy
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.reflect.MethodSignature
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class RecordSystemEventAspectTest {
    private val recorder = mock<SystemEventRecorder>()
    private val aspect = RecordSystemEventAspect(recorder, SystemEventSpelEvaluator())

    @Test
    fun `records after a successful invocation`() {
        val result = SampleResult("alpha", "id-1")
        val returned = aspect.recordEvent(
            joinPoint(throws = false, result = result),
            annotation(
                name = "Model created",
                comment = "Created model #{#result.name} (#{#result.id})",
            ),
        )

        assertEquals(result, returned)
        verify(recorder).record(
            name = eq("Model created"),
            domain = eq(SystemEventDomains.MODELS),
            comment = eq("Created model alpha (id-1)"),
            operator = eq(null),
            occurredAt = anyOrNull(),
        )
    }

    @Test
    fun `does not record when the method throws and recordOnFailure is false`() {
        assertFailsWith<IllegalStateException> {
            aspect.recordEvent(
                joinPoint(throws = true, result = null),
                annotation(name = "Model created"),
            )
        }

        verifyNoInteractions(recorder)
    }

    @Test
    fun `records failures when recordOnFailure is true`() {
        assertFailsWith<IllegalStateException> {
            aspect.recordEvent(
                joinPoint(throws = true, result = null),
                annotation(
                    name = "Model create failed",
                    comment = "Failure: #{#error.message}",
                    recordOnFailure = true,
                ),
            )
        }

        verify(recorder).record(
            name = eq("Model create failed"),
            domain = eq(SystemEventDomains.MODELS),
            comment = eq("Failure: boom"),
            operator = eq(null),
            occurredAt = anyOrNull(),
        )
    }

    @Test
    fun `skips recording when the condition is false`() {
        aspect.recordEvent(
            joinPoint(throws = false, result = SampleResult("alpha", "id-1")),
            annotation(name = "Model created", condition = "#result == null"),
        )

        verify(recorder, never()).record(any(), any(), anyOrNull(), anyOrNull(), anyOrNull())
    }

    @Test
    fun `skips recording when the rendered name is blank`() {
        aspect.recordEvent(
            joinPoint(throws = false, result = SampleResult("alpha", "id-1")),
            annotation(name = "   "),
        )

        verify(recorder, never()).record(any(), any(), anyOrNull(), anyOrNull(), anyOrNull())
    }

    @Test
    fun `swallows SpEL evaluation failures without changing the business result`() {
        val result = SampleResult("alpha", "id-1")
        val returned = aspect.recordEvent(
            joinPoint(throws = false, result = result),
            annotation(name = "#{ #result.name.definitelyNotAMethod() }"),
        )

        assertEquals(result, returned)
        verify(recorder, never()).record(any(), any(), anyOrNull(), anyOrNull(), anyOrNull())
    }

    @Test
    fun `records an explicit operator template when provided`() {
        aspect.recordEvent(
            joinPoint(throws = false, result = SampleResult("alpha", "id-1")),
            annotation(
                name = "Model created",
                operator = "#{ #request.name }",
            ),
        )

        verify(recorder).record(
            name = eq("Model created"),
            domain = eq(SystemEventDomains.MODELS),
            comment = eq(null),
            operator = eq("alpha"),
            occurredAt = anyOrNull(),
        )
    }

    private fun annotation(
        name: String,
        comment: String = "",
        operator: String = "",
        condition: String = "",
        recordOnFailure: Boolean = false,
    ): RecordSystemEvent = Proxy.newProxyInstance(
        javaClass.classLoader,
        arrayOf(RecordSystemEvent::class.java),
    ) { _, method, _ ->
        when (method.name) {
            "domain" -> SystemEventDomains.MODELS
            "name" -> name
            "comment" -> comment
            "operator" -> operator
            "condition" -> condition
            "recordOnFailure" -> recordOnFailure
            "annotationType" -> RecordSystemEvent::class.java
            else -> null
        }
    } as RecordSystemEvent

    private fun joinPoint(throws: Boolean, result: Any?): ProceedingJoinPoint {
        val method = SampleService::class.java.getDeclaredMethod("create", SampleRequest::class.java)
        val signature = Proxy.newProxyInstance(
            javaClass.classLoader,
            arrayOf(MethodSignature::class.java),
        ) { _, invoked, _ ->
            when (invoked.name) {
                "getMethod" -> method
                "getName" -> "create"
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
                "getArgs" -> arrayOf(SampleRequest("alpha"))
                "getTarget" -> SampleService()
                "proceed" -> if (throws) throw IllegalStateException("boom") else result
                else -> null
            }
        } as ProceedingJoinPoint
    }

    private class SampleService {
        fun create(request: SampleRequest): SampleResult = SampleResult(request.name, "id-1")
    }

    private data class SampleRequest(val name: String)

    private data class SampleResult(val name: String, val id: String)
}
