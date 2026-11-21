package com.agentgo.events.config

import java.util.concurrent.ThreadPoolExecutor
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SystemEventSchedulingConfigurationTest {
    @Test
    fun `retention scheduler uses one named thread and the abort policy`() {
        val scheduler = SystemEventSchedulingConfiguration().systemEventTaskScheduler()
        scheduler.initialize()
        try {
            assertEquals(1, scheduler.scheduledThreadPoolExecutor.corePoolSize)
            assertEquals("agentgo-system-event-retention-", scheduler.threadNamePrefix)
            assertTrue(scheduler.scheduledThreadPoolExecutor.rejectedExecutionHandler is ThreadPoolExecutor.AbortPolicy)
        } finally {
            scheduler.shutdown()
        }
    }
}
