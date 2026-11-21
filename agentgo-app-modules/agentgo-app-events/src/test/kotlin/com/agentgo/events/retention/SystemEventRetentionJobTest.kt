package com.agentgo.events.retention

import com.agentgo.events.service.SystemEventService
import kotlin.test.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class SystemEventRetentionJobTest {
    @Test
    fun `scheduled purge delegates to the service`() {
        val service = mock<SystemEventService>()

        SystemEventRetentionJob(service).purgeExpiredEvents()

        verify(service).enforceRetention()
    }
}
