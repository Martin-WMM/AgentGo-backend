package com.agentgo.starter

import com.agentgo.starter.annotation.AgentGoLogApi
import java.lang.reflect.Proxy
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.reflect.MethodSignature
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class AgentGoLogApiAspectTest {
    private val aspect = AgentGoLogApiAspect()

    @Test
    fun returnsSuccessfulInvocationResult() {
        val annotation = fixtureAnnotation("custom-operation")

        assertEquals("result", aspect.logApiInvocation(joinPoint(throws = false), annotation))
    }

    @Test
    fun propagatesInvocationFailure() {
        assertFailsWith<IllegalStateException> {
            aspect.logApiInvocation(joinPoint(throws = true), fixtureAnnotation(""))
        }
    }

    private fun fixtureAnnotation(name: String): AgentGoLogApi = Proxy.newProxyInstance(
        javaClass.classLoader,
        arrayOf(AgentGoLogApi::class.java),
    ) { _, method, _ ->
        when (method.name) {
            "name" -> name
            "annotationType" -> AgentGoLogApi::class.java
            else -> null
        }
    } as AgentGoLogApi

    private fun joinPoint(throws: Boolean): ProceedingJoinPoint {
        val signature = Proxy.newProxyInstance(
            javaClass.classLoader,
            arrayOf(MethodSignature::class.java),
        ) { _, method, _ ->
            when (method.name) {
                "getMethod" -> LogApiFixture::class.java.getDeclaredMethod("defaultName")
                else -> null
            }
        } as MethodSignature

        return Proxy.newProxyInstance(
            javaClass.classLoader,
            arrayOf(ProceedingJoinPoint::class.java),
        ) { _, method, _ ->
            when (method.name) {
                "getSignature" -> signature
                "getArgs" -> arrayOf("input")
                "proceed" -> if (throws) throw IllegalStateException("failure") else "result"
                else -> null
            }
        } as ProceedingJoinPoint
    }

    private class LogApiFixture {
        fun defaultName() = Unit
    }
}
