package com.agentgo.dto.presentation

import io.swagger.v3.oas.annotations.media.Schema

/**
 * Icon reference shared by user-facing resources.
 *
 * Mutable JavaBean-style properties are required so Spring Boot 4 / Jackson 3 can
 * deserialize the JSON body without a Jackson Kotlin module on the HTTP mapper.
 * The same shape is persisted inside resource metadata JSON.
 *
 * @property source icon provider
 * @property value Iconify id or absolute image URL, depending on [source]
 */
@Schema(description = "Reusable icon. source selects Iconify or an absolute image URL.")
class ResourceIcon {
    @field:Schema(
        description = "ICONIFY stores an Iconify id. URL stores an absolute http or https image.",
        example = "ICONIFY",
        requiredMode = Schema.RequiredMode.REQUIRED,
    )
    var source: IconSource? = null

    @field:Schema(
        description = "Iconify id such as lucide:bot, or an absolute http(s) URL.",
        example = "lucide:bot",
        requiredMode = Schema.RequiredMode.REQUIRED,
        maxLength = ResourceIcons.URL_VALUE_MAX_LENGTH,
    )
    var value: String = ""

    /**
     * Returns a detached copy so persistence and request beans do not share one instance.
     *
     * @return icon with the same source and value
     */
    fun duplicate(): ResourceIcon = ResourceIcon().apply {
        source = this@ResourceIcon.source
        value = this@ResourceIcon.value
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ResourceIcon) return false
        return source == other.source && value == other.value
    }

    override fun hashCode(): Int {
        var result = source?.hashCode() ?: 0
        result = 31 * result + value.hashCode()
        return result
    }

    override fun toString(): String = "ResourceIcon(source=$source, value=$value)"
}
