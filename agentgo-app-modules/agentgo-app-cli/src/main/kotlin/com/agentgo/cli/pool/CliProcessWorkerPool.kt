package com.agentgo.cli.pool

import com.agentgo.cli.exception.CliPoolExhaustedException
import com.agentgo.cli.properties.CliProperties
import com.agentgo.cli.runner.DefaultCliProcessRunner
import jakarta.annotation.PreDestroy
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.InitializingBean
import org.springframework.stereotype.Component

/**
 * Fixed-size pool of CLI workers created during application startup.
 *
 * Each API call borrows one idle worker, runs the script, and returns the worker.
 * When every worker is busy, the caller waits up to [CliProperties.acquireTimeoutMs]
 * and then receives [CliPoolExhaustedException].
 *
 * @property properties pool size and acquire timeout
 */
@Component
class CliProcessWorkerPool(
    private val properties: CliProperties,
) : InitializingBean {
    private val log = LoggerFactory.getLogger(CliProcessWorkerPool::class.java)
    private val available = LinkedBlockingQueue<CliProcessWorker>()
    private val started = AtomicBoolean(false)

    /**
     * Creates [CliProperties.poolSize] workers (default 10) before the app accepts traffic.
     */
    override fun afterPropertiesSet() {
        if (!started.compareAndSet(false, true)) return
        val size = properties.poolSize.coerceAtLeast(1)
        repeat(size) { index ->
            val workerId = index + 1
            available.offer(
                CliProcessWorker(
                    id = workerId,
                    runner = DefaultCliProcessRunner(properties),
                ),
            )
        }
        log.info("Initialized CLI process worker pool with {} workers", size)
    }

    /**
     * Number of workers currently idle in the pool.
     */
    fun availableCount(): Int = available.size

    /**
     * Borrows a worker, runs [block], and always returns the worker to the pool.
     *
     * @param block work performed while holding an exclusive worker
     * @return result of [block]
     * @throws CliPoolExhaustedException when no worker becomes free in time
     */
    fun <T> withWorker(block: (CliProcessWorker) -> T): T {
        val worker = borrowWorker()
        try {
            return block(worker)
        } finally {
            releaseWorker(worker)
        }
    }

    private fun borrowWorker(): CliProcessWorker {
        val timeoutMs = properties.acquireTimeoutMs.coerceAtLeast(0)
        val worker = available.poll(timeoutMs, TimeUnit.MILLISECONDS)
            ?: throw CliPoolExhaustedException()
        log.debug("Borrowed CLI worker {}", worker.id)
        return worker
    }

    private fun releaseWorker(worker: CliProcessWorker) {
        available.offer(worker)
        log.debug("Released CLI worker {}", worker.id)
    }

    @PreDestroy
    fun shutdown() {
        available.clear()
        started.set(false)
        log.info("CLI process worker pool shut down")
    }
}
