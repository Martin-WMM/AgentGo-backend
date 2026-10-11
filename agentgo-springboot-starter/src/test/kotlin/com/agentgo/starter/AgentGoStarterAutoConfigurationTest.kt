package com.agentgo.starter

import com.agentgo.commons.annotation.AgentGoLogApi
import com.agentgo.starter.config.AgentGoStarterAutoConfiguration
import com.agentgo.starter.properties.AgentGoOpenApiProperties
import com.agentgo.starter.properties.AgentGoStarterProperties
import java.time.LocalDateTime
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
    fun `uses defaults when optional open api fields are blank`() {
        val defaults = AgentGoOpenApiProperties()
        val openApi = AgentGoStarterAutoConfiguration().agentGoOpenAPI(defaults)

        assertEquals("AgentGo API", defaults.title)
        assertEquals("AgentGo backend service API", defaults.description)
        assertEquals("v1", defaults.version)
        assertEquals("", defaults.termsOfServiceUrl)
        assertEquals("Martin", defaults.contactName)
        assertEquals("https://github.com/Martin-WMM/AgentGo-backend", defaults.contactUrl)
        assertEquals("", defaults.contactEmail)
        assertEquals("Apache License 2.0", defaults.licenseName)
        assertEquals("https://www.apache.org/licenses/LICENSE-2.0", defaults.licenseUrl)
        assertEquals("", defaults.serverUrl)
        assertEquals("", defaults.serverDescription)
        assertEquals(null, openApi.info.termsOfService)
        assertTrue(openApi.servers.isNullOrEmpty())
    }

    @Test
    fun `uses the server url when the server description is blank`() {
        val openApi = AgentGoStarterAutoConfiguration().agentGoOpenAPI(
            AgentGoOpenApiProperties(serverUrl = "https://api.example.com", serverDescription = " "),
        )

        assertEquals("https://api.example.com", openApi.servers.first().description)
    }

    @Test
    fun createsObjectMapperBean() {
        val objectMapper = AgentGoStarterAutoConfiguration().agentGoObjectMapper()

        assertNotNull(objectMapper)
        assertEquals("\"2026-10-03T12:30:00\"", objectMapper.writeValueAsString(LocalDateTime.of(2026, 10, 3, 12, 30)))
    }

    @Test
    fun usesBlankNameAsMethodNameByDefault() {
        val annotation = LogApiFixture::class.java.getDeclaredMethod("defaultName").getAnnotation(AgentGoLogApi::class.java)

        assertEquals("", annotation.name)
    }

    private class LogApiFixture {
        @AgentGoLogApi
        fun defaultName() = Unit
    }
}
