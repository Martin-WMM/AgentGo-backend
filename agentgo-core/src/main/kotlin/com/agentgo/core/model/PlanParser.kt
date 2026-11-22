package com.agentgo.core.model

import com.agentgo.core.json.AgentJson
import com.fasterxml.jackson.databind.node.ObjectNode
import java.io.IOException

/**
 * Normalizes Plan-node model output into stable structured JSON.
 *
 * Accepts a JSON plan object, fenced JSON, or a short numbered/bulleted list.
 * Long prose that is not a plan is collapsed into a single deliverable step so
 * the UI checklist never becomes the final answer body.
 */
object PlanParser {
    private val fence: Regex = Regex("```(?:json)?\\s*([\\s\\S]*?)```", RegexOption.IGNORE_CASE)
    private val numberedLine: Regex = Regex("""^(?:[-*•]\s+|\d+[.)]\s+)(.+)$""")

    private const val DEFAULT_GOAL: String = "Answer the user request"
    private const val DEFAULT_TITLE: String = "Answer directly"
    private const val DEFAULT_INSTRUCTION: String =
        "Produce the final answer for the user request. Use a tool only when necessary."

    /**
     * Returns compact plan JSON suitable for state storage and the ReAct prompt.
     *
     * @param raw model text from the Plan node
     * @return normalized plan JSON
     */
    fun normalize(raw: String): String = AgentJson.mapper.writeValueAsString(toObjectNode(parse(raw)))

    /**
     * Parses [raw] into a [PlanDocument].
     *
     * @param raw model text from the Plan node
     * @return structured plan
     */
    fun parse(raw: String): PlanDocument {
        val trimmed = raw.trim()
        if (trimmed.isBlank()) {
            return defaultDocument()
        }
        extractJson(trimmed)?.let { payload ->
            fromJson(payload)?.let { return it }
        }
        fromList(trimmed)?.let { return it }
        val singleLine = trimmed.lines().map { it.trim() }.filter { it.isNotEmpty() }
        if (singleLine.size == 1 && singleLine[0].length <= 120) {
            val text = singleLine[0]
            return PlanDocument(
                goal = DEFAULT_GOAL,
                steps = listOf(PlanStep(id = 1, title = shorten(text), instruction = text)),
            )
        }
        return PlanDocument(
            goal = DEFAULT_GOAL,
            steps = listOf(
                PlanStep(
                    id = 1,
                    title = DEFAULT_TITLE,
                    instruction = "Answer the user request. Prior planner text was not a structured plan.",
                ),
            ),
        )
    }

    private fun defaultDocument(): PlanDocument =
        PlanDocument(
            goal = DEFAULT_GOAL,
            steps = listOf(PlanStep(1, DEFAULT_TITLE, DEFAULT_INSTRUCTION)),
        )

    private fun fromJson(payload: String): PlanDocument? {
        val node = try {
            AgentJson.mapper.readTree(payload)
        } catch (ignored: IOException) {
            return null
        }
        if (!node.isObject) {
            return null
        }
        val goal = node.path("goal").asText("").trim().ifBlank { DEFAULT_GOAL }
        val stepsNode = node.path("steps")
        if (!stepsNode.isArray || stepsNode.isEmpty) {
            return null
        }
        val steps = mutableListOf<PlanStep>()
        stepsNode.forEachIndexed { index, stepNode ->
            if (!stepNode.isObject) {
                return@forEachIndexed
            }
            val title = stepNode.path("title").asText("").trim()
                .ifBlank { stepNode.path("name").asText("").trim() }
                .ifBlank { "Step ${index + 1}" }
            val instruction = stepNode.path("instruction").asText("").trim()
                .ifBlank { stepNode.path("detail").asText("").trim() }
                .ifBlank { stepNode.path("description").asText("").trim() }
                .ifBlank { title }
            val id = stepNode.path("id").asInt(index + 1).let { if (it > 0) it else index + 1 }
            steps += PlanStep(id = id, title = title, instruction = instruction)
        }
        if (steps.isEmpty()) {
            return null
        }
        return PlanDocument(goal = goal, steps = steps)
    }

    private fun fromList(text: String): PlanDocument? {
        val lines = text.lines().map { it.trim() }.filter { it.isNotEmpty() }
        if (lines.isEmpty()) {
            return null
        }
        val items = lines.mapNotNull { line ->
            numberedLine.find(line)?.groupValues?.get(1)?.trim()?.takeIf { it.isNotEmpty() }
        }
        // Only treat as a plan list when every non-empty line is a list item.
        // Multi-line essays must not become dozens of checklist rows.
        if (items.size < 2 || items.size != lines.size) {
            return null
        }
        return PlanDocument(
            goal = DEFAULT_GOAL,
            steps = items.mapIndexed { index, item ->
                PlanStep(id = index + 1, title = shorten(item), instruction = item)
            },
        )
    }

    private fun shorten(text: String): String {
        val oneLine = text.replace(Regex("\\s+"), " ").trim()
        if (oneLine.length <= 80) {
            return oneLine
        }
        return oneLine.take(77).trimEnd() + "..."
    }

    private fun extractJson(text: String): String? {
        val fenced = fence.find(text)?.groupValues?.get(1)?.trim()
        val body = if (fenced.isNullOrBlank()) text else fenced
        val start = body.indexOf('{')
        val end = body.lastIndexOf('}')
        if (start < 0 || end <= start) {
            return null
        }
        return body.substring(start, end + 1)
    }

    private fun toObjectNode(document: PlanDocument): ObjectNode {
        val root = AgentJson.mapper.createObjectNode()
        root.put("goal", document.goal)
        val steps = root.putArray("steps")
        document.steps.forEach { step ->
            val node = steps.addObject()
            node.put("id", step.id)
            node.put("title", step.title)
            node.put("instruction", step.instruction)
        }
        return root
    }
}
