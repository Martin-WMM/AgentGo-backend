package com.agentgo.a2a.service

import com.agentgo.a2a.exception.InvalidA2aAgentRequestException
import com.agentgo.dto.a2a.A2aAgentCreateRequest
import com.agentgo.dto.a2a.A2aAgentStatus
import com.agentgo.dto.a2a.A2aAgentUpdateRequest
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class A2aAgentRequestNormalizerTest {
    private val normalizer = A2aAgentRequestNormalizer()

    @Test
    fun `normalizes a create payload and applies defaults`() {
        val request = A2aAgentCreateRequest().apply {
            cardUrl = "  HTTPS://Example.com/card  "
            groupName = "  billing  "
            name = "  billing-agent  "
            description = "  Handles invoices  "
            tags = listOf(" billing ", "", "billing", "finance")
            metadata = mapOf("team" to "ops")
            enabled = null
            status = null
        }

        val normalized = normalizer.normalizeCreate(request)

        assertEquals("HTTPS://Example.com/card", normalized.cardUrl)
        assertEquals("billing", normalized.groupName)
        assertEquals("billing-agent", normalized.name)
        assertEquals("Handles invoices", normalized.description)
        assertEquals(listOf("billing", "finance"), normalized.tags)
        assertEquals(mapOf("team" to "ops"), normalized.metadata)
        assertEquals(true, normalized.enabled)
        assertEquals(A2aAgentStatus.UNKNOWN, normalized.status)
        assertEquals(normalized, normalized.copy())
        assertEquals(normalized.hashCode(), normalized.copy().hashCode())
        assertTrue(normalized.toString().contains("billing-agent"))
    }

    @Test
    fun `normalizes an update payload with explicit enabled and status`() {
        val request = A2aAgentUpdateRequest().apply {
            cardUrl = "https://agent.example.com/card"
            groupName = "support"
            name = "support-agent"
            description = null
            tags = emptyList()
            metadata = emptyMap()
            enabled = false
            status = A2aAgentStatus.ERROR
        }

        val normalized = normalizer.normalizeUpdate(request)

        assertEquals("support-agent", normalized.name)
        assertEquals(null, normalized.description)
        assertEquals(emptyList(), normalized.tags)
        assertEquals(emptyMap(), normalized.metadata)
        assertEquals(false, normalized.enabled)
        assertEquals(A2aAgentStatus.ERROR, normalized.status)
    }

    @Test
    fun `accepts fields at the documented limits`() {
        val cardPrefix = "https://example.com/"
        val request = A2aAgentCreateRequest().apply {
            cardUrl = cardPrefix + "a".repeat(2048 - cardPrefix.length)
            groupName = "g".repeat(255)
            name = "n".repeat(255)
            description = "d".repeat(4000)
            tags = List(16) { index -> "t".repeat(31) + index.toString(16) }
            metadata = (1..32).associate { index -> "k$index" to "v".repeat(2000) }
            enabled = true
            status = A2aAgentStatus.AVAILABLE
        }

        val normalized = normalizer.normalizeCreate(request)

        assertEquals(2048, normalized.cardUrl.length)
        assertEquals(255, normalized.groupName.length)
        assertEquals(255, normalized.name.length)
        assertEquals(4000, normalized.description?.length)
        assertEquals(16, normalized.tags.size)
        assertEquals(32, normalized.tags.first().length)
        assertEquals(32, normalized.metadata.size)
    }

    @Test
    fun `rejects a blank card url`() {
        val exception = assertFailsWith<InvalidA2aAgentRequestException> {
            normalizer.normalizeCreate(validCreate().apply { cardUrl = "   " })
        }

        assertEquals("The A2A card URL must not be blank.", exception.message)
    }

    @Test
    fun `rejects a card url longer than 2048 characters`() {
        val exception = assertFailsWith<InvalidA2aAgentRequestException> {
            normalizer.normalizeCreate(validCreate().apply { cardUrl = "https://example.com/" + "a".repeat(2048) })
        }

        assertEquals("The A2A card URL must be at most 2048 characters.", exception.message)
    }

    @Test
    fun `rejects a card url that is not a valid uri`() {
        val exception = assertFailsWith<InvalidA2aAgentRequestException> {
            normalizer.normalizeCreate(validCreate().apply { cardUrl = "http://[" })
        }

        assertEquals("The A2A card URL must be a valid absolute HTTP(S) URL.", exception.message)
    }

    @Test
    fun `rejects a card url that does not use http or https`() {
        val exception = assertFailsWith<InvalidA2aAgentRequestException> {
            normalizer.normalizeCreate(validCreate().apply { cardUrl = "ftp://example.com/card" })
        }

        assertEquals("The A2A card URL must use http or https.", exception.message)
    }

    @Test
    fun `rejects a card url without a host`() {
        val exception = assertFailsWith<InvalidA2aAgentRequestException> {
            normalizer.normalizeCreate(validCreate().apply { cardUrl = "https:///card" })
        }

        assertEquals("The A2A card URL must include a host.", exception.message)
    }

    @Test
    fun `rejects a blank group name`() {
        val exception = assertFailsWith<InvalidA2aAgentRequestException> {
            normalizer.normalizeCreate(validCreate().apply { groupName = "  " })
        }

        assertEquals("The A2A agent groupName must not be blank.", exception.message)
    }

    @Test
    fun `rejects a blank name`() {
        val exception = assertFailsWith<InvalidA2aAgentRequestException> {
            normalizer.normalizeCreate(validCreate().apply { name = "  " })
        }

        assertEquals("The A2A agent name must not be blank.", exception.message)
    }

    @Test
    fun `rejects a group name longer than 255 characters`() {
        val exception = assertFailsWith<InvalidA2aAgentRequestException> {
            normalizer.normalizeCreate(validCreate().apply { groupName = "g".repeat(256) })
        }

        assertEquals("The A2A agent groupName must be at most 255 characters.", exception.message)
    }

    @Test
    fun `rejects a name longer than 255 characters`() {
        val exception = assertFailsWith<InvalidA2aAgentRequestException> {
            normalizer.normalizeUpdate(validUpdate().apply { name = "n".repeat(256) })
        }

        assertEquals("The A2A agent name must be at most 255 characters.", exception.message)
    }

    @Test
    fun `drops a blank description`() {
        val normalized = normalizer.normalizeCreate(validCreate().apply { description = "   " })

        assertEquals(null, normalized.description)
    }

    @Test
    fun `rejects a description longer than 4000 characters`() {
        val exception = assertFailsWith<InvalidA2aAgentRequestException> {
            normalizer.normalizeCreate(validCreate().apply { description = "d".repeat(4001) })
        }

        assertEquals("The A2A agent description must be at most 4000 characters.", exception.message)
    }

    @Test
    fun `rejects more than 16 tags`() {
        val exception = assertFailsWith<InvalidA2aAgentRequestException> {
            normalizer.normalizeCreate(validCreate().apply { tags = List(17) { index -> "tag$index" } })
        }

        assertEquals("The A2A agent tags support at most 16 entries.", exception.message)
    }

    @Test
    fun `rejects a tag longer than 32 characters`() {
        val exception = assertFailsWith<InvalidA2aAgentRequestException> {
            normalizer.normalizeCreate(validCreate().apply { tags = listOf("t".repeat(33)) })
        }

        assertEquals("Each A2A agent tag must be at most 32 characters.", exception.message)
    }

    @Test
    fun `accepts nested metadata scalars arrays and objects`() {
        val request = validCreate().apply {
            metadata = mapOf(
                "team" to "ops",
                "enabled" to false,
                "score" to 1.5,
                "count" to 2,
                "note" to null,
                "flags" to mapOf("beta" to true),
                "labels" to listOf("gold", null, mapOf("rank" to 1)),
            )
        }

        val normalized = normalizer.normalizeCreate(request)

        assertEquals("ops", normalized.metadata["team"])
        assertEquals(false, normalized.metadata["enabled"])
        assertEquals(1.5, normalized.metadata["score"])
        assertEquals(2, normalized.metadata["count"])
        assertEquals(null, normalized.metadata["note"])
        assertEquals(mapOf("beta" to true), normalized.metadata["flags"])
        assertEquals(listOf("gold", null, mapOf("rank" to 1)), normalized.metadata["labels"])
    }

    @Test
    fun `accepts metadata nested four levels deep`() {
        val normalized = normalizer.normalizeCreate(validCreate().apply { metadata = nestedMaps(4) })

        assertEquals("value", (((normalized.metadata["child"] as Map<*, *>)["child"] as Map<*, *>)["child"] as Map<*, *>)["leaf"])
    }

    @Test
    fun `rejects metadata nested too deeply`() {
        val exception = assertFailsWith<InvalidA2aAgentRequestException> {
            normalizer.normalizeCreate(validCreate().apply { metadata = nestedMaps(5) })
        }

        assertEquals("A2A agent metadata is nested too deeply.", exception.message)
    }

    @Test
    fun `rejects metadata with too many entries`() {
        val exception = assertFailsWith<InvalidA2aAgentRequestException> {
            normalizer.normalizeCreate(
                validCreate().apply { metadata = (1..33).associate { index -> "k$index" to "v" } },
            )
        }

        assertEquals("The A2A agent metadata supports at most 32 entries.", exception.message)
    }

    @Test
    fun `rejects metadata keys that are blank too long or null`() {
        val blank = assertFailsWith<InvalidA2aAgentRequestException> {
            normalizer.normalizeCreate(validCreate().apply { metadata = mapOf("   " to "v") })
        }
        val tooLong = assertFailsWith<InvalidA2aAgentRequestException> {
            normalizer.normalizeCreate(validCreate().apply { metadata = mapOf("k".repeat(65) to "v") })
        }
        val nullKey = assertFailsWith<InvalidA2aAgentRequestException> {
            val metadata = HashMap<String?, Any?>()
            metadata[null] = "v"
            @Suppress("UNCHECKED_CAST")
            normalizer.normalizeCreate(validCreate().apply { this.metadata = metadata as Map<String, Any?> })
        }

        assertEquals("A2A agent metadata keys must be 1..64 characters.", blank.message)
        assertEquals(blank.message, tooLong.message)
        assertEquals(blank.message, nullKey.message)
    }

    @Test
    fun `rejects metadata strings longer than 2000 characters`() {
        val exception = assertFailsWith<InvalidA2aAgentRequestException> {
            normalizer.normalizeCreate(validCreate().apply { metadata = mapOf("note" to "v".repeat(2001)) })
        }

        assertEquals("A2A agent metadata strings must be at most 2000 characters.", exception.message)
    }

    @Test
    fun `rejects non finite metadata numbers`() {
        val notANumber = assertFailsWith<InvalidA2aAgentRequestException> {
            normalizer.normalizeCreate(validCreate().apply { metadata = mapOf("n" to Double.NaN) })
        }
        val infinite = assertFailsWith<InvalidA2aAgentRequestException> {
            normalizer.normalizeCreate(validCreate().apply { metadata = mapOf("n" to Double.POSITIVE_INFINITY) })
        }

        assertEquals("A2A agent metadata numbers must be finite.", notANumber.message)
        assertEquals(notANumber.message, infinite.message)
    }

    @Test
    fun `rejects metadata arrays longer than 32 entries`() {
        val exception = assertFailsWith<InvalidA2aAgentRequestException> {
            normalizer.normalizeCreate(validCreate().apply { metadata = mapOf("items" to List(33) { "item" }) })
        }

        assertEquals("A2A agent metadata arrays support at most 32 entries.", exception.message)
    }

    @Test
    fun `rejects unsupported metadata value types`() {
        val objectValue = assertFailsWith<InvalidA2aAgentRequestException> {
            normalizer.normalizeCreate(validCreate().apply { metadata = mapOf("when" to Instant.EPOCH) })
        }
        val nestedValue = assertFailsWith<InvalidA2aAgentRequestException> {
            normalizer.normalizeCreate(
                validCreate().apply { metadata = mapOf("items" to listOf(Instant.parse("2026-01-01T00:00:00Z"))) },
            )
        }

        assertEquals("A2A agent metadata values must be JSON scalars, arrays, or objects.", objectValue.message)
        assertEquals(objectValue.message, nestedValue.message)
    }

    @Test
    fun `treats missing tags and metadata as empty`() {
        val request = validCreate()
        setField(request, "tags", null)
        setField(request, "metadata", null)

        val normalized = normalizer.normalizeCreate(request)

        assertEquals(emptyList(), normalized.tags)
        assertEquals(emptyMap(), normalized.metadata)
    }

    private fun validCreate(): A2aAgentCreateRequest =
        A2aAgentCreateRequest().apply {
            cardUrl = "https://agent.example.com/card"
            groupName = "billing"
            name = "billing-agent"
            description = "Handles invoices"
            enabled = true
            status = A2aAgentStatus.UNKNOWN
        }

    private fun validUpdate(): A2aAgentUpdateRequest =
        A2aAgentUpdateRequest().apply {
            cardUrl = "https://agent.example.com/card"
            groupName = "billing"
            name = "billing-agent"
        }

    private fun nestedMaps(levels: Int): Map<String, Any?> {
        var current: Map<String, Any?> = mapOf("leaf" to "value")
        repeat(levels - 1) {
            current = mapOf("child" to current)
        }
        return current
    }

    private fun setField(target: Any, name: String, value: Any?) {
        val field = target.javaClass.getDeclaredField(name)
        field.isAccessible = true
        field.set(target, value)
    }
}
