package com.agentgo.dto.events

import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNull

class SystemEventDtoTest {
    private val occurredAt = Instant.parse("2026-10-26T12:00:00Z")

    @Test
    fun `create request stores the supplied event fields`() {
        val request = createRequest()

        assertEquals("Model created", request.name)
        assertEquals("models", request.domain)
        assertEquals("ada", request.`operator`)
        assertEquals("Created the default model.", request.comment)
        assertEquals(occurredAt, request.occurredAt)
    }

    @Test
    fun `create request defaults operator comment and time to empty`() {
        val request = SystemEventCreateRequest()

        assertEquals("", request.name)
        assertEquals("", request.domain)
        assertNull(request.`operator`)
        assertNull(request.comment)
        assertNull(request.occurredAt)
        assertEquals(0, request.hashCode() - request.hashCode())
    }

    @Test
    fun `create request equality checks every field`() {
        val request = createRequest()

        assertEquals(request, request)
        assertEquals(request, createRequest())
        assertEquals(request.hashCode(), createRequest().hashCode())
        assertFalse(request.equals("Model created"))
        val later = occurredAt.plusSeconds(1)
        assertNotEquals(request, createRequest().apply { name = "Other" })
        assertNotEquals(request, createRequest().apply { domain = "files" })
        assertNotEquals(request, createRequest().apply { `operator` = "grace" })
        assertNotEquals(request, createRequest().apply { comment = "other" })
        assertNotEquals(request, createRequest().apply { this.occurredAt = later })
        assertNotEquals(request, createRequest().apply { `operator` = null })
        assertNotEquals(request, createRequest().apply { comment = null })
        assertNotEquals(request, createRequest().apply { this.occurredAt = null })
    }

    @Test
    fun `update request stores fields and compares them`() {
        val request = updateRequest()
        val same = updateRequest()

        assertEquals("Model renamed", request.name)
        assertEquals("models", request.domain)
        assertEquals("ada", request.`operator`)
        assertEquals("Renamed the default model.", request.comment)
        assertEquals(occurredAt, request.occurredAt)
        assertEquals(request, request)
        assertEquals(request, same)
        assertEquals(request.hashCode(), same.hashCode())
        assertFalse(request.equals(createRequest()))
        val later = occurredAt.plusSeconds(1)
        assertNotEquals(request, updateRequest().apply { name = "Other" })
        assertNotEquals(request, updateRequest().apply { domain = "files" })
        assertNotEquals(request, updateRequest().apply { `operator` = "grace" })
        assertNotEquals(request, updateRequest().apply { comment = "other" })
        assertNotEquals(request, updateRequest().apply { this.occurredAt = later })
        assertEquals(SystemEventUpdateRequest().hashCode(), SystemEventUpdateRequest().hashCode())
    }

    @Test
    fun `response stores the event returned to clients`() {
        val id = 1L
        val createdAt = Instant.parse("2026-10-26T12:00:01Z")
        val response = SystemEventResponse(
            id = id,
            name = "Model created",
            domain = "models",
            `operator` = "ada",
            comment = null,
            occurredAt = occurredAt,
            createdAt = createdAt,
            updatedAt = createdAt,
        )
        val same = response.copy()

        assertEquals(id, response.id)
        assertEquals("ada", response.`operator`)
        assertNull(response.comment)
        assertEquals(occurredAt, response.occurredAt)
        assertEquals(response, same)
        assertEquals(response.hashCode(), same.hashCode())
        assertNotEquals(response, response.copy(name = "Other"))
        assertNotEquals(response, response.copy(domain = "files"))
        assertNotEquals(response, response.copy(`operator` = "grace"))
        assertNotEquals(response, response.copy(comment = "note"))
        assertNotEquals(response, response.copy(occurredAt = occurredAt.plusSeconds(1)))
        assertNotEquals(response, response.copy(createdAt = createdAt.plusSeconds(1)))
        assertNotEquals(response, response.copy(updatedAt = createdAt.plusSeconds(1)))
        assertNotEquals(response, response.copy(id = 2L))
    }

    private fun createRequest() = SystemEventCreateRequest().also { request ->
        request.name = "Model created"
        request.domain = "models"
        request.`operator` = "ada"
        request.comment = "Created the default model."
        request.occurredAt = occurredAt
    }

    private fun updateRequest() = SystemEventUpdateRequest().also { request ->
        request.name = "Model renamed"
        request.domain = "models"
        request.`operator` = "ada"
        request.comment = "Renamed the default model."
        request.occurredAt = occurredAt
    }
}
