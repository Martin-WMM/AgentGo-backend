package com.agentgo.cli.pool

import com.agentgo.cli.runner.CliProcessRunner
import com.agentgo.cli.runner.CliRunOutcome

/**
 * One reusable CLI execution slot in the worker pool.
 *
 * A worker does not keep a long-lived OS process. It owns a [CliProcessRunner] and
 * may run at most one script at a time while checked out from the pool.
 *
 * @property id one-based worker identifier assigned at pool startup
 * @property runner process runner used while this worker is checked out
 */
class CliProcessWorker(
    val id: Int,
    private val runner: CliProcessRunner,
) {
    /**
     * Executes [script] through this worker's runner.
     *
     * @param script shell command or script body
     * @param environment optional environment variables
     * @return captured process outcome
     */
    fun run(script: String, environment: Map<String, String>): CliRunOutcome =
        runner.run(script, environment)
}
