package com.agentgo.settings.repository

import com.agentgo.dto.settings.UiDensity
import com.agentgo.dto.settings.UiLocale
import com.agentgo.dto.settings.UiTheme
import com.agentgo.settings.entity.UserSettingsEntity
import com.agentgo.settings.support.SettingsRepositoryAuditingConfiguration
import jakarta.persistence.EntityManager
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.boot.persistence.autoconfigure.EntityScan
import org.springframework.context.annotation.Import
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.test.context.ActiveProfiles

@DataJpaTest
@ActiveProfiles("test")
@Import(SettingsRepositoryAuditingConfiguration::class)
@EntityScan(basePackageClasses = [UserSettingsEntity::class])
@EnableJpaRepositories(basePackageClasses = [UserSettingsRepository::class])
class UserSettingsRepositoryTest @Autowired constructor(
    private val userSettingsRepository: UserSettingsRepository,
    private val entityManager: EntityManager,
) {
    @AfterTest
    fun clearSecurityContext() {
        SecurityContextHolder.clearContext()
    }

    @Test
    fun `save assigns id audit fields and persists the settings row`() {
        authenticateAs("subject-ada")

        val saved = userSettingsRepository.saveAndFlush(settings("subject-ada"))

        assertNotNull(saved.id)
        assertEquals("subject-ada", saved.creator)
        assertEquals("subject-ada", saved.updatedBy)
        assertNotNull(saved.createdAt)
        assertNotNull(saved.updatedAt)
        assertEquals(UiTheme.DARK, userSettingsRepository.findById(saved.id!!).orElseThrow().theme)
    }

    @Test
    fun `findByOwnerUserId returns only the matching subject`() {
        authenticateAs("subject-ada")
        userSettingsRepository.saveAndFlush(settings("subject-ada"))
        userSettingsRepository.saveAndFlush(settings("subject-grace", theme = UiTheme.LIGHT))

        assertEquals(UiTheme.DARK, userSettingsRepository.findByOwnerUserId("subject-ada")?.theme)
        assertEquals(UiTheme.LIGHT, userSettingsRepository.findByOwnerUserId("subject-grace")?.theme)
        assertNull(userSettingsRepository.findByOwnerUserId("subject-missing"))
    }

    @Test
    fun `updating preferences keeps the same owner row`() {
        authenticateAs("subject-ada")
        val saved = userSettingsRepository.saveAndFlush(settings("subject-ada"))
        entityManager.clear()

        val reloaded = userSettingsRepository.findByOwnerUserId("subject-ada")!!
        reloaded.locale = UiLocale.ZH_CN
        reloaded.sidebarCollapsed = true
        reloaded.density = UiDensity.COMPACT
        reloaded.reduceMotion = true
        userSettingsRepository.saveAndFlush(reloaded)

        val updated = userSettingsRepository.findById(saved.id!!).orElseThrow()
        assertEquals(UiLocale.ZH_CN, updated.locale)
        assertTrue(updated.sidebarCollapsed)
        assertEquals(UiDensity.COMPACT, updated.density)
        assertTrue(updated.reduceMotion)
        assertEquals("subject-ada", updated.ownerUserId)
    }

    @Test
    fun `owner user id is unique`() {
        authenticateAs("subject-ada")
        userSettingsRepository.saveAndFlush(settings("subject-ada"))

        assertFailsWith<DataIntegrityViolationException> {
            userSettingsRepository.saveAndFlush(settings("subject-ada", theme = UiTheme.SYSTEM))
        }
    }

    private fun settings(ownerUserId: String, theme: UiTheme = UiTheme.DARK) = UserSettingsEntity(
        ownerUserId = ownerUserId,
        theme = theme,
        locale = UiLocale.EN,
        sidebarCollapsed = false,
        density = UiDensity.COMFORTABLE,
        reduceMotion = false,
    )

    private fun authenticateAs(userId: String) {
        SecurityContextHolder.getContext().authentication = UsernamePasswordAuthenticationToken(
            userId,
            "n/a",
            AuthorityUtils.createAuthorityList("ROLE_USER"),
        )
    }
}
