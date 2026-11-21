package com.agentgo.a2a.embedding

import kotlin.math.abs
import kotlin.math.sqrt
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DescriptionEmbeddingTest {
    @Test
    fun `stores an empty description as a zero vector`() {
        val zeros = List(DescriptionEmbedding.DIMENSION) { 0.0 }

        assertEquals(zeros, DescriptionEmbedding.embed(null))
        assertEquals(zeros, DescriptionEmbedding.embed("   "))
        assertEquals(zeros, DescriptionEmbedding.embed("..."))
    }

    @Test
    fun `embeds text into a 64 dimension unit vector`() {
        val vector = DescriptionEmbedding.embed("  Billing   AGENT ")
        val norm = sqrt(vector.sumOf { value -> value * value })

        assertEquals(DescriptionEmbedding.DIMENSION, vector.size)
        assertEquals(DescriptionEmbedding.embed("billing agent"), vector)
        assertEquals(1.0, norm, absoluteTolerance = 1.0e-9)
        assertTrue(vector.any { value -> value != 0.0 })
    }

    @Test
    fun `embeds a single character token without bigrams`() {
        val vector = DescriptionEmbedding.embed("a")
        val norm = sqrt(vector.sumOf { value -> value * value })

        assertEquals(1.0, norm, absoluteTolerance = 1.0e-9)
        assertEquals(1, vector.count { value -> value != 0.0 })
        assertEquals(1.0, abs(vector.first { value -> value != 0.0 }), absoluteTolerance = 1.0e-9)
    }
}
