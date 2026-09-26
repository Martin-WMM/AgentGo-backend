package com.agentgo.starter

import kotlin.test.Test
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
}
