package com.agentgo.dto.events

import com.agentgo.commons.validation.NullOrTrimmedSize
import com.agentgo.commons.validation.TrimmedNotBlank
import com.agentgo.commons.validation.TrimmedSize
import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant

/**
 * Request body used to replace a global system event.
 *
 * Mutable JavaBean-style properties are required so Spring Boot 4 / Jackson 3 can
 * deserialize the JSON body without a Jackson Kotlin module on the HTTP mapper.
 *
 * @property name human-readable event name
 * @property domain business domain that produced the event
 * @property operator person related to the event; blank uses the authenticated user
 * @property comment optional free-form comment
 * @property occurredAt when the event happened; omitted keeps the stored time
 */
@Schema(
    description = "Request body for PUT /api/v1/systemEvents/{id}. Replaces the event name, domain, " +
        "operator, and comment. A blank operator stores the authenticated user. An omitted occurredAt " +
        "keeps the stored time. The three-month and 1000-row retention window still applies.",
)
class SystemEventUpdateRequest {
    @field:TrimmedNotBlank
    @field:TrimmedSize(min = 1, max = 255)
    @field:Schema(
        description = "Human-readable event name",
        example = "Model renamed",
        requiredMode = Schema.RequiredMode.REQUIRED,
        maxLength = 255,
    )
    var name: String = ""

    @field:TrimmedNotBlank
    @field:TrimmedSize(min = 1, max = 255)
    @field:Schema(
        description = "Business domain that produced the event",
        example = "models",
        requiredMode = Schema.RequiredMode.REQUIRED,
        maxLength = 255,
    )
    var domain: String = ""

    @field:NullOrTrimmedSize(max = 255)
    @field:Schema(
        description = "Person related to the event. Blank stores the authenticated user.",
        example = "ada",
        maxLength = 255,
    )
    var `operator`: String? = null

    @field:NullOrTrimmedSize(max = 4000)
    @field:Schema(
        description = "Optional free-form comment. Blank clears the stored comment.",
        example = "Renamed the workspace default model.",
        maxLength = 4000,
    )
    var comment: String? = null

    @field:Schema(
        description = "When the event happened. Omitted keeps the stored time.",
        format = "date-time",
        example = "2026-10-26T12:00:00Z",
    )
    var occurredAt: Instant? = null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SystemEventUpdateRequest) return false
        return name == other.name &&
            domain == other.domain &&
            `operator` == other.`operator` &&
            comment == other.comment &&
            occurredAt == other.occurredAt
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + domain.hashCode()
        result = 31 * result + (`operator`?.hashCode() ?: 0)
        result = 31 * result + (comment?.hashCode() ?: 0)
        result = 31 * result + (occurredAt?.hashCode() ?: 0)
        return result
    }
}
