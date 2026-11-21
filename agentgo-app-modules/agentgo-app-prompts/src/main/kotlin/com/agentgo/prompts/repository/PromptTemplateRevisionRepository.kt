package com.agentgo.prompts.repository

import com.agentgo.prompts.entity.PromptTemplateRevisionEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

/**
 * Persistence access for git-style prompt template commits.
 */
interface PromptTemplateRevisionRepository : JpaRepository<PromptTemplateRevisionEntity, Long> {
    /**
     * Returns one page of revisions for [templateId].
     *
     * Callers order the page by `revisionNumber` descending so the log reads newest first.
     *
     * @param templateId owning template identifier
     * @param pageable zero-based page, size, and sort
     * @return a page of matching revisions
     */
    fun findByTemplateId(templateId: Long, pageable: Pageable): Page<PromptTemplateRevisionEntity>

    /**
     * Finds one numbered revision of a template.
     *
     * @param templateId owning template identifier
     * @param revisionNumber one-based revision number
     * @return matching revision, or `null` when absent
     */
    fun findByTemplateIdAndRevisionNumber(
        templateId: Long,
        revisionNumber: Int,
    ): PromptTemplateRevisionEntity?

    /**
     * Deletes every revision that belongs to [templateId].
     *
     * @param templateId owning template identifier
     */
    fun deleteByTemplateId(templateId: Long)
}
