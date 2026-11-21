package com.agentgo.prompts.revision

/**
 * Renders the two-file diff recorded on a prompt template commit.
 *
 * `metadata` carries group, name, engine, description, color, icon, and tags.
 * `content` carries the template body. Unchanged files are omitted.
 */
object PromptTemplateCommitDiff {
    /**
     * Builds the commit diff from [before] to [after].
     *
     * A null [before] is the initial commit and both files are new.
     *
     * @param before parent snapshot, or null for the first commit
     * @param after snapshot stored by this commit
     * @return unified diff text
     */
    fun render(before: PromptSnapshot?, after: PromptSnapshot): String {
        val metadata = UnifiedDiff.diff(
            before?.metadataText().orEmpty(),
            after.metadataText(),
            "metadata",
        )
        val content = UnifiedDiff.diff(before?.content.orEmpty(), after.content, "content")
        return listOfNotNull(metadata, content).joinToString("\n")
    }
}
