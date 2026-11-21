package com.agentgo.events.mapper

import com.agentgo.dto.events.SystemEventCreateRequest
import com.agentgo.dto.events.SystemEventResponse
import com.agentgo.dto.events.SystemEventUpdateRequest
import com.agentgo.events.entity.SystemEventEntity
import java.time.Instant
import org.springframework.stereotype.Component

/**
 * Default [SystemEventMapper] implementation used by the system-event service.
 */
@Component("systemEventMapperImpl")
class SystemEventMapperImpl : SystemEventMapper {
    override fun toResponse(entity: SystemEventEntity): SystemEventResponse =
        SystemEventResponse(
            id = requireNotNull(entity.id) { "Persisted system event must have an identifier." },
            name = entity.name,
            domain = entity.domain,
            `operator` = entity.operatorName,
            comment = entity.comment,
            occurredAt = entity.occurredAt,
            createdAt = requireNotNull(entity.createdAt) { "Persisted system event must have a creation timestamp." },
            updatedAt = requireNotNull(entity.updatedAt) { "Persisted system event must have an update timestamp." },
        )

    override fun toEntity(actor: String, request: SystemEventCreateRequest, now: Instant): SystemEventEntity =
        SystemEventEntity(
            name = request.name.trim(),
            domain = request.domain.trim(),
            operatorName = operatorOf(actor, request.`operator`),
            comment = commentOf(request.comment),
            occurredAt = request.occurredAt ?: now,
        )

    override fun applyUpdate(entity: SystemEventEntity, actor: String, request: SystemEventUpdateRequest) {
        entity.name = request.name.trim()
        entity.domain = request.domain.trim()
        entity.operatorName = operatorOf(actor, request.`operator`)
        entity.comment = commentOf(request.comment)
        request.occurredAt?.let { entity.occurredAt = it }
    }

    private fun operatorOf(actor: String, requested: String?): String {
        val trimmed = requested?.trim().orEmpty()
        return trimmed.ifEmpty { actor }
    }

    private fun commentOf(comment: String?): String? = comment?.trim()?.ifEmpty { null }
}
