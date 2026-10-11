package com.agentgo.file.controller

import com.agentgo.file.exception.DuplicateWorkspaceNameException
import com.agentgo.file.exception.WorkspaceNotFoundException
import com.agentgo.starter.i18n.AgentGoMessages
import java.util.Locale
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.context.support.StaticMessageSource

class WorkspaceExceptionHandlerTest {
    private val messageSource = StaticMessageSource().apply {
        addMessage("WORKSPACE-001", Locale.ENGLISH, "The requested workspace was not found.")
        addMessage("WORKSPACE-001", Locale.SIMPLIFIED_CHINESE, "未找到请求的工作空间。")
        addMessage("WORKSPACE-003", Locale.ENGLISH, "A workspace with this name already exists.")
        addMessage("WORKSPACE-003", Locale.SIMPLIFIED_CHINESE, "已存在同名工作空间。")
    }
    private val handler = WorkspaceExceptionHandler(AgentGoMessages(messageSource))

    @AfterTest
    fun clearLocale() {
        LocaleContextHolder.resetLocaleContext()
    }

    @Test
    fun `maps not found exception to localized english envelope`() {
        LocaleContextHolder.setLocale(Locale.ENGLISH)

        val response = handler.handleWorkspaceException(WorkspaceNotFoundException())

        assertEquals(404, response.statusCode.value())
        assertEquals(2301, response.body?.code)
        assertEquals("WORKSPACE-001", response.body?.responseType)
        assertEquals("The requested workspace was not found.", response.body?.message)
        assertFalse(response.body?.success == true)
    }

    @Test
    fun `maps duplicate name exception to localized chinese envelope`() {
        LocaleContextHolder.setLocale(Locale.SIMPLIFIED_CHINESE)

        val response = handler.handleWorkspaceException(DuplicateWorkspaceNameException())

        assertEquals(409, response.statusCode.value())
        assertEquals(2303, response.body?.code)
        assertEquals("WORKSPACE-003", response.body?.responseType)
        assertEquals("已存在同名工作空间。", response.body?.message)
    }
}
