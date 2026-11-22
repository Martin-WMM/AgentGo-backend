package com.agentgo.cli.entity

import com.agentgo.dto.cli.CliExecutionStatus
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import java.time.Instant
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener

/**
 * Persistent audit row for one authenticated CLI / shell execution.
 *
 * @property id persistent primary key
 * @property ownerUserId authenticated operator identifier
 * @property script submitted shell script or command
 * @property environment environment variables applied to the process
 * @property exitCode process exit code, or `-1` when unavailable
 * @property stdout captured standard output
 * @property stderr captured standard error
 * @property durationMs wall-clock duration in milliseconds
 * @property status completion status
 * @property executedAt instant when the run started
 * @property creator audit actor that created the row
 * @property createdAt creation timestamp
 * @property updatedAt last modification timestamp
 * @property updatedBy audit actor that last modified the row
 */
@Entity
@Table(
    name = "tbl_cli_execution",
    indexes = [
        Index(name = "idx_tbl_cli_execution_owner_executed_at", columnList = "owner_user_id, executed_at"),
        Index(name = "idx_tbl_cli_execution_executed_at", columnList = "executed_at"),
    ],
)
@EntityListeners(AuditingEntityListener::class)
class CliExecutionEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(
        name = "owner_user_id",
        nullable = false,
        updatable = false,
        length = 255,
        comment = "OIDC subject of the authenticated user that ran the command",
    )
    var ownerUserId: String = "",

    @Column(name = "script", nullable = false, columnDefinition = "text", comment = "Shell script or command submitted by the caller")
    var script: String = "",

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(
        name = "environment",
        nullable = false,
        comment = "Environment variables supplied with the execution, stored as a JSON object",
    )
    var environment: Map<String, String> = emptyMap(),

    @Column(
        name = "exit_code",
        nullable = false,
        comment = "Process exit code, or -1 when unavailable",
    )
    var exitCode: Int = -1,

    @Column(name = "stdout", nullable = false, columnDefinition = "text", comment = "Captured standard output")
    var stdout: String = "",

    @Column(name = "stderr", nullable = false, columnDefinition = "text", comment = "Captured standard error")
    var stderr: String = "",

    @Column(name = "duration_ms", nullable = false, comment = "Wall-clock duration in milliseconds")
    var durationMs: Long = 0,

    @Enumerated(EnumType.STRING)
    @Column(
        name = "status",
        nullable = false,
        length = 32,
        comment = "Execution outcome: COMPLETED, TIMED_OUT, FAILED_TO_START, or REJECTED",
    )
    var status: CliExecutionStatus = CliExecutionStatus.COMPLETED,

    @Column(name = "executed_at", nullable = false, comment = "Instant when the execution started")
    var executedAt: Instant = Instant.EPOCH,

    @CreatedBy
    @Column(name = "creator", nullable = false, updatable = false, length = 255, comment = "Identifier of the actor that created the audit row")
    var creator: String? = null,

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false, comment = "Audit row creation timestamp")
    var createdAt: Instant? = null,

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false, comment = "Audit row last update timestamp")
    var updatedAt: Instant? = null,

    @LastModifiedBy
    @Column(name = "updated_by", nullable = false, length = 255, comment = "Identifier of the actor that last updated the audit row")
    var updatedBy: String? = null,
)
