package com.agentgo.dto.prompts

import io.swagger.v3.oas.annotations.media.Schema

/**
 * Engines that can render a stored prompt template.
 *
 * [SIMPLE] replaces `{{ identifier }}` placeholders. [JINJA2] evaluates a restricted Jinja2 template.
 */
@Schema(
    description = "Engine used to render a prompt template. SIMPLE replaces only {{ identifier }} " +
        "placeholders, where identifier matches [A-Za-z_][A-Za-z0-9_]* and whitespace inside the " +
        "braces is allowed. Expressions, filters, conditions, and loops are rejected. " +
        "JINJA2 renders a restricted Jinja2 template with conditions and loops. include, import, " +
        "from, and extends are rejected, and external templates cannot be loaded.",
    allowableValues = ["SIMPLE", "JINJA2"],
)
enum class PromptEngine {
    /** Placeholder replacement of `{{ identifier }}` tokens. */
    SIMPLE,

    /** Restricted Jinja2 rendering. */
    JINJA2,
}
