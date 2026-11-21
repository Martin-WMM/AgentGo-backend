package com.agentgo.dto.prompts

import com.agentgo.commons.validation.NullOrTrimmedSize
import com.agentgo.commons.validation.TrimmedNotBlank
import com.agentgo.commons.validation.TrimmedSize
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.Valid
import jakarta.validation.constraints.NotNull

/**
 * Request body used to replace fields of an existing user-owned prompt template.
 *
 * Mutable JavaBean-style properties are required so Spring Boot 4 / Jackson 3 can
 * deserialize the JSON body without a Jackson Kotlin module on the HTTP mapper.
 *
 * @property groupName group that scopes the template name
 * @property name display name unique with the group and creator
 * @property engine rendering engine
 * @property content template body
 * @property description optional free-form description
 * @property message required commit comment when the template changes
 * @property tags display tags that replace the stored list
 * @property metadata color and icon that replace the stored metadata
 */
@Schema(
    description = "Request body for PUT /api/v1/promptTemplates/{id}. Replaces groupName, name, " +
        "engine, content, description, tags, and metadata. message is the commit comment and is " +
        "required when the replacement differs from the stored template. Uniqueness is groupName + " +
        "name + creator. The body is syntax-checked again. The live version stays latest.",
)
class PromptTemplateUpdateRequest {
    @field:TrimmedNotBlank
    @field:TrimmedSize(min = 1, max = 255)
    @field:Schema(
        description = "Group name. Unique together with name and the creator.",
        example = "chat",
        requiredMode = Schema.RequiredMode.REQUIRED,
        maxLength = 255,
    )
    var groupName: String = ""

    @field:TrimmedNotBlank
    @field:TrimmedSize(min = 1, max = 255)
    @field:Schema(
        description = "Human-readable template name, unique with groupName and the creator",
        example = "Default system",
        requiredMode = Schema.RequiredMode.REQUIRED,
        maxLength = 255,
    )
    var name: String = ""

    @field:NotNull
    @field:Schema(
        description = "Template engine. SIMPLE replaces {{ identifier }} placeholders. JINJA2 renders a restricted Jinja2 template.",
        example = "SIMPLE",
        requiredMode = Schema.RequiredMode.REQUIRED,
    )
    var engine: PromptEngine? = null

    @field:TrimmedNotBlank
    @field:TrimmedSize(min = 1, max = 100000)
    @field:Schema(
        description = "Template body. SIMPLE templates use {{ identifier }} placeholders.",
        example = "You are helping {{ user }}.",
        requiredMode = Schema.RequiredMode.REQUIRED,
        maxLength = 100000,
    )
    var content: String = ""

    @field:NullOrTrimmedSize(max = 2000)
    @field:Schema(
        description = "Optional free-form description",
        example = "Default system prompt for chat",
        maxLength = 2000,
    )
    var description: String? = null

    @field:TrimmedNotBlank
    @field:TrimmedSize(max = 2000)
    @field:Schema(
        description = "Commit comment recorded with this change. Required when any field changes.",
        example = "Tighten the greeting",
        requiredMode = Schema.RequiredMode.REQUIRED,
        maxLength = 2000,
    )
    var message: String? = null

    @field:PromptTags
    @field:Schema(
        description = "Display tags that replace the stored list.",
        example = "[\"system\"]",
    )
    var tags: List<String>? = null

    @field:Valid
    @field:Schema(description = "Presentation metadata that replaces the stored color and icon.")
    var metadata: PromptTemplateMetadata? = null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PromptTemplateUpdateRequest) return false
        return groupName == other.groupName &&
            name == other.name &&
            engine == other.engine &&
            content == other.content &&
            description == other.description &&
            message == other.message &&
            tags == other.tags &&
            metadata == other.metadata
    }

    override fun hashCode(): Int {
        var result = groupName.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + (engine?.hashCode() ?: 0)
        result = 31 * result + content.hashCode()
        result = 31 * result + (description?.hashCode() ?: 0)
        result = 31 * result + (message?.hashCode() ?: 0)
        result = 31 * result + (tags?.hashCode() ?: 0)
        result = 31 * result + (metadata?.hashCode() ?: 0)
        return result
    }
}
