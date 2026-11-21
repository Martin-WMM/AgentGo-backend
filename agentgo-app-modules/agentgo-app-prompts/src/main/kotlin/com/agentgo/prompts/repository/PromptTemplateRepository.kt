package com.agentgo.prompts.repository

import com.agentgo.prompts.entity.PromptTemplateEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

/**
 * Persistence access for [PromptTemplateEntity] rows.
 *
 * Query methods intentionally require an owner identifier so callers cannot load
 * another user's templates by id alone.
 */
interface PromptTemplateRepository : JpaRepository<PromptTemplateEntity, Long> {
    /**
     * Finds a template owned by [ownerUserId].
     *
     * @param id template identifier
     * @param ownerUserId authenticated owner
     * @return matching entity, or `null` when absent
     */
    fun findByIdAndOwnerUserId(id: Long, ownerUserId: String): PromptTemplateEntity?

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
     * Returns whether [creator] already has [name] inside [groupName].
     *
     * @param groupName template group
     * @param name candidate template name
     * @param creator authenticated creator stored on the row
     * @return `true` when a conflicting row exists
     */
    fun existsByGroupNameAndNameAndCreator(
        groupName: String,
        name: String,
        creator: String,
    ): Boolean

    /**
     * Returns whether [creator] already has another [name] inside [groupName].
     *
     * @param groupName template group
     * @param name candidate template name
     * @param creator authenticated creator stored on the row
     * @param id template identifier to exclude from the uniqueness check
     * @return `true` when a conflicting row exists
     */
    fun existsByGroupNameAndNameAndCreatorAndIdNot(
        groupName: String,
        name: String,
        creator: String,
        id: Long,
    ): Boolean
}
