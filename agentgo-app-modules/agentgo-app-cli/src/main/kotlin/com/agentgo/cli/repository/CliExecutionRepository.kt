package com.agentgo.cli.repository

import com.agentgo.cli.entity.CliExecutionEntity
import org.springframework.data.jpa.repository.JpaRepository

/**
 * Persistence access for CLI execution audit rows.
 */
interface CliExecutionRepository : JpaRepository<CliExecutionEntity, Long>
