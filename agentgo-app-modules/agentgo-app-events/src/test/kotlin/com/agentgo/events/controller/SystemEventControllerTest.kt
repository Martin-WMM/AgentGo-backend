package com.agentgo.events.controller

import com.agentgo.commons.dto.http.request.PageRequest
import com.agentgo.commons.dto.http.response.PageResponse
import com.agentgo.commons.dto.http.response.status.success.EventsResponseCodeRegistry
import com.agentgo.dto.events.SystemEventCreateRequest
import com.agentgo.dto.events.SystemEventResponse
import com.agentgo.dto.events.SystemEventUpdateRequest
import com.agentgo.events.service.SystemEventService
import com.agentgo.starter.i18n.AgentGoMessages
import java.time.Instant
import java.util.Locale
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.context.support.StaticMessageSource
import org.springframework.http.HttpStatus

class SystemEventControllerTest {
    private val systemEventService = mock<SystemEventService>()
    private val messageSource = StaticMessageSource().apply {
        addMessage("event.created", Locale.ENGLISH, "System event created")
        addMessage("event.created", Locale.SIMPLIFIED_CHINESE, "系统事件已创建")
        addMessage("event.retrieved", Locale.ENGLISH, "System event retrieved")
        addMessage("event.listed", Locale.ENGLISH, "System events listed")
        addMessage("event.listed", Locale.SIMPLIFIED_CHINESE, "系统事件列表已获取")
        addMessage("event.updated", Locale.ENGLISH, "System event updated")
    }
    private val controller = SystemEventController(systemEventService, AgentGoMessages(messageSource))

    @AfterTest
    fun clearLocale() {
        LocaleContextHolder.resetLocaleContext()
    }

    @Test
    fun `create wraps the service result with the created response code and english message`() {
        LocaleContextHolder.setLocale(Locale.ENGLISH)
        val request = SystemEventCreateRequest().apply {
            name = "Model created"
            domain = "models"
        }
        val response = sample()
        whenever(systemEventService.create(request)).thenReturn(response)

        val http = controller.create(request)

        assertEquals(EventsResponseCodeRegistry.CREATED, http.code)
        assertEquals(EventsResponseCodeRegistry.CREATED_TYPE, http.responseType)
        assertEquals("System event created", http.message)
        assertEquals(response, http.data)
        verify(systemEventService).create(request)
    }

    @Test
    fun `create selects the chinese message`() {
        LocaleContextHolder.setLocale(Locale.SIMPLIFIED_CHINESE)
        val request = SystemEventCreateRequest().apply {
            name = "Model created"
            domain = "models"
        }
        whenever(systemEventService.create(request)).thenReturn(sample())

        assertEquals("系统事件已创建", controller.create(request).message)
    }

    @Test
    fun `get wraps the service result with the retrieved response code`() {
        LocaleContextHolder.setLocale(Locale.ENGLISH)
        val response = sample()
        whenever(systemEventService.get(response.id)).thenReturn(response)

        val http = controller.get(response.id)

        assertEquals(EventsResponseCodeRegistry.RETRIEVED, http.code)
        assertEquals(EventsResponseCodeRegistry.RETRIEVED_TYPE, http.responseType)
        assertEquals("System event retrieved", http.message)
        assertEquals(response, http.data)
    }

    @Test
    fun `list wraps the page and passes the domain and operator filters`() {
        LocaleContextHolder.setLocale(Locale.ENGLISH)
        val request = PageRequest(page = 2, size = 10)
        val page = PageResponse(
            data = listOf(sample()),
            pageSize = 10,
            totalElements = 11,
            totalPages = 2,
            empty = false,
            first = false,
            last = true,
            size = 1,
            number = 2,
        )
        whenever(systemEventService.list(request, "models", "ada")).thenReturn(page)

        val http = controller.list(request, "models", "ada")

        assertEquals(EventsResponseCodeRegistry.LIST_RETRIEVED, http.code)
        assertEquals(EventsResponseCodeRegistry.LIST_RETRIEVED_TYPE, http.responseType)
        assertEquals("System events listed", http.message)
        assertEquals(page, http.data)
    }

    @Test
    fun `list selects the chinese message`() {
        LocaleContextHolder.setLocale(Locale.SIMPLIFIED_CHINESE)
        val request = PageRequest()
        whenever(systemEventService.list(request, null, null)).thenReturn(
            PageResponse(
                data = emptyList(),
                pageSize = 20,
                totalElements = 0,
                totalPages = 0,
                empty = true,
                first = true,
                last = true,
                size = 0,
                number = 1,
            ),
        )

        assertEquals("系统事件列表已获取", controller.list(request, null, null).message)
    }

    @Test
    fun `update wraps the service result with the updated response code`() {
        LocaleContextHolder.setLocale(Locale.ENGLISH)
        val request = SystemEventUpdateRequest().apply {
            name = "Model renamed"
            domain = "models"
        }
        val response = sample(name = "Model renamed")
        whenever(systemEventService.update(response.id, request)).thenReturn(response)

        val http = controller.update(response.id, request)

        assertEquals(EventsResponseCodeRegistry.UPDATED, http.code)
        assertEquals(EventsResponseCodeRegistry.UPDATED_TYPE, http.responseType)
        assertEquals("System event updated", http.message)
        assertEquals("Model renamed", http.data?.name)
        verify(systemEventService).update(response.id, request)
    }

    @Test
    fun `delete returns no content and delegates to the service`() {
        val id = 1L

        val response = controller.delete(id)

        assertEquals(HttpStatus.NO_CONTENT, response.statusCode)
        assertNull(response.body)
        verify(systemEventService).delete(id)
    }

    private fun sample(name: String = "Model created") = SystemEventResponse(
        id = 2L,
        name = name,
        domain = "models",
        `operator` = "ada",
        comment = "Created the default model.",
        occurredAt = Instant.parse("2026-10-26T12:00:00Z"),
        createdAt = Instant.parse("2026-10-26T12:00:01Z"),
        updatedAt = Instant.parse("2026-10-26T12:00:01Z"),
    )
}
