package com.agentgo.a2a.config

import com.agentgo.a2a.controller.A2aAgentController
import com.agentgo.a2a.controller.A2aAgentExceptionHandler
import com.agentgo.a2a.exception.A2aAgentNotFoundException
import com.agentgo.a2a.exception.DuplicateA2aAgentNameException
import com.agentgo.a2a.exception.InvalidA2aAgentRequestException
import com.agentgo.a2a.mapper.A2aAgentMapperImpl
import com.agentgo.a2a.repository.A2aAgentRepository
import com.agentgo.a2a.security.AuthenticatedUserIdResolver
import com.agentgo.a2a.service.A2aAgentRequestNormalizer
import com.agentgo.a2a.service.impl.A2aAgentServiceImpl
import com.agentgo.commons.dto.http.response.status.error.ExceptionResponseCodeRegistry
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.persistence.autoconfigure.EntityScan
import org.springframework.context.annotation.Import
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

/**
 * Auto-configuration entry point for the user-owned A2A agent module.
 */
@AutoConfiguration
@EntityScan("com.agentgo.a2a.entity")
@EnableJpaRepositories(basePackageClasses = [A2aAgentRepository::class])
@Import(
    A2aAgentController::class,
    A2aAgentExceptionHandler::class,
    A2aAgentMapperImpl::class,
    A2aAgentRequestNormalizer::class,
    A2aAgentServiceImpl::class,
    AuthenticatedUserIdResolver::class,
)
class A2aAgentsAutoConfiguration {
    init {
        ExceptionResponseCodeRegistry.register<A2aAgentNotFoundException>(9931, "A2A-001")
        ExceptionResponseCodeRegistry.register<InvalidA2aAgentRequestException>(9932, "A2A-002")
        ExceptionResponseCodeRegistry.register<DuplicateA2aAgentNameException>(9933, "A2A-003")
    }
}
