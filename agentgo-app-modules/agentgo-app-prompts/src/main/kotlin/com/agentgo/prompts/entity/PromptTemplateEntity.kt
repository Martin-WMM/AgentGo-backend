package com.agentgo.prompts.entity

import com.agentgo.dto.prompts.PromptEngine
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
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
 * Persistent representation of a user-owned prompt template.
 *
 * Ownership is enforced through [ownerUserId], which is always derived from the
 * authenticated security principal and never accepted from client request bodies.
 *
 * @property id persistent primary key
 * @property ownerUserId authenticated owner identifier
 * @property name display name unique per owner
 * @property engine rendering engine
 * @property content template body
 * @property description optional free-form description
 * @property creator audit actor that created the row
 * @property createdAt creation timestamp
 * @property updatedAt last modification timestamp
 * @property updatedBy audit actor that last modified the row
 */
@Entity
@Table(name = "tbl_prompt_template")
@EntityListeners(AuditingEntityListener::class)
class PromptTemplateEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,

    @Column(name = "owner_user_id", nullable = false, updatable = false, length = 255, comment = "Authenticated owner resolved from the security context")
    var ownerUserId: String = "",

    @Column(name = "name", nullable = false, length = 255, comment = "Human-readable template name unique per owner")
    var name: String = "",

    @Enumerated(EnumType.STRING)
    @Column(name = "engine", nullable = false, length = 32, comment = "Template engine used to render the content")
    var engine: PromptEngine = PromptEngine.SIMPLE,

    @Column(name = "content", nullable = false, columnDefinition = "text", comment = "Template body rendered by the selected engine")
    var content: String = "",

    @Column(name = "description", length = 2000, comment = "Optional free-form description")
    var description: String? = null,

    @CreatedBy
    @Column(name = "creator", nullable = false, updatable = false, length = 255, comment = "Identifier of the actor that created the prompt template")
    var creator: String? = null,

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false, comment = "Prompt template creation timestamp")
    var createdAt: Instant? = null,

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false, comment = "Prompt template last update timestamp")
    var updatedAt: Instant? = null,

    @LastModifiedBy
    @Column(name = "updated_by", nullable = false, length = 255, comment = "Identifier of the actor that last updated the prompt template")
    var updatedBy: String? = null,
)
