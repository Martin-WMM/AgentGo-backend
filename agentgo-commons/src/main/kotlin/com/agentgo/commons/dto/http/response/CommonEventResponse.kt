package com.agentgo.commons.dto.http.response

import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant
import java.util.UUID

/**
 * Common payload for a server-sent event (SSE).
 *
 * [namespace] groups related events, while [finished] marks the terminal event in a stream.
 *
 * @param E type of the optional event payload
 * @property eventId unique event identifier, generated as a UUID by default
 * @property eventName event name exposed to SSE consumers
 * @property namespace group used to distinguish related event streams
 * @property timestamp instant at which the event was published, in UTC
 * @property data optional event payload
 * @property finished whether this event ends its stream
 * @property requestId identifier of the request that produced the event
 * @property eventType type of data carried by the event
 */
@Schema(description = "Common server-sent event payload")
data class CommonEventResponse<E>(
    @field:Schema(description = "Unique event identifier", format = "uuid", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6", requiredMode = Schema.RequiredMode.REQUIRED)
    val eventId: String = UUID.randomUUID().toString(),
    @field:Schema(description = "Event name exposed to SSE consumers", example = "progress", requiredMode = Schema.RequiredMode.REQUIRED)
    val eventName: String,
    @field:Schema(description = "Namespace used to group related events", example = "agent.execution", requiredMode = Schema.RequiredMode.REQUIRED)
    val namespace: String,
    @field:Schema(description = "UTC instant at which the event was published", format = "date-time", example = "2026-10-03T08:30:00Z", requiredMode = Schema.RequiredMode.REQUIRED)
    val timestamp: Instant = Instant.now(),
    @field:Schema(description = "Optional event payload")
    val data: E? = null,
    @field:Schema(description = "Whether this event ends its stream", example = "false", requiredMode = Schema.RequiredMode.REQUIRED)
    val finished: Boolean = false,
    @field:Schema(description = "Identifier of the request that produced the event", format = "uuid", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6", requiredMode = Schema.RequiredMode.REQUIRED)
    val requestId: String,
    @field:Schema(description = "Type of data carried by the event", example = "agent.status", requiredMode = Schema.RequiredMode.REQUIRED)
    val eventType: String,
)
