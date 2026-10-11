package com.agentgo.prompts.repository

import com.agentgo.prompts.entity.PromptTemplateEntity
import java.util.UUID
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

/**
 * Persistence access for [PromptTemplateEntity] rows.
 *
 * Query methods intentionally require an owner identifier so callers cannot load
 * another user's templates by id alone.
 */
interface PromptTemplateRepository : JpaRepository<PromptTemplateEntity, UUID> {
    /**
     * Finds a template owned by [ownerUserId].
     *
     * @param id template identifier
     * @param ownerUserId authenticated owner
     * @return matching entity, or `null` when absent
     */
    fun findByIdAndOwnerUserId(id: UUID, ownerUserId: String): PromptTemplateEntity?

    /**
     * Returns one page of templates owned by [ownerUserId].
     *
     * Ordering comes from [pageable]. Callers must only sort by supported fields.
     *
     * @param ownerUserId authenticated owner
     * @param pageable zero-based page, size, and sort
     * @return a page of matching template entities
     */
    fun findByOwnerUserId(ownerUserId: String, pageable: Pageable): Page<PromptTemplateEntity>

    /**
     * Returns whether the owner already has a template named [name].
     *
     * @param ownerUserId authenticated owner
     * @param name candidate template name
     * @return `true` when a conflicting row exists
     */
    fun existsByOwnerUserIdAndName(
        ownerUserId: String,
        name: String,
    ): Boolean

    /**
     * Returns whether the owner already has another template named [name].
     *
     * @param ownerUserId authenticated owner
     * @param name candidate template name
     * @param id template identifier to exclude from the uniqueness check
     * @return `true` when a conflicting row exists
     */
    fun existsByOwnerUserIdAndNameAndIdNot(
        ownerUserId: String,
        name: String,
        id: UUID,
    ): Boolean
}
