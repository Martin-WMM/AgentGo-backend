package com.agentgo.a2a.repository

import com.agentgo.a2a.config.A2aJpaAuditingConfiguration
import com.agentgo.a2a.entity.A2aAgentEntity
import com.agentgo.dto.a2a.A2aAgentStatus
import jakarta.persistence.EntityManager
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.boot.persistence.autoconfigure.EntityScan
import org.springframework.context.annotation.Import
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.test.context.ActiveProfiles

@DataJpaTest
@ActiveProfiles("test")
@Import(A2aJpaAuditingConfiguration::class)
@EntityScan(basePackageClasses = [A2aAgentEntity::class])
@EnableJpaRepositories(basePackageClasses = [A2aAgentRepository::class])
class A2aAgentRepositoryTest @Autowired constructor(
    private val a2aAgentRepository: A2aAgentRepository,
    private val entityManager: EntityManager,
) {
    @AfterTest
    fun clearSecurityContext() {
        SecurityContextHolder.clearContext()
    }

    @Test
    fun `save assigns id audit fields and persists the agent row`() {
        authenticateAs("ada")

        val saved = a2aAgentRepository.saveAndFlush(agent("ada", "billing-agent"))
        val id = requireNotNull(saved.id)
        entityManager.clear()
        val reloaded = a2aAgentRepository.findById(id).orElseThrow()

        assertEquals("ada", saved.creator)
        assertEquals("ada", saved.updatedBy)
        assertNotNull(saved.createdAt)
        assertNotNull(saved.updatedAt)
        assertEquals("billing-agent", reloaded.name)
        assertEquals(listOf("billing", "finance"), reloaded.tags)
        assertEquals("ops", reloaded.metadata["team"])
        assertEquals(listOf(0.25, 0.75), reloaded.descriptionVector)
        assertEquals(A2aAgentStatus.AVAILABLE, reloaded.status)
    }

    @Test
    fun `findByIdAndOwnerUserId returns only the matching owner row`() {
        authenticateAs("ada")
        val owned = a2aAgentRepository.saveAndFlush(agent("ada", "owned"))
        a2aAgentRepository.saveAndFlush(agent("grace", "other"))

        assertEquals(owned.id, a2aAgentRepository.findByIdAndOwnerUserId(owned.id!!, "ada")?.id)
        assertNull(a2aAgentRepository.findByIdAndOwnerUserId(owned.id!!, "grace"))
    }

    @Test
    fun `findByOwnerUserIdOrderByUpdatedAtDesc returns newest updates first`() {
        authenticateAs("ada")
        val older = a2aAgentRepository.saveAndFlush(agent("ada", "older"))
        Thread.sleep(30)
        val newer = a2aAgentRepository.saveAndFlush(agent("ada", "newer"))
        a2aAgentRepository.saveAndFlush(agent("grace", "foreign"))

        val listed = a2aAgentRepository.findByOwnerUserIdOrderByUpdatedAtDesc("ada")

        assertEquals(listOf(newer.id, older.id), listed.map { it.id })
    }

    @Test
    fun `findByOwnerUserIdAndStatusOrderByUpdatedAtDesc filters and orders by status`() {
        authenticateAs("ada")
        val older = a2aAgentRepository.saveAndFlush(
            agent("ada", "older-available", status = A2aAgentStatus.AVAILABLE),
        )
        Thread.sleep(30)
        val newer = a2aAgentRepository.saveAndFlush(
            agent("ada", "newer-available", status = A2aAgentStatus.AVAILABLE),
        )
        a2aAgentRepository.saveAndFlush(
            agent("ada", "offline", status = A2aAgentStatus.UNAVAILABLE),
        )
        a2aAgentRepository.saveAndFlush(
            agent("grace", "foreign-available", status = A2aAgentStatus.AVAILABLE),
        )

        val listed = a2aAgentRepository.findByOwnerUserIdAndStatusOrderByUpdatedAtDesc(
            "ada",
            A2aAgentStatus.AVAILABLE,
        )

        assertEquals(listOf(newer.id, older.id), listed.map { it.id })
    }

    @Test
    fun `existsByGroupNameAndNameAndCreator detects duplicates for the same creator`() {
        authenticateAs("ada")
        a2aAgentRepository.saveAndFlush(agent("ada", "billing-agent", groupName = "billing"))
        authenticateAs("grace")
        a2aAgentRepository.saveAndFlush(agent("grace", "billing-agent", groupName = "billing"))

        assertTrue(a2aAgentRepository.existsByGroupNameAndNameAndCreator("billing", "billing-agent", "ada"))
        assertFalse(a2aAgentRepository.existsByGroupNameAndNameAndCreator("support", "billing-agent", "ada"))
        assertFalse(a2aAgentRepository.existsByGroupNameAndNameAndCreator("billing", "missing", "ada"))
        assertTrue(a2aAgentRepository.existsByGroupNameAndNameAndCreator("billing", "billing-agent", "grace"))
    }

    @Test
    fun `existsByGroupNameAndNameAndCreatorAndIdNot ignores the current agent id`() {
        authenticateAs("ada")
        val current = a2aAgentRepository.saveAndFlush(agent("ada", "billing-agent"))
        val other = a2aAgentRepository.saveAndFlush(agent("ada", "archive-agent"))

        assertFalse(
            a2aAgentRepository.existsByGroupNameAndNameAndCreatorAndIdNot(
                "billing",
                "billing-agent",
                "ada",
                current.id!!,
            ),
        )
        assertTrue(
            a2aAgentRepository.existsByGroupNameAndNameAndCreatorAndIdNot(
                "billing",
                "archive-agent",
                "ada",
                current.id!!,
            ),
        )
        assertTrue(
            a2aAgentRepository.existsByGroupNameAndNameAndCreatorAndIdNot(
                "billing",
                "billing-agent",
                "ada",
                other.id!!,
            ),
        )
    }

    @Test
    fun `delete removes the persisted agent row`() {
        authenticateAs("ada")
        val saved = a2aAgentRepository.saveAndFlush(agent("ada", "disposable"))
        val id = requireNotNull(saved.id)

        a2aAgentRepository.delete(saved)
        a2aAgentRepository.flush()

        assertFalse(a2aAgentRepository.findById(id).isPresent)
    }

    private fun agent(
        ownerUserId: String,
        name: String,
        groupName: String = "billing",
        status: A2aAgentStatus = A2aAgentStatus.AVAILABLE,
    ): A2aAgentEntity =
        A2aAgentEntity(
            ownerUserId = ownerUserId,
            cardUrl = "https://agent.example.com/.well-known/agent-card.json",
            groupName = groupName,
            name = name,
            description = "Handles invoices",
            tags = mutableListOf("billing", "finance"),
            metadata = mutableMapOf("team" to "ops"),
            descriptionVector = mutableListOf(0.25, 0.75),
            enabled = true,
            status = status,
        )

    private fun authenticateAs(userId: String) {
        SecurityContextHolder.getContext().authentication =
            UsernamePasswordAuthenticationToken(userId, "n/a", emptyList())
    }
}
