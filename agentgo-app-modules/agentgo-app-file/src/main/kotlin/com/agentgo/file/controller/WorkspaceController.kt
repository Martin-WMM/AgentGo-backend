package com.agentgo.file.controller

import com.agentgo.commons.annotation.AgentGoLogApi
import com.agentgo.commons.dto.http.response.CommonHttpResponse
import com.agentgo.commons.dto.http.response.status.success.WorkspaceResponseCodeRegistry
import com.agentgo.dto.workspace.WorkspaceCreateRequest
import com.agentgo.dto.workspace.WorkspaceResponse
import com.agentgo.dto.workspace.WorkspaceUpdateRequest
import com.agentgo.file.controller.apidoc.WorkspaceControllerApiDoc
import com.agentgo.file.service.WorkspaceService
import com.agentgo.starter.i18n.AgentGoMessages
import jakarta.validation.Valid
import java.util.UUID
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

/**
 * HTTP adapter for authenticated file-workspace CRUD operations.
 *
 * @property workspaceService user-scoped workspace application service
 * @property messages localized API message resolver
 */
@RestController
class WorkspaceController(
    private val workspaceService: WorkspaceService,
    private val messages: AgentGoMessages,
) : WorkspaceControllerApiDoc {
    @AgentGoLogApi(name = "workspace.create")
    override fun create(@Valid request: WorkspaceCreateRequest): CommonHttpResponse<WorkspaceResponse> =
        CommonHttpResponse(
            code = WorkspaceResponseCodeRegistry.CREATED,
            responseType = WorkspaceResponseCodeRegistry.CREATED_TYPE,
            message = messages.get("workspace.created", defaultMessage = "Workspace created"),
            data = workspaceService.create(request),
        )

    @AgentGoLogApi(name = "workspace.get")
    override fun get(id: UUID): CommonHttpResponse<WorkspaceResponse> =
        CommonHttpResponse(
            code = WorkspaceResponseCodeRegistry.RETRIEVED,
            responseType = WorkspaceResponseCodeRegistry.RETRIEVED_TYPE,
            message = messages.get("workspace.retrieved", defaultMessage = "Workspace retrieved"),
            data = workspaceService.get(id),
        )

    @AgentGoLogApi(name = "workspace.list")
    override fun list(): CommonHttpResponse<List<WorkspaceResponse>> =
        CommonHttpResponse(
            code = WorkspaceResponseCodeRegistry.LIST_RETRIEVED,
            responseType = WorkspaceResponseCodeRegistry.LIST_RETRIEVED_TYPE,
            message = messages.get("workspace.listed", defaultMessage = "Workspaces listed"),
            data = workspaceService.list(),
        )

    @AgentGoLogApi(name = "workspace.update")
    override fun update(id: UUID, @Valid request: WorkspaceUpdateRequest): CommonHttpResponse<WorkspaceResponse> =
        CommonHttpResponse(
            code = WorkspaceResponseCodeRegistry.UPDATED,
            responseType = WorkspaceResponseCodeRegistry.UPDATED_TYPE,
            message = messages.get("workspace.updated", defaultMessage = "Workspace updated"),
            data = workspaceService.update(id, request),
        )

    @AgentGoLogApi(name = "workspace.delete")
    override fun delete(id: UUID): ResponseEntity<Void> {
        workspaceService.delete(id)
        return ResponseEntity.noContent().build()
    }
}
