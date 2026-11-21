package com.agentgo.a2a.controller

import com.agentgo.a2a.service.A2aAgentService
import com.agentgo.commons.dto.http.request.BatchIdsRequest
import com.agentgo.commons.dto.http.response.status.success.A2aResponseCodeRegistry
import com.agentgo.dto.a2a.A2aAgentCreateRequest
import com.agentgo.dto.a2a.A2aAgentResponse
import com.agentgo.dto.a2a.A2aAgentStatus
import com.agentgo.dto.a2a.A2aAgentUpdateRequest
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus

class A2aAgentControllerTest {
    private val a2aAgentService = mock<A2aAgentService>()
    private val controller = A2aAgentController(a2aAgentService)

    @Test
    fun `create wraps the service result with the created response code`() {
        val request = A2aAgentCreateRequest().apply {
            cardUrl = "https://agent.example.com/.well-known/agent-card.json"
            groupName = "billing"
            name = "billing-agent"
        }
        val response = sampleResponse(1L)
        whenever(a2aAgentService.create(request)).thenReturn(response)

        val created = controller.create(request)

        assertEquals(A2aResponseCodeRegistry.CREATED, created.code)
        assertEquals(A2aResponseCodeRegistry.CREATED_TYPE, created.responseType)
        assertEquals("A2A agent created", created.message)
        assertEquals(response, created.data)
        assertTrue(created.success)
    }

    @Test
    fun `get wraps the service result with the retrieved response code`() {
        val response = sampleResponse(2L)
        whenever(a2aAgentService.get(2L)).thenReturn(response)

        val retrieved = controller.get(2L)

        assertEquals(A2aResponseCodeRegistry.RETRIEVED, retrieved.code)
        assertEquals(A2aResponseCodeRegistry.RETRIEVED_TYPE, retrieved.responseType)
        assertEquals("A2A agent retrieved", retrieved.message)
        assertEquals(response, retrieved.data)
    }

    @Test
    fun `list wraps filtered and unfiltered service results`() {
        val response = sampleResponse(3L)
        whenever(a2aAgentService.list(null)).thenReturn(listOf(response))
        whenever(a2aAgentService.list(A2aAgentStatus.AVAILABLE)).thenReturn(emptyList())

        val listed = controller.list(null)
        val filtered = controller.list(A2aAgentStatus.AVAILABLE)

        assertEquals(A2aResponseCodeRegistry.LIST_RETRIEVED, listed.code)
        assertEquals(A2aResponseCodeRegistry.LIST_RETRIEVED_TYPE, listed.responseType)
        assertEquals("A2A agents listed", listed.message)
        assertEquals(listOf(response), listed.data)
        assertEquals(emptyList(), filtered.data)
        assertEquals(A2aResponseCodeRegistry.LIST_RETRIEVED, filtered.code)
    }

    @Test
    fun `update wraps the service result with the updated response code`() {
        val request = A2aAgentUpdateRequest().apply {
            cardUrl = "https://agent.example.com/.well-known/agent-card.json"
            groupName = "billing"
            name = "billing-agent"
        }
        val response = sampleResponse(4L)
        whenever(a2aAgentService.update(4L, request)).thenReturn(response)

        val updated = controller.update(4L, request)

        assertEquals(A2aResponseCodeRegistry.UPDATED, updated.code)
        assertEquals(A2aResponseCodeRegistry.UPDATED_TYPE, updated.responseType)
        assertEquals("A2A agent updated", updated.message)
        assertEquals(response, updated.data)
    }

    @Test
    fun `delete returns no content after the service removes the agent`() {
        val deleted = controller.delete(5L)

        assertEquals(HttpStatus.NO_CONTENT, deleted.statusCode)
        assertNull(deleted.body)
        verify(a2aAgentService).delete(5L)
    }

    @Test
    fun `deleteAll returns no content and delegates ids`() {
        val request = BatchIdsRequest().apply { ids = listOf(1L, 2L) }

        val deleted = controller.deleteAll(request)

        assertEquals(HttpStatus.NO_CONTENT, deleted.statusCode)
        assertNull(deleted.body)
        verify(a2aAgentService).deleteAll(listOf(1L, 2L))
    }

    @Test
    fun `deleteAll treats a missing id list as empty`() {
        val request = BatchIdsRequest().apply { ids = null }

        val deleted = controller.deleteAll(request)

        assertEquals(HttpStatus.NO_CONTENT, deleted.statusCode)
        verify(a2aAgentService).deleteAll(emptyList())
    }

    private fun sampleResponse(id: Long): A2aAgentResponse =
        A2aAgentResponse(
            id = id,
            cardUrl = "https://agent.example.com/.well-known/agent-card.json",
            groupName = "billing",
            name = "billing-agent",
            description = "Handles invoices",
            tags = listOf("billing"),
            metadata = mapOf("team" to "ops"),
            descriptionVector = List(64) { 0.0 },
            enabled = true,
            status = A2aAgentStatus.UNKNOWN,
            createdAt = Instant.parse("2026-01-01T00:00:00Z"),
            updatedAt = Instant.parse("2026-01-02T00:00:00Z"),
        )
}
