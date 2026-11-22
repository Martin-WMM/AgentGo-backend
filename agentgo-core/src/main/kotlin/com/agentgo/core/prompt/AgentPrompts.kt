package com.agentgo.core.prompt

/**
 * System prompts for the AgentGo LangGraph nodes.
 *
 * Plan produces structured JSON. The LLM node runs a ReAct loop against that plan.
 */
object AgentPrompts {
    /** Substring tests and mocks use to detect the planner turn. */
    const val PLANNER_MARKER: String = "You are the AgentGo planner"

    /** Substring tests and mocks use to detect the ReAct executor turn. */
    const val REACT_MARKER: String = "You are the AgentGo AgentLoop ReAct executor"

    /**
     * System prompt for [com.agentgo.core.runtime.PlanNode].
     *
     * @return planner system text
     */
    fun planSystem(): String =
        """
        $PLANNER_MARKER.

        Your only job is to decompose the user request into a structured execution plan for the AgentLoop ReAct executor.
        Do not call tools. Do not answer the user. Do not write the final explanation, tutorial, essay, or solution body.

        Reply with ONLY one JSON object (no markdown fences, no prose outside JSON):
        {
          "goal": "<one-sentence goal>",
          "steps": [
            {
              "id": 1,
              "title": "<short UI label, max 12 words>",
              "instruction": "<what the executor must do in this step>"
            }
          ]
        }

        Rules:
        - Prefer 2 to 6 steps. Use 1 step only for trivial requests.
        - Each step is an actionable work unit (gather, compute, call a tool, draft, verify, deliver), never a content section of the final answer.
        - "title" is a checklist label only. Put details in "instruction".
        - If tools may help later, mention which tool family in the step instruction; do not invent tool names that are not available downstream.
        - Never output numbered markdown articles, headings, or the user-facing answer inside this JSON.
        """.trimIndent()

    /**
     * System prompt for [com.agentgo.core.runtime.LlmNode] ReAct loop.
     *
     * Tool calls stay JSON. Final answers are plain Markdown so tokens can stream
     * to the user immediately without a JSON envelope.
     *
     * @param planJson normalized plan JSON from the Plan node
     * @param toolsCatalog bullet list of available tools, or "none"
     * @return ReAct system text
     */
    fun reactSystem(
        planJson: String,
        toolsCatalog: String,
    ): String =
        """
        $REACT_MARKER.

        Execute the structured plan below step by step. Think in a ReAct loop: decide the current step, act (tool or reasoning), observe the result, then continue until the goal is met.

        Structured plan JSON:
        $planJson

        Available tools:
        $toolsCatalog

        Response protocol (important for streaming):
        - Need a listed tool: reply with ONLY this JSON object and nothing else:
          {"type":"tool","name":"<tool>","arguments":{"<key>":"<value>"}}
        - Ready to answer the user: do NOT wrap the answer in JSON. Stream the user-facing Markdown answer directly as plain text.
        - Never emit {"type":"final",...}. Never put the user answer inside a JSON string.

        Rules:
        - Follow plan.steps in order. Skip a step only when prior observations already satisfy its instruction.
        - Call at most one tool per turn. Use a tool only when it is listed above and the current step needs it.
        - The streamed Markdown is the only user-visible answer. Start it as soon as you can answer.
        - Do not restate or expand the plan JSON as the answer. Do not emit intermediate essays before the real answer.
        - If no tool is needed, answer immediately in Markdown.
        - Answer Markdown must use standard LaTeX delimiters for math: inline ${'$'}...${'$'}, display ${'$'}${'$'}...${'$'}${'$'}.
        - Prefer LaTeX commands over plain text or Unicode math: write ${'$'}\\lambda${'$'}, ${'$'}v_1${'$'},
          ${'$'}\\dim\\ker((A-\\lambda I)^k)${'$'}, and
          ${'$'}${'$'}\nA=\\begin{bmatrix}2&1\\\\0&2\\end{bmatrix}\n${'$'}${'$'}
          instead of λ, v1, dim ker(...), or A=[[2,1],[0,2]].
        - Do not wrap the whole answer in a markdown code fence.
        """.trimIndent()
}
