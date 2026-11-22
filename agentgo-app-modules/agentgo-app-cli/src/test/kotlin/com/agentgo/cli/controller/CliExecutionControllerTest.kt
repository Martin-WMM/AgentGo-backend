package com.agentgo.cli.controller

import com.agentgo.commons.dto.http.response.status.success.CliResponseCodeRegistry
import com.agentgo.dto.cli.CliExecutionRequest
import com.agentgo.dto.cli.CliExecutionResponse
import com.agentgo.dto.cli.CliExecutionStatus
import com.agentgo.cli.service.CliExecutionService
import com.agentgo.starter.i18n.AgentGoMessages
import java.util.Locale
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.context.support.StaticMessageSource

class CliExecutionControllerTest {
    private val cliExecutionService = mock<CliExecutionService>()
    private val messageSource = StaticMessageSource().apply {
        addMessage("cli.execution.completed", Locale.ENGLISH, "CLI execution completed")
        addMessage("cli.execution.completed", Locale.SIMPLIFIED_CHINESE, "CLI 执行已完成")
    }
    private val controller = CliExecutionController(cliExecutionService, AgentGoMessages(messageSource))

    @AfterTest
    fun clearLocale() {
        LocaleContextHolder.resetLocaleContext()
    }

    @Test
    fun `execute wraps service result with executed response code and english message`() {
        LocaleContextHolder.setLocale(Locale.ENGLISH)
        val request = CliExecutionRequest().apply { script = "echo ok" }
        val response = sampleResponse()
        whenever(cliExecutionService.execute(request)).thenReturn(response)

        val http = controller.execute(request)

        assertEquals(CliResponseCodeRegistry.EXECUTED, http.code)
        assertEquals(CliResponseCodeRegistry.EXECUTED_TYPE, http.responseType)
        assertEquals("CLI execution completed", http.message)
        assertEquals(response, http.data)
        verify(cliExecutionService).execute(request)
    }

    @Test
    fun `execute selects the chinese message`() {
        LocaleContextHolder.setLocale(Locale.SIMPLIFIED_CHINESE)
        val request = CliExecutionRequest().apply { script = "echo ok" }
        whenever(cliExecutionService.execute(request)).thenReturn(sampleResponse())

        assertEquals("CLI 执行已完成", controller.execute(request).message)
    }

    private fun sampleResponse() = CliExecutionResponse(
        exitCode = 0,
        stdout = "ok\n",
        stderr = "",
        durationMs = 15,
        status = CliExecutionStatus.COMPLETED,
    )
}