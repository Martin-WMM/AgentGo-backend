package com.agentgo.prompts.service

import com.agentgo.commons.dto.http.request.PageRequest
import com.agentgo.commons.dto.http.response.PageResponse
import com.agentgo.dto.prompts.PromptTemplateCreateRequest
import com.agentgo.dto.prompts.PromptTemplatePreviewRequest
import com.agentgo.dto.prompts.PromptTemplateRenderRequest
import com.agentgo.dto.prompts.PromptTemplateRenderResponse
import com.agentgo.dto.prompts.PromptTemplateResponse
import com.agentgo.dto.prompts.PromptTemplateUpdateRequest
import java.util.UUID

/**
 * Application service for authenticated, user-scoped prompt-template management.
 *
 * Implementations must resolve ownership exclusively from the security context and
 * must never trust a client-supplied user identifier.
 */
interface PromptTemplateService {
    /**
     * Creates a prompt template for the authenticated user.
     *
     * @param request create payload
     * @return persisted template response
     */
    fun create(request: PromptTemplateCreateRequest): PromptTemplateResponse

    /**
     * Retrieves one template owned by the authenticated user.
     *
     * @param id template identifier
     * @return template response
     */
    fun get(id: UUID): PromptTemplateResponse

    /**
     * Lists one page of templates owned by the authenticated user.
     *
     * When [request] does not name a sort field, the page is ordered by `updatedAt`
     * descending. Supported sort fields are `name`, `engine`, `createdAt`, and `updatedAt`.
     *
     * @param request one-based page, size, sort fields, and direction
     * @return the requested page of template responses
     */
    fun list(request: PageRequest): PageResponse<PromptTemplateResponse>

    /**
     * Replaces fields of a template owned by the authenticated user.
     *
     * @param id template identifier
     * @param request update payload
     * @return updated template response
     */
    fun update(id: UUID, request: PromptTemplateUpdateRequest): PromptTemplateResponse

    /**
     * Deletes a template owned by the authenticated user.
     *
     * @param id template identifier
     */
    fun delete(id: UUID)

    /**
     * Renders an unsaved template.
     *
     * @param request preview payload
     * @return rendered text
     */
    fun preview(request: PromptTemplatePreviewRequest): PromptTemplateRenderResponse

    /**
     * Renders a stored template owned by the authenticated user.
     *
     * @param id template identifier
     * @param request render variables
     * @return rendered text
     */
    fun render(id: UUID, request: PromptTemplateRenderRequest): PromptTemplateRenderResponse
}
