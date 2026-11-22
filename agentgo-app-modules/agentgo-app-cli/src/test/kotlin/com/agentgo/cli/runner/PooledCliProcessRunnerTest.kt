package com.agentgo.cli.runner

import com.agentgo.cli.pool.CliProcessWorkerPool
import com.agentgo.cli.properties.CliProperties
import com.agentgo.dto.cli.CliExecutionStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PooledCliProcessRunnerTest {
    @Test
    fun `runs through a borrowed worker from the pool`() {
        val pool = CliProcessWorkerPool(
            CliProperties(
                timeoutMs = 10_000,
                maxStdoutChars = 10_000,
                maxStderrChars = 10_000,
                poolSize = 2,
                acquireTimeoutMs = 1_000,
            ),
        )
        pool.afterPropertiesSet()
        try {
            val runner = PooledCliProcessRunner(pool)
            val outcome = runner.run("echo POOL_OK", emptyMap())

            assertEquals(CliExecutionStatus.COMPLETED, outcome.status)
            assertEquals(0, outcome.exitCode)
            assertTrue(outcome.stdout.contains("POOL_OK"))
            assertEquals(2, pool.availableCount())
        } finally {
            pool.shutdown()
        }
    }
}
