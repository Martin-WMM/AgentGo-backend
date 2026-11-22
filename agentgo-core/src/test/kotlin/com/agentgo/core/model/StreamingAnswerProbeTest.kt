package com.agentgo.core.model

import kotlin.test.Test
import kotlin.test.assertEquals

class StreamingAnswerProbeTest {
    @Test
    fun `final json streams only the content field`() {
        val deltas = mutableListOf<String>()
        val probe = StreamingAnswerProbe(deltas::add)

        """{"type":"final","content":"Hello"}""".chunked(3).forEach(probe::accept)

        assertEquals("Hello", deltas.joinToString(""))
        assertEquals("""{"type":"final","content":"Hello"}""", probe.snapshot())
    }

    @Test
    fun `tool json does not emit answer deltas`() {
        val deltas = mutableListOf<String>()
        val probe = StreamingAnswerProbe(deltas::add)

        """{"type":"tool","name":"shell","arguments":{"script":"echo"}}""".chunked(4).forEach(probe::accept)

        assertEquals(emptyList(), deltas)
    }

    @Test
    fun `prose streams as answer text`() {
        val deltas = mutableListOf<String>()
        val probe = StreamingAnswerProbe(deltas::add)

        probe.accept("Hi ")
        probe.accept("there")

        assertEquals(listOf("Hi ", "there"), deltas)
    }

    @Test
    fun `markdown answer streams from the first token`() {
        val deltas = mutableListOf<String>()
        val probe = StreamingAnswerProbe(deltas::add)

        probe.accept("# Jordan")
        probe.accept(" form")

        assertEquals(listOf("# Jordan", " form"), deltas)
    }
}
