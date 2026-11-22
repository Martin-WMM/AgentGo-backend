package com.agentgo.cli.config

import com.agentgo.cli.controller.CliExceptionHandler
import com.agentgo.cli.controller.CliExecutionController
import com.agentgo.cli.pool.CliProcessWorkerPool
import com.agentgo.cli.properties.CliProperties
import com.agentgo.cli.repository.CliExecutionRepository
import com.agentgo.cli.runner.PooledCliProcessRunner
import com.agentgo.cli.security.CliScriptGuard
import com.agentgo.cli.security.CliUserIdResolver
import com.agentgo.cli.service.impl.CliExecutionServiceImpl
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.persistence.autoconfigure.EntityScan
import org.springframework.context.annotation.Import
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

/**
 * Auto-configuration entry point for authenticated CLI execution and audit persistence.
 *
 * A fixed worker pool is created at startup; each request borrows one worker to run the script.
 */
@AutoConfiguration
@EnableConfigurationProperties(CliProperties::class)
@EntityScan("com.agentgo.cli.entity")
@EnableJpaRepositories(basePackageClasses = [CliExecutionRepository::class])
@Import(
    CliExecutionController::class,
    CliExceptionHandler::class,
    CliProcessWorkerPool::class,
    PooledCliProcessRunner::class,
    CliExecutionServiceImpl::class,
    CliUserIdResolver::class,
    CliScriptGuard::class,
)
class CliAutoConfiguration
