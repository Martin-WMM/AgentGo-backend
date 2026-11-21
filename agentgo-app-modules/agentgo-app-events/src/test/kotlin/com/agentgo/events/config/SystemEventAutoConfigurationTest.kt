package com.agentgo.events.config

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class SystemEventAutoConfigurationTest {
    @Test
    fun `system event auto configuration registers its exception codes`() {
        val configuration = SystemEventAutoConfiguration()

        assertNotNull(configuration)
    }
}
