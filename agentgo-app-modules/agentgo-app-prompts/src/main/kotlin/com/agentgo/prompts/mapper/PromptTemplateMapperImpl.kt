package com.agentgo.prompts.mapper

import com.agentgo.dto.prompts.PromptTemplateCreateRequest
import com.agentgo.dto.prompts.PromptTemplateResponse
import com.agentgo.dto.prompts.PromptTemplateUpdateRequest
import com.agentgo.prompts.entity.PromptTemplateEntity
import org.springframework.stereotype.Component

/**
 * Default [PromptTemplateMapper] implementation used by the prompt-template service.
 */
@Component("promptTemplateMapperImpl")
class PromptTemplateMapperImpl : PromptTemplateMapper {
    override fun toResponse(entity: PromptTemplateEntity): PromptTemplateResponse =
        PromptTemplateResponse(
            id = requireNotNull(entity.id) { "Persisted prompt template must have an identifier." },
            name = entity.name,
            engine = entity.engine,
            content = entity.content,
            description = entity.description,
            createdAt = requireNotNull(entity.createdAt) { "Persisted prompt template must have a creation timestamp." },
            updatedAt = requireNotNull(entity.updatedAt) { "Persisted prompt template must have an update timestamp." },
        )

    override fun toEntity(ownerUserId: String, request: PromptTemplateCreateRequest): PromptTemplateEntity =
        PromptTemplateEntity(
            ownerUserId = ownerUserId,
            name = request.name,
            engine = requireNotNull(request.engine) { "Prompt template engine is required." },
            content = request.content,
            description = request.description,
        )

    override fun applyUpdate(entity: PromptTemplateEntity, request: PromptTemplateUpdateRequest) {
        entity.name = request.name
        entity.engine = requireNotNull(request.engine) { "Prompt template engine is required." }
        entity.content = request.content
        entity.description = request.description
    }
}
