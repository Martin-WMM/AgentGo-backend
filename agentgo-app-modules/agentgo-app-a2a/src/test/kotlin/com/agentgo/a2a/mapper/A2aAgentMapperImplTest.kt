package com.agentgo.a2a.mapper

import com.agentgo.a2a.entity.A2aAgentEntity
import com.agentgo.a2a.service.NormalizedA2aAgent
import com.agentgo.dto.a2a.A2aAgentStatus
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class A2aAgentMapperImplTest {
    private val mapper = A2aAgentMapperImpl()
    private val createdAt = Instant.parse("2026-01-01T00:00:00Z")
    private val updatedAt = Instant.parse("2026-01-02T00:00:00Z")

    @Test
    fun `maps a persisted entity to the api response`() {
        val tags = mutableListOf("billing")
        val metadata = mutableMapOf<String, Any?>("team" to "ops")
        val entity = persistedEntity(tags = tags, metadata = metadata)
        @Suppress("UNCHECKED_CAST")
        entity.descriptionVector = mutableListOf<Any>(1, 2.5) as MutableList<Double>

        val response = mapper.toResponse(entity)
        tags.add("extra")
        metadata["changed"] = true

        assertEquals(7L, response.id)
        assertEquals("https://agent.example.com/card", response.cardUrl)
        assertEquals("billing", response.groupName)
        assertEquals("billing-agent", response.name)
        assertEquals("Handles invoices", response.description)
        assertEquals(listOf("billing"), response.tags)
        assertEquals(mapOf("team" to "ops"), response.metadata)
        assertEquals(listOf(1.0, 2.5), response.descriptionVector)
        assertEquals(true, response.enabled)
        assertEquals(A2aAgentStatus.AVAILABLE, response.status)
        assertEquals(createdAt, response.createdAt)
        assertEquals(updatedAt, response.updatedAt)
    }

    @Test
    fun `rejects an entity that has not been persisted`() {
        val missingId = assertFailsWith<IllegalArgumentException> {
            mapper.toResponse(persistedEntity().apply { id = null })
        }
        val missingCreatedAt = assertFailsWith<IllegalArgumentException> {
            mapper.toResponse(persistedEntity().apply { createdAt = null })
        }
        val missingUpdatedAt = assertFailsWith<IllegalArgumentException> {
            mapper.toResponse(persistedEntity().apply { updatedAt = null })
        }

        assertEquals("Persisted A2A agent must have an identifier.", missingId.message)
        assertEquals("Persisted A2A agent must have a creation timestamp.", missingCreatedAt.message)
        assertEquals("Persisted A2A agent must have an update timestamp.", missingUpdatedAt.message)
    }

    @Test
    fun `rejects a description vector that contains a non numeric value`() {
        val entity = persistedEntity()
        @Suppress("UNCHECKED_CAST")
        entity.descriptionVector = mutableListOf<Any>("bad") as MutableList<Double>

        val exception = assertFailsWith<IllegalArgumentException> {
            mapper.toResponse(entity)
        }

        assertEquals("Description vector contains a non-numeric value.", exception.message)
    }

    @Test
    fun `maps normalized fields onto a new entity without a description vector`() {
        val agent = sampleAgent()

        val entity = mapper.toEntity("ada", agent)

        assertEquals("ada", entity.ownerUserId)
        assertEquals(agent.cardUrl, entity.cardUrl)
        assertEquals(agent.groupName, entity.groupName)
        assertEquals(agent.name, entity.name)
        assertEquals(agent.description, entity.description)
        assertEquals(agent.tags, entity.tags)
        assertEquals(agent.metadata, entity.metadata)
        assertEquals(agent.enabled, entity.enabled)
        assertEquals(agent.status, entity.status)
        assertTrue(entity.descriptionVector.isEmpty())
        assertTrue(entity.tags !== agent.tags)
    }

    @Test
    fun `apply update replaces editable fields on the existing entity`() {
        val entity = persistedEntity()
        val agent = sampleAgent().copy(
            cardUrl = "https://agent.example.com/v2",
            groupName = "support",
            name = "support-agent",
            description = null,
            tags = listOf("support"),
            metadata = mapOf("tier" to "gold"),
            enabled = false,
            status = A2aAgentStatus.UNAVAILABLE,
        )

        mapper.applyUpdate(entity, agent)

        assertEquals("https://agent.example.com/v2", entity.cardUrl)
        assertEquals("support", entity.groupName)
        assertEquals("support-agent", entity.name)
        assertEquals(null, entity.description)
        assertEquals(listOf("support"), entity.tags)
        assertEquals(mapOf<String, Any?>("tier" to "gold"), entity.metadata)
        assertEquals(false, entity.enabled)
        assertEquals(A2aAgentStatus.UNAVAILABLE, entity.status)
        assertEquals(7L, entity.id)
    }

    private fun sampleAgent(): NormalizedA2aAgent =
        NormalizedA2aAgent(
            cardUrl = "https://agent.example.com/card",
            groupName = "billing",
            name = "billing-agent",
            description = "Handles invoices",
            tags = listOf("billing"),
            metadata = mapOf("team" to "ops"),
            enabled = true,
            status = A2aAgentStatus.AVAILABLE,
        )

    private fun persistedEntity(
        tags: MutableList<String> = mutableListOf("billing"),
        metadata: MutableMap<String, Any?> = mutableMapOf("team" to "ops"),
    ): A2aAgentEntity =
        A2aAgentEntity(
            id = 7L,
            ownerUserId = "ada",
            cardUrl = "https://agent.example.com/card",
            groupName = "billing",
            name = "billing-agent",
            description = "Handles invoices",
            tags = tags,
            metadata = metadata,
            descriptionVector = mutableListOf(0.0),
            enabled = true,
            status = A2aAgentStatus.AVAILABLE,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
}
