package com.agentgo.file.repository

import com.agentgo.dto.workspace.WorkspaceMetadata
import com.agentgo.file.config.JpaAuditingConfiguration
import com.agentgo.file.entity.WorkspaceEntity
import jakarta.persistence.EntityManager
import java.util.UUID
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.persistence.autoconfigure.EntityScan
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.test.context.ActiveProfiles

@DataJpaTest
@ActiveProfiles("test")
@Import(JpaAuditingConfiguration::class)
@EntityScan(basePackageClasses = [WorkspaceEntity::class])
@EnableJpaRepositories(basePackageClasses = [WorkspaceRepository::class])
class WorkspaceRepositoryTest @Autowired constructor(
    private val workspaceRepository: WorkspaceRepository,
    private val entityManager: EntityManager,
) {
    @AfterTest
    fun clearSecurityContext() {
        SecurityContextHolder.clearContext()
    }

    @Test
    fun `save assigns id audit fields and persists workspace row`() {
        authenticateAs("ada")

        val saved = workspaceRepository.saveAndFlush(
            WorkspaceEntity(
                ownerUserId = "ada",
                name = "Reports",
                description = "Quarterly drafts",
            ),
        )

        assertNotNull(saved.id)
        assertEquals("ada", saved.creator)
        assertEquals("ada", saved.updatedBy)
        assertNotNull(saved.createdAt)
        assertNotNull(saved.updatedAt)
        assertEquals("Reports", workspaceRepository.findById(saved.id!!).orElseThrow().name)
    }

    @Test
    fun `findByIdAndOwnerUserId returns only matching owner row`() {
        authenticateAs("ada")
        val owned = workspaceRepository.saveAndFlush(
            WorkspaceEntity(ownerUserId = "ada", name = "Owned"),
        )
        workspaceRepository.saveAndFlush(
            WorkspaceEntity(ownerUserId = "grace", name = "Other"),
        )

        assertEquals(owned.id, workspaceRepository.findByIdAndOwnerUserId(owned.id!!, "ada")?.id)
        assertNull(workspaceRepository.findByIdAndOwnerUserId(owned.id!!, "grace"))
    }

    @Test
    fun `findByOwnerUserIdOrderByUpdatedAtDesc returns newest updates first`() {
        authenticateAs("ada")
        val older = workspaceRepository.saveAndFlush(
            WorkspaceEntity(ownerUserId = "ada", name = "Older"),
        )
        Thread.sleep(5)
        val newer = workspaceRepository.saveAndFlush(
            WorkspaceEntity(ownerUserId = "ada", name = "Newer"),
        )
        workspaceRepository.saveAndFlush(
            WorkspaceEntity(ownerUserId = "grace", name = "Foreign"),
        )

        val listed = workspaceRepository.findByOwnerUserIdOrderByUpdatedAtDesc("ada")

        assertEquals(listOf(newer.id, older.id), listed.map { it.id })
    }

    @Test
    fun `existsByOwnerUserIdAndName detects duplicate names per owner`() {
        authenticateAs("ada")
        workspaceRepository.saveAndFlush(
            WorkspaceEntity(ownerUserId = "ada", name = "Reports"),
        )
        workspaceRepository.saveAndFlush(
            WorkspaceEntity(ownerUserId = "grace", name = "Reports"),
        )

        assertTrue(workspaceRepository.existsByOwnerUserIdAndName("ada", "Reports"))
        assertFalse(workspaceRepository.existsByOwnerUserIdAndName("ada", "Missing"))
        assertTrue(workspaceRepository.existsByOwnerUserIdAndName("grace", "Reports"))
    }

    @Test
    fun `existsByOwnerUserIdAndNameAndIdNot ignores current workspace id`() {
        authenticateAs("ada")
        val current = workspaceRepository.saveAndFlush(
            WorkspaceEntity(ownerUserId = "ada", name = "Reports"),
        )
        val other = workspaceRepository.saveAndFlush(
            WorkspaceEntity(ownerUserId = "ada", name = "Archive"),
        )

        assertFalse(
            workspaceRepository.existsByOwnerUserIdAndNameAndIdNot("ada", "Reports", current.id!!),
        )
        assertTrue(
            workspaceRepository.existsByOwnerUserIdAndNameAndIdNot("ada", "Archive", current.id!!),
        )
        assertTrue(
            workspaceRepository.existsByOwnerUserIdAndNameAndIdNot("ada", "Reports", other.id!!),
        )
    }

    @Test
    fun `metadata color round trips through jsonb storage`() {
        authenticateAs("ada")
        val saved = workspaceRepository.saveAndFlush(
            WorkspaceEntity(
                ownerUserId = "ada",
                name = "Colored",
                metadata = WorkspaceMetadata().apply { color = "#B91C1C" },
            ),
        )
        val id = requireNotNull(saved.id)
        entityManager.clear()

        val reloaded = workspaceRepository.findById(id).orElseThrow()

        assertEquals("#B91C1C", reloaded.metadata.color)
    }

    @Test
    fun `delete removes persisted workspace row`() {
        authenticateAs("ada")
        val saved = workspaceRepository.saveAndFlush(
            WorkspaceEntity(ownerUserId = "ada", name = "Disposable"),
        )
        val id = requireNotNull(saved.id)

        workspaceRepository.delete(saved)
        workspaceRepository.flush()

        assertFalse(workspaceRepository.findById(id).isPresent)
    }

    private fun authenticateAs(userId: String) {
        SecurityContextHolder.getContext().authentication =
            UsernamePasswordAuthenticationToken(userId, "n/a", emptyList())
    }
}
