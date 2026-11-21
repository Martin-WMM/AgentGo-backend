package com.agentgo.prompts.mapper

import com.agentgo.dto.presentation.ResourceIcons
import com.agentgo.dto.prompts.PromptTemplateCreateRequest
import com.agentgo.dto.prompts.PromptTemplateMetadata
import com.agentgo.dto.prompts.PromptTemplateResponse
import com.agentgo.dto.prompts.PromptTemplateUpdateRequest
import com.agentgo.dto.prompts.PromptTemplateVersions
import com.agentgo.dto.workspace.WorkspaceColors
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
            groupName = entity.groupName,
            name = entity.name,
            version = entity.version.ifBlank { PromptTemplateVersions.LATEST },
            revision = entity.headRevision,
            engine = entity.engine,
            content = entity.content,
            description = entity.description,
            tags = entity.tags.toList(),
            metadata = entity.metadata.duplicate(),
            createdAt = requireNotNull(entity.createdAt) { "Persisted prompt template must have a creation timestamp." },
            updatedAt = requireNotNull(entity.updatedAt) { "Persisted prompt template must have an update timestamp." },
        )

    override fun toEntity(ownerUserId: String, request: PromptTemplateCreateRequest): PromptTemplateEntity =
        PromptTemplateEntity(
            ownerUserId = ownerUserId,
            groupName = request.groupName.trim(),
            name = request.name.trim(),
            version = PromptTemplateVersions.LATEST,
            headRevision = 1,
            engine = checkNotNull(request.engine) { "Prompt template engine is required." },
            content = request.content.trim(),
            description = normalizeDescription(request.description),
            tags = normalizeTags(request.tags),
            metadata = normalizeMetadata(request.metadata),
        )

    override fun applyUpdate(entity: PromptTemplateEntity, request: PromptTemplateUpdateRequest) {
        entity.groupName = request.groupName.trim()
        entity.name = request.name.trim()
        entity.version = PromptTemplateVersions.LATEST
        entity.engine = checkNotNull(request.engine) { "Prompt template engine is required." }
        entity.content = request.content.trim()
        entity.description = normalizeDescription(request.description)
        entity.tags = normalizeTags(request.tags)
        entity.metadata = normalizeMetadata(request.metadata)
    }

    private fun normalizeDescription(description: String?): String? =
        description?.trim()?.takeIf { it.isNotEmpty() }

    private fun normalizeTags(tags: List<String>?): MutableList<String> {
        val normalized = linkedSetOf<String>()
        for (raw in tags.orEmpty()) {
            val tag = raw.trim()
            if (tag.isNotEmpty()) normalized += tag
        }
        return normalized.toMutableList()
    }

    private fun normalizeMetadata(metadata: PromptTemplateMetadata?): PromptTemplateMetadata {
        val color = metadata?.color?.let(WorkspaceColors::canonicalize) ?: WorkspaceColors.DEFAULT
        return PromptTemplateMetadata().apply {
            this.color = color
            icon = metadata?.icon?.let(ResourceIcons::canonicalize)
        }
    }
}
