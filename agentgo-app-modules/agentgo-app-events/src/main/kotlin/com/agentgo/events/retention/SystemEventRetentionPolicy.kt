package com.agentgo.events.retention

import java.time.Instant
import java.time.ZoneOffset

/**
 * Retention limits for the global system-event log.
 *
 * The log keeps the newest [MAX_RETAINED_EVENTS] rows whose event time is still
 * inside the last [RETENTION_MONTHS] months. Older rows, and rows beyond that
 * count, are deleted.
 */
object SystemEventRetentionPolicy {
    /** Maximum number of events retained after expired rows are removed. */
    const val MAX_RETAINED_EVENTS = 1000

    /** Number of months an event may remain before it is discarded. */
    const val RETENTION_MONTHS = 3L

    /** Largest page accepted by the list endpoint. */
    const val MAX_PAGE_SIZE = 100

    /** Number of oldest rows removed in one trim batch. */
    const val PURGE_BATCH_SIZE = 500

    /**
     * Returns the oldest instant that is still inside the retention window.
     *
     * Events strictly before this instant are discarded.
     *
     * @param now current instant
     * @return exclusive cutoff
     */
    fun cutoff(now: Instant): Instant =
        now.atZone(ZoneOffset.UTC).minusMonths(RETENTION_MONTHS).toInstant()
}
