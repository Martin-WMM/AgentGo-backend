package com.agentgo.prompts.controller

import com.agentgo.commons.annotation.AgentGoLogApi
import com.agentgo.commons.dto.http.request.PageRequest
import com.agentgo.commons.dto.http.response.CommonHttpResponse
import com.agentgo.commons.dto.http.response.PageResponse
import com.agentgo.commons.dto.http.response.status.success.PromptsResponseCodeRegistry
import com.agentgo.dto.prompts.PromptTemplateCreateRequest
import com.agentgo.dto.prompts.PromptTemplatePreviewRequest
import com.agentgo.dto.prompts.PromptTemplateRenderRequest
import com.agentgo.dto.prompts.PromptTemplateRenderResponse
import com.agentgo.dto.prompts.PromptTemplateResponse
import com.agentgo.dto.prompts.PromptTemplateUpdateRequest
import com.agentgo.prompts.controller.apidoc.PromptTemplateControllerApiDoc
import com.agentgo.prompts.service.PromptTemplateService
import com.agentgo.starter.i18n.AgentGoMessages
import java.util.UUID
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

/**
 * HTTP adapter for authenticated prompt-template operations.
 *
 * @property promptTemplateService user-scoped prompt-template application service
 * @property messages localized API message resolver
 */
@RestController
class PromptTemplateController(
    private val promptTemplateService: PromptTemplateService,
    private val messages: AgentGoMessages,
) : PromptTemplateControllerApiDoc {
    @AgentGoLogApi(name = "prompt-template.create")
    override fun create(request: PromptTemplateCreateRequest): CommonHttpResponse<PromptTemplateResponse> =
        CommonHttpResponse(
            code = PromptsResponseCodeRegistry.CREATED,
            responseType = PromptsResponseCodeRegistry.CREATED_TYPE,
            message = messages.get("prompt.created", defaultMessage = "Prompt template created"),
            data = promptTemplateService.create(request),
        )

    @AgentGoLogApi(name = "prompt-template.get")
    override fun get(id: UUID): CommonHttpResponse<PromptTemplateResponse> =
        CommonHttpResponse(
            code = PromptsResponseCodeRegistry.RETRIEVED,
            responseType = PromptsResponseCodeRegistry.RETRIEVED_TYPE,
            message = messages.get("prompt.retrieved", defaultMessage = "Prompt template retrieved"),
            data = promptTemplateService.get(id),
        )

    @AgentGoLogApi(name = "prompt-template.list")
    override fun list(request: PageRequest): CommonHttpResponse<PageResponse<PromptTemplateResponse>> =
        CommonHttpResponse(
            code = PromptsResponseCodeRegistry.LIST_RETRIEVED,
            responseType = PromptsResponseCodeRegistry.LIST_RETRIEVED_TYPE,
            message = messages.get("prompt.listed", defaultMessage = "Prompt templates listed"),
            data = promptTemplateService.list(request),
        )

    @AgentGoLogApi(name = "prompt-template.update")
    override fun update(
        id: UUID,
        request: PromptTemplateUpdateRequest,
    ): CommonHttpResponse<PromptTemplateResponse> =
        CommonHttpResponse(
            code = PromptsResponseCodeRegistry.UPDATED,
            responseType = PromptsResponseCodeRegistry.UPDATED_TYPE,
            message = messages.get("prompt.updated", defaultMessage = "Prompt template updated"),
            data = promptTemplateService.update(id, request),
        )

    @AgentGoLogApi(name = "prompt-template.delete")
    override fun delete(id: UUID): ResponseEntity<Void> {
        promptTemplateService.delete(id)
        return ResponseEntity.noContent().build()
    }

    @AgentGoLogApi(name = "prompt-template.preview")
    override fun preview(
        request: PromptTemplatePreviewRequest,
    ): CommonHttpResponse<PromptTemplateRenderResponse> =
        rendered(promptTemplateService.preview(request))

    @AgentGoLogApi(name = "prompt-template.render")
    override fun render(
        id: UUID,
        request: PromptTemplateRenderRequest,
    ): CommonHttpResponse<PromptTemplateRenderResponse> =
        rendered(promptTemplateService.render(id, request))

    private fun rendered(data: PromptTemplateRenderResponse): CommonHttpResponse<PromptTemplateRenderResponse> =
        CommonHttpResponse(
            code = PromptsResponseCodeRegistry.RENDERED,
            responseType = PromptsResponseCodeRegistry.RENDERED_TYPE,
            message = messages.get("prompt.rendered", defaultMessage = "Prompt template rendered"),
            data = data,
        )
}
