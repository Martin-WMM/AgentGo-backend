package com.agentgo.events.service

import java.time.Instant

/**
 * Fail-open writer used by business code and [com.agentgo.events.annotation.RecordSystemEvent].
 *
 * Prefer this over calling [SystemEventService.create] from domain services: HTTP
 * clients still use the service directly and receive validation errors, while
 * automatic monitoring must never roll back a successful business write.
 */
interface SystemEventRecorder {
    /**
     * Persists one system event. Failures are logged and swallowed.
     *
     * @param name human-readable event name
     * @param domain business domain
     * @param comment optional free-form comment
     * @param operator optional related person; blank uses the authenticated user
     * @param occurredAt optional event time; blank uses the current time
     */
    fun record(
        name: String,
        domain: String,
        comment: String? = null,
        operator: String? = null,
        occurredAt: Instant? = null,
    )
}
