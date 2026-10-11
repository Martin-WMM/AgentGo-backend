package com.agentgo.dto.workspace

import com.agentgo.commons.validation.NullOrTrimmedSize
import com.agentgo.commons.validation.TrimmedNotBlank
import com.agentgo.commons.validation.TrimmedSize
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.Valid

/**
 * Request body used to replace fields of an existing user-owned file workspace.
 *
 * Mutable JavaBean-style properties are required so Spring Boot 4 / Jackson 3 can
 * deserialize the JSON body without a Jackson Kotlin module on the HTTP mapper.
 */
@Schema(description = "Request body for updating a user-owned file workspace")
class WorkspaceUpdateRequest {
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
        description = "Optional free-form description of the workspace",
        example = "Quarterly report drafts",
        maxLength = 2000,
    )
    var description: String? = null

    @field:Valid
    @field:Schema(
        description = "Optional presentation metadata. Send metadata.color to change the accent. " +
            "Omit metadata to leave the stored accent unchanged.",
    )
    var metadata: WorkspaceMetadata? = null
}
