package com.agentgo.core.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PlanParserTest {
    @Test
    fun `json plan is normalized`() {
        val raw = """
            ```json
            {
              "goal": "Explain Gaussian distribution",
              "steps": [
                {"id": 1, "title": "Outline concepts", "instruction": "List definition and parameters"},
                {"id": 2, "title": "Write answer", "instruction": "Compose the user-facing explanation"}
              ]
            }
            ```
        """.trimIndent()

        val document = PlanParser.parse(raw)

        assertEquals("Explain Gaussian distribution", document.goal)
        assertEquals(2, document.steps.size)
        assertEquals("Outline concepts", document.steps[0].title)
        assertTrue(PlanParser.normalize(raw).contains("\"goal\""))
    }

    @Test
    fun `numbered list becomes steps`() {
        val document = PlanParser.parse("1. Run the shell.\n2. Reply to the user.")

        assertEquals(2, document.steps.size)
        assertEquals("Run the shell.", document.steps[0].title)
        assertEquals("Reply to the user.", document.steps[1].instruction)
    }

    @Test
    fun `essay prose collapses to one deliverable step`() {
        val essay = """
            ## 8. Simple example
            Mean 100, sigma 2.
            ## 9. Relationship
            Gaussian means normal.
        """.trimIndent()

        val document = PlanParser.parse(essay)

        assertEquals(1, document.steps.size)
        assertEquals("Answer directly", document.steps[0].title)
    }

    @Test
    fun `blank input uses the default plan`() {
        val document = PlanParser.parse("   ")

        assertEquals("Answer the user request", document.goal)
        assertEquals("Answer directly", document.steps.single().title)
    }

    @Test
    fun `short single-line prose becomes one step`() {
        val document = PlanParser.parse("Answer directly.")

        assertEquals(1, document.steps.size)
        assertEquals("Answer directly.", document.steps.single().title)
    }
}
