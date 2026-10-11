package com.agentgo.dto.workspace

import com.agentgo.commons.validation.NullOrTrimmedSize
import com.agentgo.commons.validation.TrimmedNotBlank
import com.agentgo.commons.validation.TrimmedSize
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.Valid

/**
 * Request body used to create a user-owned file workspace.
 *
 * Mutable JavaBean-style properties are required so Spring Boot 4 / Jackson 3 can
 * deserialize the JSON body without a Jackson Kotlin module on the HTTP mapper.
 */
@Schema(
    description = "Request body for POST /api/v1/workspaces. Creates a file workspace owned by " +
        "the authenticated user. Do not send an owner or user id.",
)
class WorkspaceCreateRequest {
    @field:TrimmedNotBlank
    @field:TrimmedSize(min = 1, max = 255)
    @field:Schema(
        description = "Human-readable workspace name unique for the authenticated user",
        example = "Reports",
        requiredMode = Schema.RequiredMode.REQUIRED,
        maxLength = 255,
    )
    var name: String = ""

    @field:NullOrTrimmedSize(max = 2000)
    @field:Schema(
        description = "Optional free-form description shown in management UIs",
        example = "Quarterly report drafts",
        maxLength = 2000,
    )
    var description: String? = null

    @field:Valid
    @field:Schema(description = "Optional presentation metadata. color selects the workspace accent.")
    var metadata: WorkspaceMetadata? = null
}
