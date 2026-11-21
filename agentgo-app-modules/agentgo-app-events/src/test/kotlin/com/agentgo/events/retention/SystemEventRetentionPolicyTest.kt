package com.agentgo.events.retention

import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals

class SystemEventRetentionPolicyTest {
    @Test
    fun `cutoff is three utc months before now`() {
        val now = Instant.parse("2026-10-26T12:00:00Z")

        assertEquals(Instant.parse("2026-07-26T12:00:00Z"), SystemEventRetentionPolicy.cutoff(now))
        assertEquals(1000, SystemEventRetentionPolicy.MAX_RETAINED_EVENTS)
        assertEquals(3L, SystemEventRetentionPolicy.RETENTION_MONTHS)
    }
}
