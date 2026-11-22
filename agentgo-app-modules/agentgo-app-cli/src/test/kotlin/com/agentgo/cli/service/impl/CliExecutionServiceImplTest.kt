package com.agentgo.cli.service.impl

import com.agentgo.cli.entity.CliExecutionEntity
import com.agentgo.cli.exception.CliFailedToStartException
import com.agentgo.cli.exception.ForbiddenCliCommandException
import com.agentgo.cli.repository.CliExecutionRepository
import com.agentgo.cli.runner.CliProcessRunner
import com.agentgo.cli.runner.CliRunOutcome
import com.agentgo.cli.security.CliScriptGuard
import com.agentgo.cli.security.CliUserIdResolver
import com.agentgo.dto.cli.CliExecutionRequest
import com.agentgo.dto.cli.CliExecutionStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class CliExecutionServiceImplTest {
    private val cliProcessRunner = mock<CliProcessRunner>()
    private val cliExecutionRepository = mock<CliExecutionRepository>()
    private val cliUserIdResolver = mock<CliUserIdResolver>()
    private val cliScriptGuard = mock<CliScriptGuard>()
    private val service = CliExecutionServiceImpl(
        cliProcessRunner,
        cliExecutionRepository,
        cliUserIdResolver,
        cliScriptGuard,
    )

    @Test
    fun `execute maps runner outcome to response and persists an audit row`() {
        whenever(cliUserIdResolver.requireUserId()).thenReturn("ada")
        val request = CliExecutionRequest().apply {
            script = "echo hello"
            environment = mapOf("FOO" to "bar")
        }
        whenever(cliProcessRunner.run("echo hello", mapOf("FOO" to "bar"))).thenReturn(
            CliRunOutcome(
                exitCode = 0,
                stdout = "hello\n",
                stderr = "",
                durationMs = 20,
                status = CliExecutionStatus.COMPLETED,
            ),
        )
        whenever(cliExecutionRepository.save(any<CliExecutionEntity>())).thenAnswer { it.arguments[0] }

        val response = service.execute(request)

        assertEquals(0, response.exitCode)
        assertEquals("hello\n", response.stdout)
        assertEquals("", response.stderr)
        assertEquals(20, response.durationMs)
        assertEquals(CliExecutionStatus.COMPLETED, response.status)
        verify(cliScriptGuard).validate("echo hello")

        val captor = argumentCaptor<CliExecutionEntity>()
        verify(cliExecutionRepository).save(captor.capture())
        val audit = captor.firstValue
        assertEquals("ada", audit.ownerUserId)
        assertEquals("echo hello", audit.script)
        assertEquals(mapOf("FOO" to "bar"), audit.environment)
        assertEquals(0, audit.exitCode)
        assertEquals("hello\n", audit.stdout)
        assertEquals(CliExecutionStatus.COMPLETED, audit.status)
    }

    @Test
    fun `execute passes empty environment when request omits map`() {
        whenever(cliUserIdResolver.requireUserId()).thenReturn("ada")
        val request = CliExecutionRequest().apply { script = "pwd" }
        whenever(cliProcessRunner.run("pwd", emptyMap())).thenReturn(
            CliRunOutcome(
                exitCode = 0,
                stdout = "/",
                stderr = "",
                durationMs = 5,
                status = CliExecutionStatus.COMPLETED,
            ),
        )
        whenever(cliExecutionRepository.save(any<CliExecutionEntity>())).thenAnswer { it.arguments[0] }

        service.execute(request)

        verify(cliProcessRunner).run("pwd", emptyMap())
        val captor = argumentCaptor<CliExecutionEntity>()
        verify(cliExecutionRepository).save(captor.capture())
        assertEquals(emptyMap(), captor.firstValue.environment)
    }

    @Test
    fun `execute persists a failed-to-start audit row before rethrowing`() {
        whenever(cliUserIdResolver.requireUserId()).thenReturn("ada")
        val request = CliExecutionRequest().apply { script = "boom" }
        whenever(cliProcessRunner.run("boom", emptyMap())).thenThrow(CliFailedToStartException())
        whenever(cliExecutionRepository.save(any<CliExecutionEntity>())).thenAnswer { it.arguments[0] }

        assertFailsWith<CliFailedToStartException> { service.execute(request) }

        val captor = argumentCaptor<CliExecutionEntity>()
        verify(cliExecutionRepository).save(captor.capture())
        val audit = captor.firstValue
        assertEquals("ada", audit.ownerUserId)
        assertEquals("boom", audit.script)
        assertEquals(-1, audit.exitCode)
        assertEquals(CliExecutionStatus.FAILED_TO_START, audit.status)
    }

    @Test
    fun `execute persists a rejected audit row and skips the runner`() {
        whenever(cliUserIdResolver.requireUserId()).thenReturn("ada")
        val request = CliExecutionRequest().apply { script = "rm -rf /" }
        doThrow(ForbiddenCliCommandException("The CLI command \"rm\" is not allowed."))
            .whenever(cliScriptGuard)
            .validate("rm -rf /")
        whenever(cliExecutionRepository.save(any<CliExecutionEntity>())).thenAnswer { it.arguments[0] }

        assertFailsWith<ForbiddenCliCommandException> { service.execute(request) }

        verify(cliProcessRunner, never()).run(any(), any())
        val captor = argumentCaptor<CliExecutionEntity>()
        verify(cliExecutionRepository).save(captor.capture())
        val audit = captor.firstValue
        assertEquals(CliExecutionStatus.REJECTED, audit.status)
        assertEquals(-1, audit.exitCode)
        assertEquals("The CLI command \"rm\" is not allowed.", audit.stderr)
    }
}
