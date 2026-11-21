package com.agentgo.prompts.repository

import com.agentgo.dto.presentation.IconSource
import com.agentgo.dto.presentation.ResourceIcon
import com.agentgo.dto.prompts.PromptEngine
import com.agentgo.dto.prompts.PromptTemplateMetadata
import com.agentgo.prompts.config.PromptTemplateJpaAuditingConfiguration
import com.agentgo.prompts.entity.PromptTemplateEntity
import com.agentgo.prompts.entity.PromptTemplateRevisionEntity
import jakarta.persistence.EntityManager
import java.time.Instant
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
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.test.context.ActiveProfiles

@DataJpaTest
@ActiveProfiles("test")
@Import(PromptTemplateJpaAuditingConfiguration::class)
@EntityScan(basePackageClasses = [PromptTemplateEntity::class])
@EnableJpaRepositories(basePackageClasses = [PromptTemplateRepository::class])
class PromptTemplateRepositoryTest @Autowired constructor(
    private val promptTemplateRepository: PromptTemplateRepository,
    private val promptTemplateRevisionRepository: PromptTemplateRevisionRepository,
    private val entityManager: EntityManager,
) {
    @AfterTest
    fun clearSecurityContext() {
        SecurityContextHolder.clearContext()
    }

    @Test
    fun `save assigns id audit fields and persists the template row`() {
        authenticateAs("ada")

        val saved = promptTemplateRepository.saveAndFlush(template("ada", "Greeting"))

        assertNotNull(saved.id)
        assertEquals("ada", saved.creator)
        assertEquals("ada", saved.updatedBy)
        assertNotNull(saved.createdAt)
        assertNotNull(saved.updatedAt)
        assertEquals("Greeting", promptTemplateRepository.findById(saved.id!!).orElseThrow().name)
        entityManager.clear()
        val reloaded = promptTemplateRepository.findById(saved.id!!).orElseThrow()
        assertEquals(listOf("draft"), reloaded.tags)
        assertEquals("#1D4ED8", reloaded.metadata.color)
        assertEquals(IconSource.ICONIFY, reloaded.metadata.icon?.source)
        assertEquals("lucide:bot", reloaded.metadata.icon?.value)
    }

    @Test
    fun `findByIdAndOwnerUserId returns only the matching owner row`() {
        authenticateAs("ada")
        val owned = promptTemplateRepository.saveAndFlush(template("ada", "Owned"))
        promptTemplateRepository.saveAndFlush(template("grace", "Other"))

        assertEquals(owned.id, promptTemplateRepository.findByIdAndOwnerUserId(owned.id!!, "ada")?.id)
        assertNull(promptTemplateRepository.findByIdAndOwnerUserId(owned.id!!, "grace"))
    }

    @Test
    fun `findByOwnerUserId returns one owner page ordered by the pageable`() {
        authenticateAs("ada")
        val older = promptTemplateRepository.saveAndFlush(template("ada", "Older"))
        val newer = promptTemplateRepository.saveAndFlush(template("ada", "Newer"))
        val third = promptTemplateRepository.saveAndFlush(template("ada", "Third"))
        promptTemplateRepository.saveAndFlush(template("grace", "Foreign"))
        setUpdatedAt(older.id!!, Instant.parse("2026-01-01T00:00:00Z"))
        setUpdatedAt(newer.id!!, Instant.parse("2026-01-03T00:00:00Z"))
        setUpdatedAt(third.id!!, Instant.parse("2026-01-02T00:00:00Z"))
        entityManager.clear()

        val listed = promptTemplateRepository.findByOwnerUserId(
            "ada",
            PageRequest.of(0, 2, Sort.by(Sort.Direction.DESC, "updatedAt")),
        )

        assertEquals(3L, listed.totalElements)
        assertEquals(2, listed.totalPages)
        assertEquals(listOf(newer.id, third.id), listed.content.map { it.id })
    }

    @Test
    fun `existsByGroupNameAndNameAndCreator allows the same name in another group or for another creator`() {
        authenticateAs("ada")
        promptTemplateRepository.saveAndFlush(template("ada", "Greeting"))
        promptTemplateRepository.saveAndFlush(template("ada", "Greeting", groupName = "tools"))
        authenticateAs("grace")
        promptTemplateRepository.saveAndFlush(template("grace", "Greeting"))

        assertTrue(promptTemplateRepository.existsByGroupNameAndNameAndCreator("chat", "Greeting", "ada"))
        assertTrue(promptTemplateRepository.existsByGroupNameAndNameAndCreator("tools", "Greeting", "ada"))
        assertFalse(promptTemplateRepository.existsByGroupNameAndNameAndCreator("chat", "Missing", "ada"))
        assertTrue(promptTemplateRepository.existsByGroupNameAndNameAndCreator("chat", "Greeting", "grace"))
    }

    @Test
    fun `existsByGroupNameAndNameAndCreatorAndIdNot ignores the current template id`() {
        authenticateAs("ada")
        val current = promptTemplateRepository.saveAndFlush(template("ada", "Greeting"))
        val other = promptTemplateRepository.saveAndFlush(template("ada", "Archive"))

        assertFalse(
            promptTemplateRepository.existsByGroupNameAndNameAndCreatorAndIdNot(
                "chat",
                "Greeting",
                "ada",
                current.id!!,
            ),
        )
        assertTrue(
            promptTemplateRepository.existsByGroupNameAndNameAndCreatorAndIdNot(
                "chat",
                "Archive",
                "ada",
                current.id!!,
            ),
        )
        assertTrue(
            promptTemplateRepository.existsByGroupNameAndNameAndCreatorAndIdNot(
                "chat",
                "Greeting",
                "ada",
                other.id!!,
            ),
        )
    }

    @Test
    fun `revisions round trip and deleteByTemplateId removes them`() {
        authenticateAs("ada")
        val saved = promptTemplateRepository.saveAndFlush(template("ada", "Revisioned"))
        promptTemplateRevisionRepository.saveAndFlush(
            PromptTemplateRevisionEntity(
                templateId = saved.id,
                revisionNumber = 1,
                message = "Initial commit",
                author = "ada",
                diff = "+Hello",
                groupName = "chat",
                name = "Revisioned",
                content = "Hello {{ name }}",
                metadata = saved.metadata.duplicate(),
                tags = mutableListOf("draft"),
            ),
        )
        entityManager.clear()

        val loaded = promptTemplateRevisionRepository.findByTemplateIdAndRevisionNumber(saved.id!!, 1)
        assertEquals("Initial commit", loaded?.message)
        assertEquals("ada", loaded?.author)

        promptTemplateRevisionRepository.deleteByTemplateId(saved.id!!)
        promptTemplateRevisionRepository.flush()
        entityManager.clear()
        assertTrue(
            promptTemplateRevisionRepository.findByTemplateId(saved.id!!, PageRequest.of(0, 10)).isEmpty,
        )
    }

    @Test
    fun `delete removes the persisted template row`() {
        authenticateAs("ada")
        val saved = promptTemplateRepository.saveAndFlush(template("ada", "Disposable"))
        val id = requireNotNull(saved.id)

        promptTemplateRepository.delete(saved)
        promptTemplateRepository.flush()

        assertFalse(promptTemplateRepository.findById(id).isPresent)
    }

    private fun setUpdatedAt(id: Long, updatedAt: Instant) {
        entityManager.createQuery(
            "update PromptTemplateEntity template set template.updatedAt = :updatedAt where template.id = :id",
        )
            .setParameter("updatedAt", updatedAt)
            .setParameter("id", id)
            .executeUpdate()
        entityManager.flush()
    }

    private fun template(ownerUserId: String, name: String, groupName: String = "chat") = PromptTemplateEntity(
        ownerUserId = ownerUserId,
        groupName = groupName,
        name = name,
        engine = PromptEngine.SIMPLE,
        content = "Hello {{ name }}",
        description = "sample",
        tags = mutableListOf("draft"),
        metadata = PromptTemplateMetadata().apply {
            color = "#1D4ED8"
            icon = ResourceIcon().apply {
                source = IconSource.ICONIFY
                value = "lucide:bot"
            }
        },
    )

    private fun authenticateAs(userId: String) {
        SecurityContextHolder.getContext().authentication =
            UsernamePasswordAuthenticationToken(userId, "n/a", emptyList())
    }
}
