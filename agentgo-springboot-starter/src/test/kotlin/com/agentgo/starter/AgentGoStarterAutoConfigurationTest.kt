package com.agentgo.starter

import com.agentgo.starter.properties.AgentGoOpenApiProperties
import com.agentgo.starter.properties.AgentGoStarterProperties
import com.agentgo.starter.config.AgentGoStarterAutoConfiguration
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
            AgentGoOpenApiProperties(title = "Test API", version = "2026.1"),
        )

        assertEquals("Test API", openApi.info.title)
        assertEquals("2026.1", openApi.info.version)
        assertNotNull(openApi.info.contact)
        assertNotNull(openApi.info.license)
    }
}
