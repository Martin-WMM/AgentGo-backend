package com.agentgo.auth.repository

import com.agentgo.auth.entity.UserProfileEntity
import com.agentgo.file.config.JpaAuditingConfiguration
import jakarta.persistence.EntityManager
import java.util.UUID
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
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
@Import(JpaAuditingConfiguration::class)
@EntityScan(basePackageClasses = [UserProfileEntity::class])
@EnableJpaRepositories(basePackageClasses = [UserProfileRepository::class])
class UserProfileRepositoryTest @Autowired constructor(
    private val userProfileRepository: UserProfileRepository,
    private val entityManager: EntityManager,
) {
    @AfterTest
    fun clearSecurityContext() {
        SecurityContextHolder.clearContext()
    }

    @Test
    fun `save assigns id audit fields and persists profile row`() {
        authenticateAs("subject-ada")

        val saved = userProfileRepository.saveAndFlush(profile("subject-ada", "ada"))

        assertNotNull(saved.id)
        assertEquals("subject-ada", saved.creator)
        assertEquals("subject-ada", saved.updatedBy)
        assertNotNull(saved.createdAt)
        assertNotNull(saved.updatedAt)
        assertEquals("Ada Lovelace", userProfileRepository.findById(saved.id!!).orElseThrow().displayName)
    }

    @Test
    fun `findByOwnerUserId returns only the matching subject`() {
        authenticateAs("subject-ada")
        userProfileRepository.saveAndFlush(profile("subject-ada", "ada"))
        userProfileRepository.saveAndFlush(profile("subject-grace", "grace"))

        assertEquals("ada", userProfileRepository.findByOwnerUserId("subject-ada")?.username)
        assertNull(userProfileRepository.findByOwnerUserId("subject-missing"))
    }

    @Test
    fun `findByUsername and findByAuthentikUserUuid return the mirrored identity`() {
        authenticateAs("subject-ada")
        val saved = userProfileRepository.saveAndFlush(profile("subject-ada", "ada"))

        assertEquals(saved.id, userProfileRepository.findByUsername("ada")?.id)
        assertEquals(saved.id, userProfileRepository.findByAuthentikUserUuid(saved.authentikUserUuid!!)?.id)
        assertNull(userProfileRepository.findByUsername("missing"))
    }

    @Test
    fun `extended personal details survive an identity refresh`() {
        authenticateAs("subject-ada")
        val saved = userProfileRepository.saveAndFlush(
            profile("subject-ada", "ada").apply {
                phoneNumber = "+86 138 0013 8000"
                occupation = "Mathematician"
                homepageUrl = "https://example.com/ada"
            },
        )
        entityManager.clear()

        val reloaded = userProfileRepository.findByOwnerUserId("subject-ada")!!
        reloaded.displayName = "Ada"
        userProfileRepository.saveAndFlush(reloaded)

        val updated = userProfileRepository.findById(saved.id!!).orElseThrow()
        assertEquals("Ada", updated.displayName)
        assertEquals("+86 138 0013 8000", updated.phoneNumber)
        assertEquals("Mathematician", updated.occupation)
        assertEquals("https://example.com/ada", updated.homepageUrl)
    }

    private fun profile(subject: String, username: String) = UserProfileEntity(
        ownerUserId = subject,
        authentikUserUuid = UUID.nameUUIDFromBytes(subject.toByteArray()),
        username = username,
        displayName = "Ada Lovelace",
        email = "$username@example.com",
        description = "Personal notes",
        organization = "Analytical Engines",
        location = "London",
        timezone = "Europe/London",
        preferredLanguage = "en",
    )

    private fun authenticateAs(userId: String) {
        SecurityContextHolder.getContext().authentication =
            UsernamePasswordAuthenticationToken(userId, "n/a", emptyList())
    }
}
