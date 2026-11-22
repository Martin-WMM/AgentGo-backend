package com.agentgo.cli.config

import kotlin.test.Test
import kotlin.test.assertNotNull

class CliAutoConfigurationTest {
    @Test
    fun `cli auto configuration can be constructed`() {
        assertNotNull(CliAutoConfiguration())
    }
}