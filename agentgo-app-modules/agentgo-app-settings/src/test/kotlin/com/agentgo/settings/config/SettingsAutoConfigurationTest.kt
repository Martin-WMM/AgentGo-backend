package com.agentgo.settings.config

import kotlin.test.Test
import kotlin.test.assertNotNull

class SettingsAutoConfigurationTest {
    @Test
    fun `settings auto configuration can be constructed`() {
        assertNotNull(SettingsAutoConfiguration())
    }
}
