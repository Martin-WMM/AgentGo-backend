package com.agentgo.core.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class LlmDecisionParserTest {
    @Test
    fun `plain text is a final answer`() {
        assertEquals(LlmDecision.Final("just prose"), LlmDecisionParser.parse("just prose"))
    }

    @Test
    fun `fenced json selects a tool call and keeps nested arguments`() {
        val decision = LlmDecisionParser.parse(
            """
            ```json
            {"type":"tool","name":"lookup","arguments":{"key":"alpha","n":7,"nested":{"a":1}}}
            ```
            """.trimIndent(),
        )

        val call = assertIs<LlmDecision.ToolCall>(decision)
        assertEquals("lookup", call.name)
        assertEquals("alpha", call.arguments["key"])
        assertEquals("7", call.arguments["n"])
        assertEquals("{\"a\":1}", call.arguments["nested"])
    }

    @Test
    fun `final json returns the content`() {
        assertEquals(LlmDecision.Final("hello"), LlmDecisionParser.parse("""{"type":"final","content":"hello"}"""))
    }

    @Test
    fun `blank final content and an unknown type stay final answers`() {
        val blank = """{"type":"final","content":"  "}"""
        val other = """{"type":"note","content":"hello"}"""

        assertEquals(LlmDecision.Final(blank), LlmDecisionParser.parse(blank))
        assertEquals(LlmDecision.Final(other), LlmDecisionParser.parse(other))
    }

    @Test
    fun `a tool without a name or arguments stays a final answer`() {
        val raw = """{"type":"tool","name":"  "}"""

        assertEquals(LlmDecision.Final(raw), LlmDecisionParser.parse(raw))
        val call = assertIs<LlmDecision.ToolCall>(LlmDecisionParser.parse("""{"type":"tool","name":"lookup"}"""))
        assertTrue(call.arguments.isEmpty())
    }

    @Test
    fun `broken json and a json array stay final answers`() {
        assertEquals(LlmDecision.Final("{not json"), LlmDecisionParser.parse("{not json"))
        assertEquals(LlmDecision.Final("[1]"), LlmDecisionParser.parse("[1]"))
        val fenced = "```json\nno braces\n```"
        assertEquals(LlmDecision.Final(fenced), LlmDecisionParser.parse(fenced))
    }
}
