package com.agentgo.prompts.revision

import com.agentgo.dto.prompts.PromptEngine
import com.agentgo.dto.prompts.PromptTemplateMetadata

/**
 * Fields compared and stored for one prompt template commit.
 *
 * @property groupName group name
 * @property name template name
 * @property engine rendering engine
 * @property content template body
 * @property description optional description
 * @property metadata presentation color and icon
 * @property tags display tags
 */
class PromptSnapshot(
    val groupName: String,
    val name: String,
    val engine: PromptEngine,
    val content: String,
    val description: String?,
    val metadata: PromptTemplateMetadata,
    val tags: List<String>,
) {
    /**
     * Returns whether this snapshot and [other] would produce an empty commit.
     *
     * @param other snapshot to compare
     * @return `true` when the metadata text and content are unchanged
     */
    fun sameAs(other: PromptSnapshot): Boolean =
        metadataText() == other.metadataText() && content == other.content

    /**
     * Returns the stable metadata file used by the unified diff.
     *
     * @return one field per line, without a trailing newline
     */
    fun metadataText(): String = listOf(
        "groupName: $groupName",
        "name: $name",
        "engine: ${engine.name}",
        "description: ${description.orEmpty()}",
        "color: ${metadata.color.orEmpty()}",
        "icon.source: ${metadata.icon?.source?.name.orEmpty()}",
        "icon.value: ${metadata.icon?.value.orEmpty()}",
        "tags: ${tags.joinToString(", ")}",
    ).joinToString("\n")
}
