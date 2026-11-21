package com.agentgo.prompts.revision

import com.agentgo.dto.prompts.PromptTemplateRevisionResponse
import com.agentgo.dto.prompts.PromptTemplateVersions
import com.agentgo.prompts.entity.PromptTemplateRevisionEntity

/**
 * Maps a stored prompt revision onto its API response.
 */
object PromptTemplateRevisionMapper {
    /**
     * Converts a persisted revision into an API response.
     *
     * @param entity stored revision
     * @return response DTO
     * @throws IllegalArgumentException when required generated fields are missing
     */
    fun toResponse(entity: PromptTemplateRevisionEntity): PromptTemplateRevisionResponse =
        PromptTemplateRevisionResponse(
            id = requireNotNull(entity.id) { "Persisted prompt template revision must have an identifier." },
            templateId = requireNotNull(entity.templateId) { "Persisted prompt template revision must have a template." },
            version = PromptTemplateVersions.label(entity.revisionNumber),
            revision = entity.revisionNumber,
            parentRevision = entity.parentRevisionNumber,
            message = entity.message,
            author = entity.author,
            diff = entity.diff,
            groupName = entity.groupName,
            name = entity.name,
            engine = entity.engine,
            content = entity.content,
            description = entity.description,
            tags = entity.tags.toList(),
            metadata = entity.metadata.duplicate(),
            createdAt = requireNotNull(entity.createdAt) { "Persisted prompt template revision must have a creation timestamp." },
        )

    /**
     * Builds an unsaved revision for [templateId].
     *
     * @param templateId owning template identifier
     * @param revisionNumber one-based revision number
     * @param parentRevision previous revision number, or null for the first commit
     * @param message commit comment
     * @param author authenticated principal
     * @param diff unified diff
     * @param snapshot tree stored by this commit
     * @return unsaved revision entity
     */
    fun toEntity(
        templateId: Long,
        revisionNumber: Int,
        parentRevision: Int?,
        message: String,
        author: String,
        diff: String,
        snapshot: PromptSnapshot,
    ): PromptTemplateRevisionEntity = PromptTemplateRevisionEntity(
        templateId = templateId,
        revisionNumber = revisionNumber,
        parentRevisionNumber = parentRevision,
        message = message,
        author = author,
        diff = diff,
        groupName = snapshot.groupName,
        name = snapshot.name,
        engine = snapshot.engine,
        content = snapshot.content,
        description = snapshot.description,
        tags = snapshot.tags.toMutableList(),
        metadata = snapshot.metadata.duplicate(),
    )
}
