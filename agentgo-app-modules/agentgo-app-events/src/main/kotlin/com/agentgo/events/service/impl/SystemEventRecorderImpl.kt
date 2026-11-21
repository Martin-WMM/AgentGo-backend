package com.agentgo.events.service.impl

import com.agentgo.dto.events.SystemEventCreateRequest
import com.agentgo.events.service.SystemEventRecorder
import com.agentgo.events.service.SystemEventService
import java.time.Instant
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * Default [SystemEventRecorder] that delegates to [SystemEventService] and never
 * propagates recording failures to the caller.
 */
@Component("systemEventRecorderImpl")
class SystemEventRecorderImpl(
    private val systemEventService: SystemEventService,
) : SystemEventRecorder {
    private val logger = LoggerFactory.getLogger(SystemEventRecorderImpl::class.java)

    override fun record(
        name: String,
        domain: String,
        comment: String?,
        operator: String?,
        occurredAt: Instant?,
    ) {
        val trimmedName = name.trim()
        val trimmedDomain = domain.trim()
        if (trimmedName.isEmpty() || trimmedDomain.isEmpty()) {
            logger.warn(
                "Skipping system event with blank name or domain (nameLength={}, domainLength={})",
                trimmedName.length,
                trimmedDomain.length,
            )
            return
        }
        try {
            systemEventService.create(
                SystemEventCreateRequest().apply {
                    this.name = trimmedName.take(NAME_MAX)
                    this.domain = trimmedDomain.take(DOMAIN_MAX)
                    this.`operator` = operator?.trim()?.takeIf { it.isNotEmpty() }?.take(OPERATOR_MAX)
                    this.comment = comment?.trim()?.takeIf { it.isNotEmpty() }?.take(COMMENT_MAX)
                    this.occurredAt = occurredAt
                },
            )
        } catch (exception: Exception) {
            logger.warn(
                "Failed to record system event domain='{}' name='{}': {}",
                trimmedDomain,
                trimmedName,
                exception.message,
            )
        }
    }

    private companion object {
        const val NAME_MAX = 255
        const val DOMAIN_MAX = 255
        const val OPERATOR_MAX = 255
        const val COMMENT_MAX = 4000
    }
}
