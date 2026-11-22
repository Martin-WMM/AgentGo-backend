package com.agentgo.cli.controller

import com.agentgo.cli.exception.CliFailedToStartException
import com.agentgo.cli.exception.CliPoolExhaustedException
import com.agentgo.cli.exception.ForbiddenCliCommandException
import com.agentgo.cli.exception.InvalidCliScriptException
import com.agentgo.commons.dto.http.response.status.ResponseCodeDescription
import com.agentgo.starter.i18n.AgentGoMessages
import java.util.Locale
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.context.support.StaticMessageSource
import org.springframework.http.HttpStatus

class CliExceptionHandlerTest {
    private val messageSource = StaticMessageSource().apply {
        addMessage("CLI-001", Locale.ENGLISH, "The CLI script is invalid.")
        addMessage("CLI-001", Locale.SIMPLIFIED_CHINESE, "CLI 脚本无效。")
        addMessage("CLI-002", Locale.ENGLISH, "The CLI process could not be started.")
        addMessage("CLI-002", Locale.SIMPLIFIED_CHINESE, "无法启动 CLI 进程。")
        addMessage("CLI-003", Locale.ENGLISH, "All CLI workers are busy. Retry later.")
        addMessage("CLI-003", Locale.SIMPLIFIED_CHINESE, "CLI 执行池已满，请稍后重试。")
        addMessage("CLI-004", Locale.ENGLISH, "This CLI command is not allowed.")
        addMessage("CLI-004", Locale.SIMPLIFIED_CHINESE, "不允许执行此 CLI 命令。")
    }
    private val handler = CliExceptionHandler(AgentGoMessages(messageSource))

    @AfterTest
    fun clearLocale() {
        LocaleContextHolder.resetLocaleContext()
    }

    @Test
    fun `maps invalid script to localized english envelope`() {
        LocaleContextHolder.setLocale(Locale.ENGLISH)

        val response = handler.handleCliException(InvalidCliScriptException())

        assertEquals(400, response.statusCode.value())
        assertEquals(9301, response.body?.code)
        assertEquals("CLI-001", response.body?.responseType)
        assertEquals("The CLI script is invalid.", response.body?.message)
        assertFalse(response.body?.success == true)
    }

    @Test
    fun `maps failed to start to localized chinese envelope`() {
        LocaleContextHolder.setLocale(Locale.SIMPLIFIED_CHINESE)

        val response = handler.handleCliException(CliFailedToStartException())

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.statusCode)
        assertEquals(9302, response.body?.code)
        assertEquals("CLI-002", response.body?.responseType)
        assertEquals("无法启动 CLI 进程。", response.body?.message)
        assertEquals("CLI", (response.body?.error as? ResponseCodeDescription)?.category)
    }

    @Test
    fun `maps pool exhausted to service unavailable envelope`() {
        LocaleContextHolder.setLocale(Locale.ENGLISH)

        val response = handler.handleCliException(CliPoolExhaustedException())

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.statusCode)
        assertEquals(9303, response.body?.code)
        assertEquals("CLI-003", response.body?.responseType)
        assertEquals("All CLI workers are busy. Retry later.", response.body?.message)
    }

    @Test
    fun `maps forbidden command to chinese forbidden envelope`() {
        LocaleContextHolder.setLocale(Locale.SIMPLIFIED_CHINESE)

        val response = handler.handleCliException(ForbiddenCliCommandException())

        assertEquals(HttpStatus.FORBIDDEN, response.statusCode)
        assertEquals(9304, response.body?.code)
        assertEquals("CLI-004", response.body?.responseType)
        assertEquals("不允许执行此 CLI 命令。", response.body?.message)
    }
}