package com.agentgo.starter

import com.agentgo.starter.properties.AgentGoOpenApiProperties
import com.agentgo.starter.properties.AgentGoStarterProperties
import com.agentgo.starter.config.AgentGoStarterAutoConfiguration
import java.lang.reflect.Proxy
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.Signature
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AgentGoStarterAutoConfigurationTest {
    @Test
    fun createsClockBean() {
        assertNotNull(AgentGoStarterAutoConfiguration().agentGoClock())
    }

    @Test
    fun enablesStarterByDefault() {
        assertTrue(AgentGoStarterProperties().enabled)
    }

    @Test
    fun createsConfiguredOpenApiMetadata() {
        val openApi = AgentGoStarterAutoConfiguration().agentGoOpenAPI(
            AgentGoOpenApiProperties(
                title = "Test API",
                version = "2026.1",
                termsOfServiceUrl = "https://example.com/terms",
                serverUrl = "https://api.example.com",
                serverDescription = "Example API",
            ),
        )

        assertEquals("Test API", openApi.info.title)
        assertEquals("2026.1", openApi.info.version)
        assertNotNull(openApi.info.contact)
        assertNotNull(openApi.info.license)
        assertEquals("https://example.com/terms", openApi.info.termsOfService)
        assertEquals("https://api.example.com", openApi.servers.first().url)
        assertEquals("Example API", openApi.servers.first().description)
    }

    @Test
    fun createsObjectMapperBean() {
        assertNotNull(AgentGoStarterAutoConfiguration().agentGoObjectMapper().findAndRegisterModules())
    }

    @Test
    fun logsSuccessfulInvocationAndRedactsSensitiveArguments() {
        val joinPoint = joinPoint(throws = false)

        assertEquals("result", com.agentgo.starter.LogAspect().logInvocation(joinPoint))
    }

    @Test
    fun logsAndRethrowsInvocationFailure() {
        val joinPoint = joinPoint(throws = true)

        kotlin.test.assertFailsWith<IllegalStateException> {
            com.agentgo.starter.LogAspect().logInvocation(joinPoint)
        }
    }

    private fun joinPoint(throws: Boolean): ProceedingJoinPoint {
        val signature = Proxy.newProxyInstance(
            javaClass.classLoader,
            arrayOf(Signature::class.java),
        ) { _, method, _ ->
            if (method.name == "toShortString") "test.operation()" else null
        } as Signature

        return Proxy.newProxyInstance(
            javaClass.classLoader,
            arrayOf(ProceedingJoinPoint::class.java),
        ) { _, method, _ ->
            when (method.name) {
                "getSignature" -> signature
                "getArgs" -> arrayOf("password=secret", "x".repeat(513))
                "proceed" -> if (throws) throw IllegalStateException("failure") else "result"
                else -> null
            }
        } as ProceedingJoinPoint
    }
}
