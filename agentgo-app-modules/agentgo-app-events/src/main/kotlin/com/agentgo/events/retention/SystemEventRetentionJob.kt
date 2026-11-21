package com.agentgo.events.retention

import com.agentgo.events.service.SystemEventService
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

/**
 * Periodic cleanup for system events that age out between writes.
 *
 * Successful creates and updates already trim the log. This job covers the case
 * where time passes without a new event.
 *
 * @property systemEventService event application service
 */
@Component
class SystemEventRetentionJob(
    private val systemEventService: SystemEventService,
) {
    /**
     * Deletes events older than three months and trims the log to 1,000 rows.
     */
    @Scheduled(
        fixedDelay = 3_600_000,
        initialDelay = 60_000,
        scheduler = "systemEventTaskScheduler",
    )
    fun purgeExpiredEvents() {
        systemEventService.enforceRetention()
    }
}
