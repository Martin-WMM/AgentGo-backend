package com.agentgo.ai.tool

import com.agentgo.core.tool.ToolRuntimeContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class RuntimeContextEnvironmentTest {
    @Test
    fun `runtime context overwrites model extras for protected keys`() {
        val env =
            RuntimeContextEnvironment.build(
                context =
                    ToolRuntimeContext(
                        runId = "r1",
                        threadId = "t1",
                        headers = mapOf("Accept-Language" to "en"),
                        metadata = mapOf("ownerUserId" to "ada", "idToken" to "tok"),
                    ),
                extras = mapOf(
                    "FOO" to "bar",
                    RuntimeContextEnvironment.USER_ID to "spoofed",
                    RuntimeContextEnvironment.TOKEN to "spoofed-token",
                ),
            )

        assertEquals("bar", env["FOO"])
        assertEquals("ada", env[RuntimeContextEnvironment.USER_ID])
        assertEquals("tok", env[RuntimeContextEnvironment.TOKEN])
        assertEquals("en", env[RuntimeContextEnvironment.LOCALE])
        assertEquals("en.UTF-8", env[RuntimeContextEnvironment.LANG])
        assertEquals("r1", env[RuntimeContextEnvironment.RUN_ID])
        assertEquals("t1", env[RuntimeContextEnvironment.THREAD_ID])
    }

    @Test
    fun `blank context values are omitted`() {
        val env = RuntimeContextEnvironment.build(ToolRuntimeContext.EMPTY)

        assertNull(env[RuntimeContextEnvironment.USER_ID])
        assertNull(env[RuntimeContextEnvironment.TOKEN])
        assertNull(env[RuntimeContextEnvironment.RUN_ID])
    }
}
