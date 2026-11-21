package com.agentgo.dto.prompts

import com.agentgo.dto.presentation.ResourceIcon
import com.agentgo.dto.presentation.ValidResourceIcon
import com.agentgo.dto.workspace.WorkspaceColor
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.Valid

/**
 * Presentation metadata stored with a prompt template.
 *
 * Mutable JavaBean-style properties are required so Spring Boot 4 / Jackson 3 can
 * deserialize the JSON body without a Jackson Kotlin module on the HTTP mapper.
 * The same shape is persisted as JSON. Additional keys may be added later.
 *
 * @property color optional accent from the shared workspace palette
 * @property icon optional Iconify id or image URL
 */
@Schema(description = "Presentation metadata for a prompt template. color is a palette accent. icon is optional.")
class PromptTemplateMetadata {
    @field:WorkspaceColor
    @field:Schema(
        description = "Accent color from the shared palette. Omit it to use #1D4ED8.",
        example = "#1D4ED8",
        allowableValues = [
            "#1D4ED8",
            "#0E7490",
            "#0F766E",
            "#15803D",
            "#B45309",
            "#C2410C",
            "#B91C1C",
            "#BE185D",
            "#6D28D9",
            "#4338CA",
        ],
    )
    var color: String? = null

    @field:Valid
    @field:ValidResourceIcon
    @field:Schema(description = "Optional icon. Omit it when the template has no icon.")
    var icon: ResourceIcon? = null

    /**
     * Returns a detached copy so a request and a stored row do not share one instance.
     *
     * @return metadata with the same color and a duplicated icon
     */
    fun duplicate(): PromptTemplateMetadata = PromptTemplateMetadata().apply {
        color = this@PromptTemplateMetadata.color
        icon = this@PromptTemplateMetadata.icon?.duplicate()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PromptTemplateMetadata) return false
        return color == other.color && icon == other.icon
    }

    override fun hashCode(): Int {
        var result = color?.hashCode() ?: 0
        result = 31 * result + (icon?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String = "PromptTemplateMetadata(color=$color, icon=$icon)"
}
