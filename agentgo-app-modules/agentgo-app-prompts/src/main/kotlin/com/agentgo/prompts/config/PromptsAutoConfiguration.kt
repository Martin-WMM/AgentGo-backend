package com.agentgo.prompts.config

import com.agentgo.commons.dto.http.response.status.error.ExceptionResponseCodeRegistry
import com.agentgo.prompts.controller.PromptTemplateController
import com.agentgo.prompts.controller.PromptTemplateExceptionHandler
import com.agentgo.prompts.exception.DuplicatePromptTemplateNameException
import com.agentgo.prompts.exception.InvalidPromptTemplateRequestException
import com.agentgo.prompts.exception.PromptTemplateNotFoundException
import com.agentgo.prompts.exception.PromptTemplateRenderException
import com.agentgo.prompts.exception.UnauthorizedPromptTemplateException
import com.agentgo.prompts.mapper.PromptTemplateMapperImpl
import com.agentgo.prompts.render.Jinja2PromptRenderer
import com.agentgo.prompts.render.PromptTemplateEngine
import com.agentgo.prompts.render.SimplePromptRenderer
import com.agentgo.prompts.repository.PromptTemplateRepository
import com.agentgo.prompts.security.AuthenticatedUserIdResolver
import com.agentgo.prompts.service.impl.PromptTemplateServiceImpl
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.persistence.autoconfigure.EntityScan
import org.springframework.context.annotation.Import
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

/**
 * Auto-configuration entry point for user-owned prompt-template management.
 *
 * Registers persistence scanning, HTTP adapters, template engines, and stable
 * exception-to-response-code mappings used by the shared exception registry.
 */
@AutoConfiguration
@EntityScan("com.agentgo.prompts.entity")
@EnableJpaRepositories(basePackageClasses = [PromptTemplateRepository::class])
@Import(
    PromptTemplateController::class,
    PromptTemplateExceptionHandler::class,
    PromptTemplateMapperImpl::class,
    PromptTemplateServiceImpl::class,
    AuthenticatedUserIdResolver::class,
    SimplePromptRenderer::class,
    Jinja2PromptRenderer::class,
    PromptTemplateEngine::class,
)
class PromptsAutoConfiguration {
    init {
        ExceptionResponseCodeRegistry.register<PromptTemplateNotFoundException>(9001, "PROMPT-001")
        ExceptionResponseCodeRegistry.register<InvalidPromptTemplateRequestException>(9002, "PROMPT-002")
        ExceptionResponseCodeRegistry.register<DuplicatePromptTemplateNameException>(9003, "PROMPT-003")
        ExceptionResponseCodeRegistry.register<PromptTemplateRenderException>(9004, "PROMPT-004")
        ExceptionResponseCodeRegistry.register<UnauthorizedPromptTemplateException>(9005, "PROMPT-005")
    }
}
