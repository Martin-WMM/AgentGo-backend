package com.agentgo.events.service

import com.agentgo.commons.dto.http.request.PageRequest
import com.agentgo.commons.dto.http.response.PageResponse
import com.agentgo.dto.events.SystemEventCreateRequest
import com.agentgo.dto.events.SystemEventResponse
import com.agentgo.dto.events.SystemEventUpdateRequest

/**
 * Application service for the global system-event log.
 *
 * Events are visible to every authenticated user. Each successful write also
 * discards events older than three months and trims the table to the newest
 * 1,000 rows. [enforceRetention] performs that same cleanup on a schedule so
 * rows age out even when nobody writes.
 */
interface SystemEventService {
    /**
     * Records one event and applies retention.
     *
     * @param request create payload
     * @return stored event
     * @throws com.agentgo.events.exception.SystemEventException when authentication is missing
     * @throws com.agentgo.events.exception.InvalidSystemEventRequestException when the event
     * falls outside the retention window
     */
    fun create(request: SystemEventCreateRequest): SystemEventResponse

    /**
     * Returns one event by id.
     *
     * @param id event identifier
     * @return stored event
     * @throws com.agentgo.events.exception.SystemEventNotFoundException when the id is unknown
     */
    fun get(id: Long): SystemEventResponse

    /**
     * Returns one page of events.
     *
     * Blank [domain] and [operatorName] filters are ignored. When [PageRequest.sort] is
     * omitted, events are ordered by `occurredAt` descending.
     *
     * @param request one-based page, size, and sort
     * @param domain optional exact domain filter
     * @param operatorName optional exact operator filter
     * @return a page of matching events
     * @throws com.agentgo.events.exception.InvalidSystemEventRequestException when the page
     * size or sort field is not supported
     */
    fun list(
        request: PageRequest,
        domain: String?,
        operatorName: String?,
    ): PageResponse<SystemEventResponse>

    /**
     * Replaces the business fields of one event and applies retention.
     *
     * @param id event identifier
     * @param request replacement payload
     * @return updated event
     * @throws com.agentgo.events.exception.SystemEventNotFoundException when the id is unknown
     * @throws com.agentgo.events.exception.InvalidSystemEventRequestException when the updated
     * event falls outside the retention window
     */
    fun update(id: Long, request: SystemEventUpdateRequest): SystemEventResponse

    /**
     * Deletes one event.
     *
     * @param id event identifier
     * @throws com.agentgo.events.exception.SystemEventNotFoundException when the id is unknown
     */
    fun delete(id: Long)

    /**
     * Deletes events older than three months, then deletes the oldest rows beyond 1,000.
     */
    fun enforceRetention()
}
