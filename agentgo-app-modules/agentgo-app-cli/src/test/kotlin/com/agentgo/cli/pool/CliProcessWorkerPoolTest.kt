package com.agentgo.cli.pool

import com.agentgo.cli.exception.CliPoolExhaustedException
import com.agentgo.cli.properties.CliProperties
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class CliProcessWorkerPoolTest {
    @Test
    fun `initializes the configured number of workers at startup`() {
        val pool = CliProcessWorkerPool(
            CliProperties(poolSize = 10, acquireTimeoutMs = 100),
        )
        pool.afterPropertiesSet()

        assertEquals(10, pool.availableCount())
        pool.shutdown()
    }

    @Test
    fun `withWorker borrows and returns the same worker`() {
        val pool = CliProcessWorkerPool(
            CliProperties(poolSize = 2, acquireTimeoutMs = 200),
        )
        pool.afterPropertiesSet()

        val seen = mutableSetOf<Int>()
        pool.withWorker { worker ->
            seen += worker.id
            assertEquals(1, pool.availableCount())
        }
        assertEquals(2, pool.availableCount())
        assertTrue(seen.isNotEmpty())
        pool.shutdown()
    }

    @Test
    fun `throws when every worker is busy past the acquire timeout`() {
        val pool = CliProcessWorkerPool(
            CliProperties(poolSize = 1, acquireTimeoutMs = 100),
        )
        pool.afterPropertiesSet()

        val started = CountDownLatch(1)
        val release = CountDownLatch(1)
        val executor = Executors.newSingleThreadExecutor()
        try {
            executor.submit {
                pool.withWorker {
                    started.countDown()
                    release.await(2, TimeUnit.SECONDS)
                }
            }
            assertTrue(started.await(2, TimeUnit.SECONDS))

            assertFailsWith<CliPoolExhaustedException> {
                pool.withWorker { error("should not run") }
            }
        } finally {
            release.countDown()
            executor.shutdownNow()
            pool.shutdown()
        }
    }

    @Test
    fun `releases worker even when the borrowed work throws`() {
        val pool = CliProcessWorkerPool(
            CliProperties(poolSize = 1, acquireTimeoutMs = 200),
        )
        pool.afterPropertiesSet()

        assertFailsWith<IllegalStateException> {
            pool.withWorker { throw IllegalStateException("boom") }
        }
        assertEquals(1, pool.availableCount())
        pool.shutdown()
    }

    @Test
    fun `supports concurrent borrows up to pool size`() {
        val pool = CliProcessWorkerPool(
            CliProperties(poolSize = 3, acquireTimeoutMs = 1_000),
        )
        pool.afterPropertiesSet()

        val inFlight = AtomicInteger(0)
        val maxInFlight = AtomicInteger(0)
        val ready = CountDownLatch(3)
        val release = CountDownLatch(1)
        val executor = Executors.newFixedThreadPool(3)
        try {
            repeat(3) {
                executor.submit {
                    pool.withWorker {
                        val current = inFlight.incrementAndGet()
                        maxInFlight.updateAndGet { maxOf(it, current) }
                        ready.countDown()
                        release.await(2, TimeUnit.SECONDS)
                        inFlight.decrementAndGet()
                    }
                }
            }
            assertTrue(ready.await(2, TimeUnit.SECONDS))
            assertEquals(3, maxInFlight.get())
            assertEquals(0, pool.availableCount())
        } finally {
            release.countDown()
            executor.shutdownNow()
            pool.shutdown()
        }
    }
}
