package com.agentgo.dto.prompts

/**
 * Version labels for a user-owned prompt template.
 *
 * The live row is always [LATEST]. Concrete commits in the revision history use
 * decimal labels such as `1` and `2`.
 */
object PromptTemplateVersions {
    /** Alias for the current head revision. */
    const val LATEST: String = "latest"

    /**
     * Returns the concrete version label for a revision number.
     *
     * @param revision one-based revision number
     * @return decimal version label
     */
    fun label(revision: Int): String = revision.toString()
}
