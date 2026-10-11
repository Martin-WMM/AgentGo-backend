package com.agentgo.auth.entity

import com.agentgo.commons.validation.NullOrTrimmedSize
import com.agentgo.commons.validation.TrimmedNotBlank
import com.agentgo.commons.validation.TrimmedSize
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener

/**
 * Local profile for one authenticated user.
 *
 * [ownerUserId] is the OIDC subject. Identity columns are refreshed from Authentik.
 * Extended columns are owned by AgentGo so later agent runs can read personal context
 * without calling the identity provider.
 *
 * @property id persistent primary key
 * @property ownerUserId OIDC subject
 * @property authentikUserUuid Authentik core user UUID
 * @property username username mirrored from Authentik
 * @property displayName display name mirrored from Authentik
 * @property email email mirrored from Authentik
 * @property phoneNumber optional phone number
 * @property avatarUrl avatar URL mirrored from Authentik
 * @property description free-form personal description
 * @property occupation occupation or job title
 * @property organization organization or employer
 * @property homepageUrl personal or organization homepage
 * @property location free-form location
 * @property timezone IANA time zone id
 * @property preferredLanguage BCP 47 language tag
 * @property creator audit actor that created the row
 * @property createdAt creation timestamp
 * @property updatedAt last modification timestamp
 * @property updatedBy audit actor that last modified the row
 */
@Entity
@Table(name = "tbl_user_profile")
@EntityListeners(AuditingEntityListener::class)
class UserProfileEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,

    @field:TrimmedNotBlank
    @field:TrimmedSize(min = 1, max = 255)
    @Column(name = "owner_user_id", nullable = false, updatable = false, unique = true, length = 255, comment = "OIDC subject of the authenticated user")
    var ownerUserId: String = "",

    @Column(name = "authentik_user_uuid", unique = true, comment = "Authentik core user UUID")
    var authentikUserUuid: UUID? = null,

    @field:TrimmedNotBlank
    @field:TrimmedSize(min = 1, max = 150)
    @Column(name = "username", nullable = false, unique = true, length = 150, comment = "Username mirrored from Authentik")
    var username: String = "",

    @field:TrimmedNotBlank
    @field:TrimmedSize(min = 1, max = 150)
    @Column(name = "display_name", nullable = false, length = 150, comment = "Display name mirrored from Authentik")
    var displayName: String = "",

    @field:NullOrTrimmedSize(max = 254)
    @Column(name = "email", length = 254, comment = "Email address mirrored from Authentik")
    var email: String? = null,

    @field:NullOrTrimmedSize(max = 32)
    @Column(name = "phone_number", length = 32, comment = "Optional phone number maintained in AgentGo")
    var phoneNumber: String? = null,

    @field:NullOrTrimmedSize(max = 2048)
    @Column(name = "avatar_url", length = 2048, comment = "Avatar URL mirrored from Authentik")
    var avatarUrl: String? = null,

    @field:NullOrTrimmedSize(max = 2000)
    @Column(name = "description", length = 2000, comment = "Free-form personal description for agent context")
    var description: String? = null,

    @field:NullOrTrimmedSize(max = 128)
    @Column(name = "occupation", length = 128, comment = "Occupation or job title for agent context")
    var occupation: String? = null,

    @field:NullOrTrimmedSize(max = 255)
    @Column(name = "organization", length = 255, comment = "Organization or employer for agent context")
    var organization: String? = null,

    @field:NullOrTrimmedSize(max = 2048)
    @Column(name = "homepage_url", length = 2048, comment = "Personal or organization homepage URL")
    var homepageUrl: String? = null,

    @field:NullOrTrimmedSize(max = 255)
    @Column(name = "location", length = 255, comment = "Free-form location for agent context")
    var location: String? = null,

    @field:NullOrTrimmedSize(max = 64)
    @Column(name = "timezone", length = 64, comment = "IANA time zone id such as Asia/Shanghai")
    var timezone: String? = null,

    @field:NullOrTrimmedSize(max = 35)
    @Column(name = "preferred_language", length = 35, comment = "Preferred BCP 47 language tag such as zh-CN")
    var preferredLanguage: String? = null,

    @CreatedBy
    @Column(name = "creator", nullable = false, updatable = false, length = 255, comment = "Identifier of the actor that created the profile")
    var creator: String? = null,

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false, comment = "Profile creation timestamp")
    var createdAt: Instant? = null,

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false, comment = "Profile last update timestamp")
    var updatedAt: Instant? = null,

    @LastModifiedBy
    @Column(name = "updated_by", nullable = false, length = 255, comment = "Identifier of the actor that last updated the profile")
    var updatedBy: String? = null,
)
