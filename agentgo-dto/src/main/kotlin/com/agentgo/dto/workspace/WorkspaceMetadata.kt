package com.agentgo.dto.workspace

import io.swagger.v3.oas.annotations.media.Schema

/**
 * Presentation metadata stored with a file workspace.
 *
 * Mutable JavaBean-style properties are required so Spring Boot 4 / Jackson 3 can
 * deserialize the JSON body without a Jackson Kotlin module on the HTTP mapper.
 * The same shape is persisted as JSON.
 *
 * @property color optional accent from [WorkspaceColors.palette]
 */
@Schema(description = "Presentation metadata for a file workspace. Additional keys may be added later.")
class WorkspaceMetadata {
    @field:WorkspaceColor
    @field:Schema(
        description = "Accent color from the supported palette. Omit it to keep the default #1D4ED8 on create, " +
            "or to leave the stored color unchanged when metadata itself is omitted on update.",
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

    override fun equals(other: Any?): Boolean = other is WorkspaceMetadata && color == other.color

    override fun hashCode(): Int = color?.hashCode() ?: 0

    override fun toString(): String = "WorkspaceMetadata(color=$color)"
}
