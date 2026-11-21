package com.agentgo.dto.events

import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant

/**
 * API representation of one global system event.
 *
 * @property id persistent event identifier
 * @property name human-readable event name
 * @property domain business domain that produced the event
 * @property operator person related to the event
 * @property comment optional free-form comment
 * @property occurredAt when the event happened
 * @property createdAt row creation timestamp
 * @property updatedAt row last modification timestamp
 */
@Schema(description = "Global system event returned by GET, POST, and PUT /api/v1/systemEvents")
data class SystemEventResponse(
    @field:Schema(
        description = "Persistent system event identifier",
        format = "int64",
        requiredMode = Schema.RequiredMode.REQUIRED,
    )
    val id: Long,
    @field:Schema(
        description = "Human-readable event name",
        example = "Model created",
        requiredMode = Schema.RequiredMode.REQUIRED,
    )
    val name: String,
    @field:Schema(
        description = "Business domain that produced the event",
        example = "models",
        requiredMode = Schema.RequiredMode.REQUIRED,
    )
    val domain: String,
    @field:Schema(
        description = "Person related to the event",
        example = "ada",
        requiredMode = Schema.RequiredMode.REQUIRED,
    )
    val `operator`: String,
    @field:Schema(description = "Optional free-form comment")
    val comment: String? = null,
    @field:Schema(
        description = "When the event happened",
        format = "date-time",
        requiredMode = Schema.RequiredMode.REQUIRED,
    )
    val occurredAt: Instant,
    @field:Schema(
        description = "Row creation timestamp",
        format = "date-time",
        requiredMode = Schema.RequiredMode.REQUIRED,
    )
    val createdAt: Instant,
    @field:Schema(
        description = "Row last modification timestamp",
        format = "date-time",
        requiredMode = Schema.RequiredMode.REQUIRED,
    )
    val updatedAt: Instant,
)
