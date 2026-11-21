package com.agentgo.models.config

import com.agentgo.commons.dto.http.response.status.error.ExceptionResponseCodeRegistry
import com.agentgo.models.controller.ModelController
import com.agentgo.models.controller.ModelExceptionHandler
import com.agentgo.models.exception.DuplicateModelNameException
import com.agentgo.models.exception.InvalidModelRequestException
import com.agentgo.models.exception.ModelNotFoundException
import com.agentgo.models.mapper.ModelMapperImpl
import com.agentgo.models.repository.ModelRepository
import com.agentgo.models.security.AuthenticatedUserIdResolver
import com.agentgo.models.service.impl.ModelServiceImpl
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.persistence.autoconfigure.EntityScan
import org.springframework.context.annotation.Import
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

/**
 * Auto-configuration entry point for the user-owned model management module.
 *
 * Registers persistence scanning, HTTP adapters, and stable exception-to-response-code
 * mappings used by the shared exception registry.
 */
@AutoConfiguration
@EntityScan("com.agentgo.models.entity")
@EnableJpaRepositories(basePackageClasses = [ModelRepository::class])
@Import(
    ModelController::class,
    ModelExceptionHandler::class,
    ModelMapperImpl::class,
    ModelServiceImpl::class,
    AuthenticatedUserIdResolver::class,
)
class ModelsAutoConfiguration {
    init {
        ExceptionResponseCodeRegistry.register<ModelNotFoundException>(8001, "MODEL-001")
        ExceptionResponseCodeRegistry.register<InvalidModelRequestException>(8002, "MODEL-002")
        ExceptionResponseCodeRegistry.register<DuplicateModelNameException>(8003, "MODEL-003")
    }
}
