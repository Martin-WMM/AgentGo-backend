package com.agentgo.dto.prompts

import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant

/**
 * One git-style commit in a prompt template's history.
 *
 * @property id persistent revision identifier
 * @property templateId owning template identifier
 * @property version concrete decimal version label such as `1`
 * @property revision one-based revision number
 * @property parentRevision previous revision number, or null for the first commit
 * @property message commit comment
 * @property author authenticated principal that recorded the commit
 * @property diff unified diff of metadata and content
 * @property groupName group name after this commit
 * @property name template name after this commit
 * @property engine engine after this commit
 * @property content template body after this commit
 * @property description description after this commit
 * @property tags tags after this commit
 * @property metadata presentation metadata after this commit
 * @property createdAt commit timestamp
 */
@Schema(description = "One committed revision of a user-owned prompt template")
data class PromptTemplateRevisionResponse(
    @field:Schema(description = "Persistent revision identifier", format = "int64", requiredMode = Schema.RequiredMode.REQUIRED)
    val id: Long,
    @field:Schema(description = "Owning prompt template identifier", format = "int64", requiredMode = Schema.RequiredMode.REQUIRED)
    val templateId: Long,
    @field:Schema(description = "Concrete version label. latest is only an alias on the live template.", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    val version: String,
    @field:Schema(description = "One-based revision number", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    val revision: Int,
    @field:Schema(description = "Parent revision number. Null on the first commit.", example = "1")
    val parentRevision: Int? = null,
    @field:Schema(description = "Commit comment", example = "Initial commit", requiredMode = Schema.RequiredMode.REQUIRED)
    val message: String,
    @field:Schema(description = "Authenticated principal that recorded the commit", example = "ada", requiredMode = Schema.RequiredMode.REQUIRED)
    val author: String,
    @field:Schema(description = "Unified diff of the metadata file and the content file", requiredMode = Schema.RequiredMode.REQUIRED)
    val diff: String,
    @field:Schema(description = "Group name after this commit", example = "chat", requiredMode = Schema.RequiredMode.REQUIRED)
    val groupName: String,
    @field:Schema(description = "Template name after this commit", example = "Greeting", requiredMode = Schema.RequiredMode.REQUIRED)
    val name: String,
    @field:Schema(description = "Template engine after this commit", example = "SIMPLE", requiredMode = Schema.RequiredMode.REQUIRED)
    val engine: PromptEngine,
    @field:Schema(description = "Template body after this commit", requiredMode = Schema.RequiredMode.REQUIRED)
    val content: String,
    @field:Schema(description = "Description after this commit")
    val description: String? = null,
    @field:Schema(description = "Tags after this commit", requiredMode = Schema.RequiredMode.REQUIRED)
    val tags: List<String> = emptyList(),
    @field:Schema(description = "Presentation metadata after this commit", requiredMode = Schema.RequiredMode.REQUIRED)
    val metadata: PromptTemplateMetadata = PromptTemplateMetadata(),
    @field:Schema(description = "Commit timestamp", format = "date-time", requiredMode = Schema.RequiredMode.REQUIRED)
    val createdAt: Instant,
)
