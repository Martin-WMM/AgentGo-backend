package com.agentgo.events.repository

import com.agentgo.events.entity.SystemEventEntity
import com.agentgo.events.support.SystemEventRepositoryAuditingConfiguration
import java.time.Instant
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.boot.persistence.autoconfigure.EntityScan
import org.springframework.context.annotation.Import
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.domain.Specification
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.test.context.ActiveProfiles

@DataJpaTest
@ActiveProfiles("test")
@Import(SystemEventRepositoryAuditingConfiguration::class)
@EntityScan(basePackageClasses = [SystemEventEntity::class])
@EnableJpaRepositories(basePackageClasses = [SystemEventRepository::class])
class SystemEventRepositoryTest @Autowired constructor(
    private val systemEventRepository: SystemEventRepository,
) {
    @AfterTest
    fun clearSecurityContext() {
        SecurityContextHolder.clearContext()
    }

    @Test
    fun `save assigns id audit fields and persists the event row`() {
        authenticateAs("ada")

        val saved = systemEventRepository.saveAndFlush(event(name = "Model created"))

        assertNotNull(saved.id)
        assertEquals("ada", saved.creator)
        assertEquals("ada", saved.updatedBy)
        assertNotNull(saved.createdAt)
        assertNotNull(saved.updatedAt)
        assertEquals("models", systemEventRepository.findById(saved.id!!).orElseThrow().domain)
    }

    @Test
    fun `deleteOlderThan removes only events before the cutoff`() {
        authenticateAs("ada")
        val cutoff = Instant.parse("2026-07-26T12:00:00Z")
        systemEventRepository.saveAndFlush(event(name = "old", occurredAt = cutoff.minusSeconds(1)))
        val kept = systemEventRepository.saveAndFlush(event(name = "kept", occurredAt = cutoff))

        val deleted = systemEventRepository.deleteOlderThan(cutoff)

        assertEquals(1, deleted)
        val remaining = systemEventRepository.findAll()
        assertEquals(listOf(kept.id), remaining.map { it.id })
    }

    @Test
    fun `countNewerThan counts later events and ignores the row itself`() {
        authenticateAs("ada")
        val earlier = systemEventRepository.saveAndFlush(
            event(name = "earlier", occurredAt = Instant.parse("2026-10-26T12:00:00Z")),
        )
        val later = systemEventRepository.saveAndFlush(
            event(name = "later", occurredAt = Instant.parse("2026-10-26T13:00:00Z")),
        )

        assertEquals(1, systemEventRepository.countNewerThan(earlier.occurredAt, earlier.id!!))
        assertEquals(0, systemEventRepository.countNewerThan(later.occurredAt, later.id!!))
    }

    @Test
    fun `findAll orders by occurredAt descending`() {
        authenticateAs("ada")
        systemEventRepository.saveAndFlush(event(name = "older", occurredAt = Instant.parse("2026-10-25T00:00:00Z")))
        systemEventRepository.saveAndFlush(event(name = "newer", occurredAt = Instant.parse("2026-10-26T00:00:00Z")))

        val page = systemEventRepository.findAll(
            PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "occurredAt")),
        )

        assertEquals(listOf("newer", "older"), page.content.map { it.name })
    }

    @Test
    fun `domain specification returns only the matching domain`() {
        authenticateAs("ada")
        systemEventRepository.saveAndFlush(event(name = "model", domain = "models"))
        systemEventRepository.saveAndFlush(event(name = "file", domain = "files"))

        val matches = systemEventRepository.findAll(
            Specification { root, _, builder -> builder.equal(root.get<String>("domain"), "files") },
            PageRequest.of(0, 10),
        )

        assertEquals(listOf("file"), matches.content.map { it.name })
        assertTrue(matches.totalElements == 1L)
    }

    private fun event(
        name: String,
        domain: String = "models",
        occurredAt: Instant = Instant.parse("2026-10-26T12:00:00Z"),
    ) = SystemEventEntity(
        name = name,
        domain = domain,
        operatorName = "ada",
        comment = "note",
        occurredAt = occurredAt,
    )

    private fun authenticateAs(userId: String) {
        SecurityContextHolder.getContext().authentication = UsernamePasswordAuthenticationToken(
            userId,
            "n/a",
            AuthorityUtils.createAuthorityList("ROLE_USER"),
        )
    }
}
