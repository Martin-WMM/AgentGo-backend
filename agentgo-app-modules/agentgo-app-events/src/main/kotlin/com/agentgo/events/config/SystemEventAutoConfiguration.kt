package com.agentgo.events.config

import com.agentgo.commons.dto.http.response.status.error.ExceptionResponseCodeRegistry
import com.agentgo.events.aspect.RecordSystemEventAspect
import com.agentgo.events.controller.SystemEventController
import com.agentgo.events.controller.SystemEventExceptionHandler
import com.agentgo.events.exception.InvalidSystemEventRequestException
import com.agentgo.events.exception.SystemEventNotFoundException
import com.agentgo.events.mapper.SystemEventMapperImpl
import com.agentgo.events.repository.SystemEventRepository
import com.agentgo.events.retention.SystemEventRetentionJob
import com.agentgo.events.security.AuthenticatedUserIdResolver
import com.agentgo.events.service.impl.SystemEventRecorderImpl
import com.agentgo.events.service.impl.SystemEventServiceImpl
import com.agentgo.events.support.SystemEventSpelEvaluator
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.persistence.autoconfigure.EntityScan
import org.springframework.context.annotation.Import
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.scheduling.annotation.EnableScheduling

/**
 * Auto-configuration entry point for the global system-event log.
 *
 * Registers persistence scanning, the HTTP adapter, retention scheduling,
 * `@RecordSystemEvent` monitoring, and stable exception-to-response-code mappings.
 */
@AutoConfiguration
@EnableScheduling
@EntityScan("com.agentgo.events.entity")
@EnableJpaRepositories(basePackageClasses = [SystemEventRepository::class])
@Import(
    SystemEventController::class,
    SystemEventExceptionHandler::class,
    SystemEventMapperImpl::class,
    SystemEventServiceImpl::class,
    SystemEventRecorderImpl::class,
    SystemEventSpelEvaluator::class,
    RecordSystemEventAspect::class,
    AuthenticatedUserIdResolver::class,
    SystemEventSchedulingConfiguration::class,
    SystemEventRetentionJob::class,
)
class SystemEventAutoConfiguration {
    init {
        ExceptionResponseCodeRegistry.register<SystemEventNotFoundException>(9701, "EVENT-001")
        ExceptionResponseCodeRegistry.register<InvalidSystemEventRequestException>(9702, "EVENT-002")
    }
}
