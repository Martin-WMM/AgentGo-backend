package com.agentgo.cli.repository

import com.agentgo.cli.entity.CliExecutionEntity
import com.agentgo.cli.support.CliRepositoryAuditingConfiguration
import com.agentgo.dto.cli.CliExecutionStatus
import java.time.Instant
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.boot.persistence.autoconfigure.EntityScan
import org.springframework.context.annotation.Import
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.test.context.ActiveProfiles

@DataJpaTest
@ActiveProfiles("test")
@Import(CliRepositoryAuditingConfiguration::class)
@EntityScan(basePackageClasses = [CliExecutionEntity::class])
@EnableJpaRepositories(basePackageClasses = [CliExecutionRepository::class])
class CliExecutionRepositoryTest @Autowired constructor(
    private val cliExecutionRepository: CliExecutionRepository,
) {
    @AfterTest
    fun clearSecurityContext() {
        SecurityContextHolder.clearContext()
    }

    @Test
    fun `save assigns id audit fields and persists the execution row`() {
        authenticateAs("subject-ada")

        val saved = cliExecutionRepository.saveAndFlush(
            CliExecutionEntity(
                ownerUserId = "subject-ada",
                script = "echo hello",
                environment = mapOf("FOO" to "bar"),
                exitCode = 0,
                stdout = "hello\n",
                stderr = "",
                durationMs = 12,
                status = CliExecutionStatus.COMPLETED,
                executedAt = Instant.parse("2026-11-22T06:00:00Z"),
            ),
        )

        assertNotNull(saved.id)
        assertEquals("subject-ada", saved.creator)
        assertEquals("subject-ada", saved.updatedBy)
        assertNotNull(saved.createdAt)
        assertNotNull(saved.updatedAt)

        val reloaded = cliExecutionRepository.findById(saved.id!!).orElseThrow()
        assertEquals("echo hello", reloaded.script)
        assertEquals(mapOf("FOO" to "bar"), reloaded.environment)
        assertEquals(0, reloaded.exitCode)
        assertEquals(CliExecutionStatus.COMPLETED, reloaded.status)
        assertEquals(Instant.parse("2026-11-22T06:00:00Z"), reloaded.executedAt)
    }

    private fun authenticateAs(subject: String) {
        SecurityContextHolder.getContext().authentication =
            UsernamePasswordAuthenticationToken(subject, "n/a", AuthorityUtils.NO_AUTHORITIES)
    }
}
