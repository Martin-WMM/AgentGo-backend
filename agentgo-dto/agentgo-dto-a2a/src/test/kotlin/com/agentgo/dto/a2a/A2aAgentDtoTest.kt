package com.agentgo.dto.a2a

import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class A2aAgentDtoTest {
    @Test
    fun `create request stores defaults and matches an equal copy`() {
        val empty = A2aAgentCreateRequest()
        assertEquals("", empty.cardUrl)
        assertEquals("", empty.groupName)
        assertEquals("", empty.name)
        assertNull(empty.description)
        assertEquals(emptyList(), empty.tags)
        assertEquals(emptyMap(), empty.metadata)
        assertEquals(true, empty.enabled)
        assertEquals(A2aAgentStatus.UNKNOWN, empty.status)

        val request = filledCreate()
        val same = A2aAgentCreateRequest().apply {
            cardUrl = request.cardUrl
            groupName = request.groupName
            name = request.name
            description = request.description
            tags = request.tags
            metadata = request.metadata
            enabled = request.enabled
            status = request.status
        }

        assertEquals(request, same)
        assertEquals(request.hashCode(), same.hashCode())
        assertEquals(request, request)
        assertNotEquals(request, empty)
        assertNotEquals(request.hashCode(), empty.hashCode())
        assertFalse(request.equals("billing-agent"))
    }

    @Test
    fun `update request stores fields and rejects a different value`() {
        val request = filledUpdate()
        val renamed = A2aAgentUpdateRequest().apply {
            cardUrl = request.cardUrl
            groupName = request.groupName
            name = "other-agent"
            description = request.description
            tags = request.tags
            metadata = request.metadata
            enabled = request.enabled
            status = request.status
        }
        val cleared = A2aAgentUpdateRequest().apply {
            cardUrl = request.cardUrl
            groupName = request.groupName
            name = request.name
            description = null
            tags = emptyList()
            metadata = emptyMap()
            enabled = null
            status = null
        }

        assertEquals("billing-agent", request.name)
        assertEquals("Handles invoice questions for the billing workspace.", request.description)
        assertEquals(listOf("billing", "finance"), request.tags)
        assertEquals(mapOf("team" to "ops"), request.metadata)
        assertEquals(false, request.enabled)
        assertEquals(A2aAgentStatus.UNAVAILABLE, request.status)
        assertEquals(request, request)
        assertEquals(request.hashCode(), request.hashCode())
        assertNotEquals(request, renamed)
        assertNotEquals(request.hashCode(), renamed.hashCode())
        assertNotEquals(request, cleared)
        assertNotEquals(request, A2aAgentUpdateRequest())
        assertFalse(request.equals(A2aAgentCreateRequest()))
        assertFalse(request.equals("billing-agent"))
    }

    @Test
    fun `create request equality tracks every field`() {
        val filled = filledCreate()
        val differentCard = filledCreate().apply { cardUrl = "https://other.example/.well-known/agent-card.json" }
        val differentGroup = filledCreate().apply { groupName = "support" }
        val differentName = filledCreate().apply { name = "support-agent" }
        val differentDescription = filledCreate().apply { description = null }
        val differentTags = filledCreate().apply { tags = listOf("support") }
        val differentMetadata = filledCreate().apply { metadata = emptyMap() }
        val differentEnabled = filledCreate().apply { enabled = false }
        val differentStatus = filledCreate().apply { status = A2aAgentStatus.ERROR }

        assertNotEquals(filled, differentCard)
        assertNotEquals(filled, differentGroup)
        assertNotEquals(filled, differentName)
        assertNotEquals(filled, differentDescription)
        assertNotEquals(filled, differentTags)
        assertNotEquals(filled, differentMetadata)
        assertNotEquals(filled, differentEnabled)
        assertNotEquals(filled, differentStatus)
        assertNotEquals(filled.hashCode(), differentStatus.hashCode())
    }

    @Test
    fun `response keeps agent fields and generated members`() {
        val createdAt = Instant.parse("2026-10-11T00:00:00Z")
        val updatedAt = Instant.parse("2026-10-11T01:00:00Z")
        val vector = List(64) { 0.0 }
        val response = A2aAgentResponse(
            id = 1L,
            cardUrl = "https://agent.example.com/.well-known/agent-card.json",
            groupName = "billing",
            name = "billing-agent",
            description = "Handles invoice questions for the billing workspace.",
            tags = listOf("billing", "finance"),
            metadata = mapOf("team" to "ops"),
            descriptionVector = vector,
            enabled = true,
            status = A2aAgentStatus.AVAILABLE,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )

        assertEquals(1L, response.id)
        assertEquals("https://agent.example.com/.well-known/agent-card.json", response.cardUrl)
        assertEquals("billing", response.groupName)
        assertEquals("billing-agent", response.name)
        assertEquals("Handles invoice questions for the billing workspace.", response.description)
        assertEquals(listOf("billing", "finance"), response.tags)
        assertEquals(mapOf("team" to "ops"), response.metadata)
        assertEquals(vector, response.descriptionVector)
        assertTrue(response.enabled)
        assertEquals(A2aAgentStatus.AVAILABLE, response.status)
        assertEquals(createdAt, response.createdAt)
        assertEquals(updatedAt, response.updatedAt)
        assertEquals(response, response.copy())
        assertEquals(response.hashCode(), response.copy().hashCode())
        assertNotEquals(response, response.copy(description = null, status = A2aAgentStatus.ERROR))
        assertNotEquals(response.hashCode(), response.copy(name = "other").hashCode())
        assertTrue(response.toString().contains("billing-agent"))
        assertNull(response.copy(description = null).description)
    }

    @Test
    fun `status exposes every operator value`() {
        assertEquals(
            listOf(
                A2aAgentStatus.UNKNOWN,
                A2aAgentStatus.AVAILABLE,
                A2aAgentStatus.UNAVAILABLE,
                A2aAgentStatus.ERROR,
            ),
            A2aAgentStatus.entries,
        )
        assertEquals(A2aAgentStatus.UNKNOWN, A2aAgentStatus.valueOf("UNKNOWN"))
        assertEquals(A2aAgentStatus.AVAILABLE, A2aAgentStatus.valueOf("AVAILABLE"))
        assertEquals(A2aAgentStatus.UNAVAILABLE, A2aAgentStatus.valueOf("UNAVAILABLE"))
        assertEquals(A2aAgentStatus.ERROR, A2aAgentStatus.valueOf("ERROR"))
    }

    private fun filledCreate(): A2aAgentCreateRequest = A2aAgentCreateRequest().apply {
        cardUrl = "https://agent.example.com/.well-known/agent-card.json"
        groupName = "billing"
        name = "billing-agent"
        description = "Handles invoice questions for the billing workspace."
        tags = listOf("billing", "finance")
        metadata = mapOf("team" to "ops")
        enabled = true
        status = A2aAgentStatus.UNKNOWN
    }

    private fun filledUpdate(): A2aAgentUpdateRequest = A2aAgentUpdateRequest().apply {
        cardUrl = "https://agent.example.com/.well-known/agent-card.json"
        groupName = "billing"
        name = "billing-agent"
        description = "Handles invoice questions for the billing workspace."
        tags = listOf("billing", "finance")
        metadata = mapOf("team" to "ops")
        enabled = false
        status = A2aAgentStatus.UNAVAILABLE
    }
}
