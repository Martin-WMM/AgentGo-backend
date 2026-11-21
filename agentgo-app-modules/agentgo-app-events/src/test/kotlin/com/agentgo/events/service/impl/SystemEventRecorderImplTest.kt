package com.agentgo.events.service.impl

import com.agentgo.dto.events.SystemEventCreateRequest
import com.agentgo.dto.events.SystemEventResponse
import com.agentgo.events.service.SystemEventService
import java.time.Instant
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SystemEventRecorderImplTest {
    private val systemEventService = mock<SystemEventService>()
    private val recorder = SystemEventRecorderImpl(systemEventService)

    @Test
    fun `record forwards trimmed fields to the system event service`() {
        whenever(systemEventService.create(any())).thenReturn(sampleResponse())

        recorder.record(
            name = "  Model created  ",
            domain = " models ",
            comment = "  note  ",
            operator = "  ada  ",
            occurredAt = Instant.parse("2026-10-26T12:00:00Z"),
        )

        val captor = argumentCaptor<SystemEventCreateRequest>()
        verify(systemEventService).create(captor.capture())
        assertEquals("Model created", captor.firstValue.name)
        assertEquals("models", captor.firstValue.domain)
        assertEquals("note", captor.firstValue.comment)
        assertEquals("ada", captor.firstValue.`operator`)
        assertEquals(Instant.parse("2026-10-26T12:00:00Z"), captor.firstValue.occurredAt)
    }

    @Test
    fun `record skips blank name or domain without calling the service`() {
        recorder.record(name = "  ", domain = "models")
        recorder.record(name = "Model created", domain = " ")

        verify(systemEventService, never()).create(any())
    }

    @Test
    fun `record swallows service failures`() {
        whenever(systemEventService.create(any())).thenThrow(IllegalStateException("boom"))

        recorder.record(name = "Model created", domain = "models")

        verify(systemEventService).create(any())
    }

    @Test
    fun `record clears blank optional fields`() {
        whenever(systemEventService.create(any())).thenReturn(sampleResponse())

        recorder.record(name = "Model created", domain = "models", comment = "  ", operator = "")

        val captor = argumentCaptor<SystemEventCreateRequest>()
        verify(systemEventService).create(captor.capture())
        assertNull(captor.firstValue.comment)
        assertNull(captor.firstValue.`operator`)
    }

    @Test
    fun `record truncates oversized fields to protocol limits`() {
        whenever(systemEventService.create(any())).thenReturn(sampleResponse())
        val longName = "n".repeat(300)
        val longDomain = "d".repeat(300)
        val longOperator = "o".repeat(300)
        val longComment = "c".repeat(5000)

        recorder.record(
            name = longName,
            domain = longDomain,
            comment = longComment,
            operator = longOperator,
        )

        val captor = argumentCaptor<SystemEventCreateRequest>()
        verify(systemEventService).create(captor.capture())
        assertEquals(255, captor.firstValue.name.length)
        assertEquals(255, captor.firstValue.domain.length)
        assertEquals(255, captor.firstValue.`operator`!!.length)
        assertEquals(4000, captor.firstValue.comment!!.length)
    }

    private fun sampleResponse(): SystemEventResponse =
        SystemEventResponse(
            id = 1L,
            name = "Model created",
            domain = "models",
            `operator` = "ada",
            comment = null,
            occurredAt = Instant.parse("2026-10-26T12:00:00Z"),
            createdAt = Instant.parse("2026-10-26T12:00:00Z"),
            updatedAt = Instant.parse("2026-10-26T12:00:00Z"),
        )
}
