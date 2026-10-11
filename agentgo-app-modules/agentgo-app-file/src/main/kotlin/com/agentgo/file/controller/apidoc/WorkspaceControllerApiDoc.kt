package com.agentgo.file.controller.apidoc

import com.agentgo.commons.api.ApiPaths
import com.agentgo.commons.dto.http.response.CommonHttpResponse
import com.agentgo.dto.workspace.WorkspaceCreateRequest
import com.agentgo.dto.workspace.WorkspaceResponse
import com.agentgo.dto.workspace.WorkspaceUpdateRequest
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import java.util.UUID
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping

/**
 * Springdoc contract for authenticated file-workspace endpoints under `/api/v1/workspaces`.
 *
 * Ownership is resolved from the authentication context. Clients must not send a user id.
 */
@Tag(
    name = "Workspaces",
    description = "Versioned user-owned file workspace APIs at /api/v1/workspaces. " +
        "Every workspace belongs to the authenticated user; operations never cross owners. " +
        "name is unique per owner (HTTP 409 on conflict). metadata.color is an optional palette " +
        "accent; create defaults it to #1D4ED8, and update leaves the stored color unchanged when " +
        "metadata is omitted. name is at most 255 characters and description is optional and at " +
        "most 2000 characters. List returns newest updates first. Delete returns HTTP 204 and " +
        "fails with WORKSPACE-004 when the workspace still contains files. Success codes are " +
        "3200 through 3204. WORKSPACE-001 is not found, WORKSPACE-002 is an invalid request, " +
        "and WORKSPACE-003 is a duplicate name. Missing authentication uses AUTH-002.",
)
@RequestMapping(ApiPaths.V1_WORKSPACES)
interface WorkspaceControllerApiDoc {
    @Operation(
        summary = "Create a workspace for the authenticated user",
        description = "Stores a workspace owned by the caller. A missing metadata.color becomes #1D4ED8. " +
            "Returns 3201 WORKSPACE-CREATED. A duplicate name returns WORKSPACE-003.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Workspace created", useReturnTypeSchema = true),
            ApiResponse(responseCode = "400", description = "Invalid workspace request"),
            ApiResponse(responseCode = "401", description = "Authentication required"),
            ApiResponse(responseCode = "409", description = "Duplicate workspace name"),
        ],
    )
    @PostMapping
    fun create(@Valid @RequestBody request: WorkspaceCreateRequest): CommonHttpResponse<WorkspaceResponse>

    @Operation(
        summary = "Get one workspace owned by the authenticated user",
        description = "Returns one workspace when the id belongs to the caller. Another user's id " +
            "is not found. Returns 3202 WORKSPACE-RETRIEVED.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Workspace retrieved", useReturnTypeSchema = true),
            ApiResponse(responseCode = "401", description = "Authentication required"),
            ApiResponse(responseCode = "404", description = "Workspace not found"),
        ],
    )
    @GetMapping("/{id}")
    fun get(
        @Parameter(description = "Workspace identifier", `in` = ParameterIn.PATH, required = true)
        @PathVariable id: UUID,
    ): CommonHttpResponse<WorkspaceResponse>

    @Operation(
        summary = "List workspaces owned by the authenticated user",
        description = "Lists the caller's workspaces, newest update first. Returns 3200 WORKSPACE-LIST-RETRIEVED.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Workspaces listed", useReturnTypeSchema = true),
            ApiResponse(responseCode = "401", description = "Authentication required"),
        ],
    )
    @GetMapping
    fun list(): CommonHttpResponse<List<WorkspaceResponse>>

    @Operation(
        summary = "Replace a workspace owned by the authenticated user",
        description = "Replaces name and description. Omitting metadata leaves the stored color unchanged. " +
            "Sending metadata.color replaces the accent. Returns 3203 WORKSPACE-UPDATED.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Workspace updated", useReturnTypeSchema = true),
            ApiResponse(responseCode = "400", description = "Invalid workspace request"),
            ApiResponse(responseCode = "401", description = "Authentication required"),
            ApiResponse(responseCode = "404", description = "Workspace not found"),
            ApiResponse(responseCode = "409", description = "Duplicate workspace name"),
        ],
    )
    @PutMapping("/{id}")
    fun update(
        @Parameter(description = "Workspace identifier", `in` = ParameterIn.PATH, required = true)
        @PathVariable id: UUID,
        @Valid @RequestBody request: WorkspaceUpdateRequest,
    ): CommonHttpResponse<WorkspaceResponse>

    @Operation(
        summary = "Delete an empty workspace owned by the authenticated user",
        description = "Deletes the caller's workspace and returns HTTP 204 with an empty body. " +
            "WORKSPACE-004 rejects the delete while files remain. The success code 3204 " +
            "WORKSPACE-DELETED is registered but is not sent with this response.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "Workspace deleted"),
            ApiResponse(responseCode = "401", description = "Authentication required"),
            ApiResponse(responseCode = "404", description = "Workspace not found"),
            ApiResponse(responseCode = "409", description = "Workspace still contains files"),
        ],
    )
    @DeleteMapping("/{id}")
    fun delete(
        @Parameter(description = "Workspace identifier", `in` = ParameterIn.PATH, required = true)
        @PathVariable id: UUID,
    ): ResponseEntity<Void>
}
