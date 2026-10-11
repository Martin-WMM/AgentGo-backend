package com.agentgo.settings.entity

import com.agentgo.dto.settings.UiDensity
import com.agentgo.dto.settings.UiLocale
import com.agentgo.dto.settings.UiTheme
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.time.Instant
import java.util.UUID
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener

/**
 * Persistent system settings for one authenticated user.
 *
 * Ownership is enforced through [ownerUserId], which is always derived from the
 * authenticated security principal and never accepted from client request bodies.
 *
 * @property id persistent primary key
 * @property ownerUserId authenticated owner identifier
 * @property theme color theme
 * @property locale interface language
 * @property sidebarCollapsed whether the workspace sidebar is collapsed
 * @property density information density
 * @property reduceMotion whether animations and transitions are minimized
 * @property creator audit actor that created the row
 * @property createdAt creation timestamp
 * @property updatedAt last modification timestamp
 * @property updatedBy audit actor that last modified the row
 */
@Entity
@Table(
    name = "tbl_user_settings",
    uniqueConstraints = [
        UniqueConstraint(name = "uq_tbl_user_settings_owner", columnNames = ["owner_user_id"]),
    ],
)
@EntityListeners(AuditingEntityListener::class)
class UserSettingsEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,

    @Column(name = "owner_user_id", nullable = false, updatable = false, length = 255, comment = "OIDC subject of the authenticated user")
    var ownerUserId: String = "",

    @Enumerated(EnumType.STRING)
    @Column(name = "theme", nullable = false, length = 16, comment = "Color theme: LIGHT, DARK, or SYSTEM")
    var theme: UiTheme = UiTheme.SYSTEM,

    @Enumerated(EnumType.STRING)
    @Column(name = "locale", nullable = false, length = 16, comment = "Interface language: EN or ZH_CN")
    var locale: UiLocale = UiLocale.EN,

    @Column(name = "sidebar_collapsed", nullable = false, comment = "Whether the workspace sidebar is collapsed")
    var sidebarCollapsed: Boolean = false,

    @Enumerated(EnumType.STRING)
    @Column(name = "density", nullable = false, length = 16, comment = "Information density: COMFORTABLE or COMPACT")
    var density: UiDensity = UiDensity.COMFORTABLE,

    @Column(name = "reduce_motion", nullable = false, comment = "Whether animations and transitions are minimized")
    var reduceMotion: Boolean = false,

    @CreatedBy
    @Column(name = "creator", nullable = false, updatable = false, length = 255, comment = "Identifier of the actor that created the settings")
    var creator: String? = null,

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false, comment = "Settings creation timestamp")
    var createdAt: Instant? = null,

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false, comment = "Settings last update timestamp")
    var updatedAt: Instant? = null,

    @LastModifiedBy
    @Column(name = "updated_by", nullable = false, length = 255, comment = "Identifier of the actor that last updated the settings")
    var updatedBy: String? = null,
)
