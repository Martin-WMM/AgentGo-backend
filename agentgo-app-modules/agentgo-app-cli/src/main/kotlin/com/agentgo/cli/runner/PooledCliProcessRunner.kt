package com.agentgo.cli.runner

import com.agentgo.cli.pool.CliProcessWorkerPool
import org.springframework.stereotype.Component

/**
 * [CliProcessRunner] that executes each request on a borrowed pool worker.
 *
 * @property workerPool fixed worker pool initialized at application startup
 */
@Component
class PooledCliProcessRunner(
    private val workerPool: CliProcessWorkerPool,
) : CliProcessRunner {
    override fun run(script: String, environment: Map<String, String>): CliRunOutcome =
        workerPool.withWorker { worker ->
            worker.run(script, environment)
        }
}
