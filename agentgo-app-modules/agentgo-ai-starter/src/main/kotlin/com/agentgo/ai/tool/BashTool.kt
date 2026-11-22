package com.agentgo.ai.tool

import com.agentgo.core.tool.ToolKind
import com.agentgo.core.tool.ToolSpec

/**
 * Catalog entry for the Bash / CLI execution tool exposed to the agent ReAct loop.
 *
 * Execution is delegated to [com.agentgo.cli.service.CliExecutionService]
 * (the same service behind `POST /api/v1/cli/executions`).
 */
object BashTool {
    /** Stable tool name used in model decisions and allow-list checks. */
    const val NAME: String = "bash"

    /** Argument key for the one-shot command / script text. */
    const val ARG_SCRIPT: String = "script"

    /** Optional JSON-object argument of environment variables. */
    const val ARG_ENVIRONMENT: String = "environment"

    /**
     * Optional Python source. When set, AgentGo writes a temporary `.py` file and
     * runs it with `python3` (or `python`) so multi-line scripts do not need shell
     * redirection operators.
     */
    const val ARG_CODE: String = "code"

    /** Alias for [ARG_CODE]. */
    const val ARG_SOURCE: String = "source"

    /** Model-visible tool specification. */
    val SPEC: ToolSpec =
        ToolSpec(
            name = NAME,
            description =
                "Run an allow-listed one-shot CLI command through AgentGo CLI execution. " +
                    "Pass arguments.script with the command text (for example \"echo hello\", " +
                    "\"date\", or \"python3\"). " +
                    "Prefer python3 with arguments.code for calculations and multi-step logic; " +
                    "use date only to print the current calendar date. " +
                    "To write and run a Python script, pass arguments.code (or arguments.source) " +
                    "with the full Python source; AgentGo saves it to a temp file and runs " +
                    "python3 on it. " +
                    "The process environment is injected from the agent runtime context " +
                    "(user, token, locale, thread, session, and metadata). " +
                    "Optional arguments.environment may add extra KEY=VALUE pairs; " +
                    "runtime context values always take precedence.",
            kind = ToolKind.CLI,
        )
}
