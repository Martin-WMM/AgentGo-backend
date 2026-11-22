package com.agentgo.cli.service.impl

import com.agentgo.cli.entity.CliExecutionEntity
import com.agentgo.cli.exception.CliFailedToStartException
import com.agentgo.cli.exception.ForbiddenCliCommandException
import com.agentgo.cli.repository.CliExecutionRepository
import com.agentgo.cli.runner.CliProcessRunner
import com.agentgo.cli.security.CliScriptGuard
import com.agentgo.cli.security.CliUserIdResolver
import com.agentgo.cli.service.CliExecutionService
import com.agentgo.dto.cli.CliExecutionRequest
import com.agentgo.dto.cli.CliExecutionResponse
import com.agentgo.dto.cli.CliExecutionStatus
import java.time.Instant
import org.springframework.stereotype.Service

/**
 * Runs validated CLI execution requests and persists an audit row for each run.
 *
 * @property cliProcessRunner platform process runner
 * @property cliExecutionRepository audit persistence
 * @property cliUserIdResolver authenticated operator resolver
 * @property cliScriptGuard command security policy
 */
@Service
class CliExecutionServiceImpl(
    private val cliProcessRunner: CliProcessRunner,
    private val cliExecutionRepository: CliExecutionRepository,
    private val cliUserIdResolver: CliUserIdResolver,
    private val cliScriptGuard: CliScriptGuard,
) : CliExecutionService {
    override fun execute(request: CliExecutionRequest): CliExecutionResponse {
        val ownerUserId = cliUserIdResolver.requireUserId()
        val script = request.script
        val environment = request.environment.orEmpty()
        val executedAt = Instant.now()

        try {
            cliScriptGuard.validate(script)
        } catch (exception: ForbiddenCliCommandException) {
            persistAudit(
                ownerUserId = ownerUserId,
                script = script,
                environment = environment,
                exitCode = -1,
                stdout = "",
                stderr = exception.message,
                durationMs = 0,
                status = CliExecutionStatus.REJECTED,
                executedAt = executedAt,
            )
            throw exception
        }

        try {
            val outcome = cliProcessRunner.run(script = script, environment = environment)
            val response = CliExecutionResponse(
                exitCode = outcome.exitCode,
                stdout = outcome.stdout,
                stderr = outcome.stderr,
                durationMs = outcome.durationMs,
                status = outcome.status,
            )
            persistAudit(
                ownerUserId = ownerUserId,
                script = script,
                environment = environment,
                exitCode = response.exitCode,
                stdout = response.stdout,
                stderr = response.stderr,
                durationMs = response.durationMs,
                status = response.status,
                executedAt = executedAt,
            )
            return response
        } catch (exception: CliFailedToStartException) {
            persistAudit(
                ownerUserId = ownerUserId,
                script = script,
                environment = environment,
                exitCode = -1,
                stdout = "",
                stderr = exception.message,
                durationMs = 0,
                status = CliExecutionStatus.FAILED_TO_START,
                executedAt = executedAt,
            )
            throw exception
        }
    }

    private fun persistAudit(
        ownerUserId: String,
        script: String,
        environment: Map<String, String>,
        exitCode: Int,
        stdout: String,
        stderr: String,
        durationMs: Long,
        status: CliExecutionStatus,
        executedAt: Instant,
    ) {
        cliExecutionRepository.save(
            CliExecutionEntity(
                ownerUserId = ownerUserId,
                script = script,
                environment = environment,
                exitCode = exitCode,
                stdout = stdout,
                stderr = stderr,
                durationMs = durationMs,
                status = status,
                executedAt = executedAt,
            ),
        )
    }
}
