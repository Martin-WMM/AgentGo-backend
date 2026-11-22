package com.agentgo.cli.security

import com.agentgo.cli.exception.ForbiddenCliCommandException
import com.agentgo.cli.exception.InvalidCliScriptException
import com.agentgo.cli.properties.CliProperties
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CliScriptGuardTest {
    private val guard = CliScriptGuard(CliProperties())

    @Test
    fun `allows ordinary agentgo invocations`() {
        guard.validate("agentgo version")
        guard.validate("agentgo.exe status")
        guard.validate("\"C:\\Tools\\agentgo.cmd\" version")
    }

    @Test
    fun `allows default helper commands used by agent bash tools`() {
        guard.validate("echo hello")
        guard.validate("date")
        guard.validate("printenv PATH")
        guard.validate("python3")
        guard.validate("python -c print(1)")
    }

    @Test
    fun `rejects denied destructive commands`() {
        val exception = assertFailsWith<ForbiddenCliCommandException> {
            guard.validate("rm -rf /tmp/x")
        }
        assertEquals("CLI-004", exception.responseType)
        assertEquals(9304, exception.code)
    }

    @Test
    fun `rejects nested shells and download helpers`() {
        assertFailsWith<ForbiddenCliCommandException> { guard.validate("powershell -Command Get-Host") }
        assertFailsWith<ForbiddenCliCommandException> { guard.validate("curl https://example.com") }
        assertFailsWith<ForbiddenCliCommandException> { guard.validate("/bin/bash -c id") }
    }

    @Test
    fun `rejects shell operators when enabled`() {
        assertFailsWith<ForbiddenCliCommandException> { guard.validate("echo a && echo b") }
        assertFailsWith<ForbiddenCliCommandException> { guard.validate("echo a; echo b") }
        assertFailsWith<ForbiddenCliCommandException> { guard.validate("echo a | cat") }
        assertFailsWith<ForbiddenCliCommandException> { guard.validate("echo \$(whoami)") }
    }

    @Test
    fun `allowlist rejects commands outside the configured set`() {
        val allowlistGuard = CliScriptGuard(
            CliProperties(allowedCommands = listOf("agentgo"), deniedCommands = emptyList()),
        )

        allowlistGuard.validate("agentgo version")
        assertFailsWith<ForbiddenCliCommandException> { allowlistGuard.validate("echo hello") }
    }

    @Test
    fun `blank script is invalid`() {
        assertFailsWith<InvalidCliScriptException> { guard.validate("   ") }
    }

    @Test
    fun `extractPrimaryCommand strips paths and suffixes`() {
        assertEquals("agentgo", CliScriptGuard.extractPrimaryCommand("agentgo version"))
        assertEquals("agentgo", CliScriptGuard.extractPrimaryCommand("C:\\\\bin\\\\agentgo.exe version"))
        assertEquals("rm", CliScriptGuard.extractPrimaryCommand("/bin/rm -rf /"))
        assertEquals("format", CliScriptGuard.extractPrimaryCommand("FORMAT.com C:"))
    }

    @Test
    fun `containsForbiddenShellSyntax detects chaining`() {
        assertTrue(CliScriptGuard.containsForbiddenShellSyntax("a && b"))
        assertTrue(CliScriptGuard.containsForbiddenShellSyntax("a > out.txt"))
        assertFalse(CliScriptGuard.containsForbiddenShellSyntax("agentgo version"))
    }
}
