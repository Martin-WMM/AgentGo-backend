package com.agentgo.core.model

/**
 * Structured plan produced by the Plan node for the ReAct loop.
 *
 * @property goal one-sentence objective
 * @property steps ordered executable steps
 */
data class PlanDocument(
    val goal: String,
    val steps: List<PlanStep>,
) {
    init {
        require(goal.isNotBlank()) { "Plan goal must not be blank." }
        require(steps.isNotEmpty()) { "Plan must contain at least one step." }
    }
}

/**
 * One actionable step inside a [PlanDocument].
 *
 * @property id 1-based step id
 * @property title short checklist label for the UI
 * @property instruction what the ReAct executor should do
 */
data class PlanStep(
    val id: Int,
    val title: String,
    val instruction: String,
) {
    init {
        require(id > 0) { "Plan step id must be positive." }
        require(title.isNotBlank()) { "Plan step title must not be blank." }
        require(instruction.isNotBlank()) { "Plan step instruction must not be blank." }
    }
}
