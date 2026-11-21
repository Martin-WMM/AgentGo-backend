package com.agentgo.prompts.render

/**
 * Shared limits for prompt-template text and variable payloads.
 */
object PromptTemplateLimits {
    /** Maximum stored template name length. */
    const val NAME_MAX_LENGTH: Int = 255

    /** Maximum stored group name length. */
    const val GROUP_NAME_MAX_LENGTH: Int = 255

    /** Maximum stored description length. */
    const val DESCRIPTION_MAX_LENGTH: Int = 2000

    /** Maximum stored commit comment length. */
    const val COMMIT_MESSAGE_MAX_LENGTH: Int = 2000

    /** Commit comment used when create omits one. */
    const val INITIAL_COMMIT_MESSAGE: String = "Initial commit"

    /** Maximum number of tags stored on one template. */
    const val TAG_MAX_COUNT: Int = 20

    /** Maximum length of one tag. */
    const val TAG_MAX_LENGTH: Int = 32

    /** Maximum stored template body length. */
    const val CONTENT_MAX_LENGTH: Int = 100_000

    /** Maximum rendered output length. */
    const val OUTPUT_MAX_LENGTH: Int = 200_000

    /** Maximum number of entries in one variable object or array. */
    const val VARIABLE_COLLECTION_MAX_SIZE: Int = 200

    /** Maximum nesting depth of a variable value. */
    const val VARIABLE_MAX_DEPTH: Int = 8

    /** Maximum length of one string variable. */
    const val VARIABLE_STRING_MAX_LENGTH: Int = 100_000
}
