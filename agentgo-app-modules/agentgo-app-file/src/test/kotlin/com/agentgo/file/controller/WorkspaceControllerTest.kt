package com.agentgo.file.controller

import com.agentgo.commons.dto.http.response.status.success.WorkspaceResponseCodeRegistry
import com.agentgo.dto.workspace.WorkspaceCreateRequest
import com.agentgo.dto.workspace.WorkspaceResponse
import com.agentgo.dto.workspace.WorkspaceUpdateRequest
import com.agentgo.file.service.WorkspaceService
import com.agentgo.starter.i18n.AgentGoMessages
import java.time.Instant
import java.util.Locale
import java.util.UUID
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.context.support.StaticMessageSource
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.http.HttpStatus

class WorkspaceControllerTest {
    private val workspaceService = mock<WorkspaceService>()
    private val messageSource = StaticMessageSource().apply {
        addMessage("workspace.created", Locale.ENGLISH, "Workspace created")
        addMessage("workspace.created", Locale.SIMPLIFIED_CHINESE, "工作空间已创建")
        addMessage("workspace.retrieved", Locale.ENGLISH, "Workspace retrieved")
        addMessage("workspace.retrieved", Locale.SIMPLIFIED_CHINESE, "工作空间已获取")
        addMessage("workspace.listed", Locale.ENGLISH, "Workspaces listed")
        addMessage("workspace.listed", Locale.SIMPLIFIED_CHINESE, "工作空间列表已获取")
        addMessage("workspace.updated", Locale.ENGLISH, "Workspace updated")
        addMessage("workspace.updated", Locale.SIMPLIFIED_CHINESE, "工作空间已更新")
    }
    private val messages = AgentGoMessages(messageSource)
    private val controller = WorkspaceController(workspaceService, messages)

    @AfterTest
    fun clearLocale() {
        LocaleContextHolder.resetLocaleContext()
    }

    @Test
    fun `create wraps service result with created response code and english message`() {
        LocaleContextHolder.setLocale(Locale.ENGLISH)
        val request = WorkspaceCreateRequest().apply {
            name = "Reports"
            description = "Quarterly drafts"
        }
        val response = sampleResponse(UUID.randomUUID(), "Reports", "Quarterly drafts")
        whenever(workspaceService.create(request)).thenReturn(response)

        val created = controller.create(request)

        assertEquals(WorkspaceResponseCodeRegistry.CREATED, created.code)
        assertEquals(WorkspaceResponseCodeRegistry.CREATED_TYPE, created.responseType)
        assertEquals("Workspace created", created.message)
        assertEquals(response, created.data)
        verify(workspaceService).create(request)
    }

    @Test
    fun `create returns localized chinese message when accept language is zh-CN`() {
        LocaleContextHolder.setLocale(Locale.SIMPLIFIED_CHINESE)
        val request = WorkspaceCreateRequest().apply { name = "报告" }
        val response = sampleResponse(UUID.randomUUID(), "报告", null)
        whenever(workspaceService.create(request)).thenReturn(response)

        val created = controller.create(request)

        assertEquals("工作空间已创建", created.message)
        assertEquals(response, created.data)
    }

    @Test
    fun `get wraps service result with retrieved response code`() {
        LocaleContextHolder.setLocale(Locale.ENGLISH)
        val id = UUID.randomUUID()
        val response = sampleResponse(id, "Reports", null)
        whenever(workspaceService.get(id)).thenReturn(response)

        val retrieved = controller.get(id)

        assertEquals(WorkspaceResponseCodeRegistry.RETRIEVED, retrieved.code)
        assertEquals("Workspace retrieved", retrieved.message)
        assertEquals(response, retrieved.data)
        verify(workspaceService).get(id)
    }

    @Test
    fun `list wraps service result with list retrieved response code`() {
        LocaleContextHolder.setLocale(Locale.ENGLISH)
        val response = sampleResponse(UUID.randomUUID(), "Reports", null)
        whenever(workspaceService.list()).thenReturn(listOf(response))

        val listed = controller.list()

        assertEquals(WorkspaceResponseCodeRegistry.LIST_RETRIEVED, listed.code)
        assertEquals("Workspaces listed", listed.message)
        assertEquals(listOf(response), listed.data)
        verify(workspaceService).list()
    }

    @Test
    fun `update wraps service result with updated response code`() {
        LocaleContextHolder.setLocale(Locale.ENGLISH)
        val id = UUID.randomUUID()
        val request = WorkspaceUpdateRequest().apply {
            name = "Reports"
            description = "Updated"
        }
        val response = sampleResponse(id, "Reports", "Updated")
        whenever(workspaceService.update(id, request)).thenReturn(response)

        val updated = controller.update(id, request)

        assertEquals(WorkspaceResponseCodeRegistry.UPDATED, updated.code)
        assertEquals("Workspace updated", updated.message)
        assertEquals(response, updated.data)
        verify(workspaceService).update(id, request)
    }

    @Test
    fun `delete returns http 204 and delegates to service`() {
        val id = UUID.randomUUID()

        val deleted = controller.delete(id)

        assertEquals(HttpStatus.NO_CONTENT, deleted.statusCode)
        assertNull(deleted.body)
        verify(workspaceService).delete(id)
    }

    private fun sampleResponse(id: UUID, name: String, description: String?): WorkspaceResponse =
        WorkspaceResponse(
            id = id,
            name = name,
            description = description,
            createdAt = Instant.parse("2026-01-01T00:00:00Z"),
            updatedAt = Instant.parse("2026-01-02T00:00:00Z"),
        )
}
