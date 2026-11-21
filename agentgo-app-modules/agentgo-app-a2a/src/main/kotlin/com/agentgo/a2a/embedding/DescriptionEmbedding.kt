package com.agentgo.a2a.embedding

import kotlin.math.sqrt

/**
 * Builds the stored description vector for an A2A agent.
 *
 * The vector is a deterministic 64-dimension feature hash of the description text.
 * It is L2-normalized so later cosine comparisons do not depend on description length.
 * An empty description is stored as a zero vector of the same dimension. This is not
 * a neural embedding and does not call an external model.
 */
object DescriptionEmbedding {
    /** Fixed width of every stored description vector. */
    const val DIMENSION: Int = 64

    /**
     * Embeds [description] into a [DIMENSION]-length vector.
     *
     * @param description free-form agent description, or null
     * @return a new list of [DIMENSION] doubles
     */
    fun embed(description: String?): List<Double> {
        val vector = DoubleArray(DIMENSION)
        val text = description?.trim()?.lowercase().orEmpty()
        if (text.isEmpty()) {
            return zero()
        }
        val tokens = tokensOf(text)
        if (tokens.isEmpty()) {
            return zero()
        }
        for (token in tokens) {
            val hash = token.hashCode()
            val index = Math.floorMod(hash, DIMENSION)
            val sign = if ((hash ushr 1) and 1 == 0) 1.0 else -1.0
            vector[index] += sign
        }
        var sumOfSquares = 0.0
        for (value in vector) {
            sumOfSquares += value * value
        }
        val norm = sqrt(sumOfSquares)
        if (norm == 0.0) {
            return vector.toList()
        }
        return vector.map { it / norm }
    }

    private fun tokensOf(text: String): List<String> = buildList {
        text.split(Regex("[^\\p{L}\\p{N}]+"))
            .filter { it.isNotEmpty() }
            .forEach { token ->
                add(token)
                if (token.length > 1) {
                    for (index in 0 until token.length - 1) {
                        add(token.substring(index, index + 2))
                    }
                }
            }
    }

    private fun zero(): List<Double> = List(DIMENSION) { 0.0 }
}
