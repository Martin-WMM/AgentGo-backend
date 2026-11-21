package com.agentgo.prompts.entity

import com.agentgo.dto.prompts.PromptEngine
import com.agentgo.dto.prompts.PromptTemplateMetadata
import com.agentgo.dto.prompts.PromptTemplateVersions
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
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
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
 * @property groupName group that scopes [name] for the creator
 * @property name display name unique with [groupName] and [creator]
 * @property version live version label, always `latest`
 * @property headRevision revision number that `latest` points at
 * @property engine rendering engine
 * @property content template body
 * @property description optional free-form description
 * @property tags display tags
 * @property metadata presentation color and icon
 * @property creator audit actor that created the row
 * @property createdAt creation timestamp
 * @property updatedAt last modification timestamp
 * @property updatedBy audit actor that last modified the row
 */
@Entity
@Table(
    name = "tbl_prompt_template",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uq_tbl_prompt_template_group_name_name_creator",
            columnNames = ["group_name", "name", "creator"],
        ),
    ],
)
@EntityListeners(AuditingEntityListener::class)
class PromptTemplateEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(name = "owner_user_id", nullable = false, updatable = false, length = 255, comment = "Authenticated owner resolved from the security context")
    var ownerUserId: String = "",

    @Column(name = "group_name", nullable = false, length = 255, comment = "Group name unique together with name and creator")
    var groupName: String = "",

    @Column(name = "name", nullable = false, length = 255, comment = "Human-readable template name unique with group name and creator")
    var name: String = "",

    @Column(name = "version", nullable = false, length = 32, comment = "Live version label. Always latest")
    var version: String = PromptTemplateVersions.LATEST,

    @Column(name = "head_revision", nullable = false, comment = "Revision number that the latest version points at")
    var headRevision: Int = 1,

    @Enumerated(EnumType.STRING)
    @Column(name = "engine", nullable = false, length = 32, comment = "Template engine used to render the content")
    var engine: PromptEngine = PromptEngine.SIMPLE,

    @Column(name = "content", nullable = false, columnDefinition = "text", comment = "Template body rendered by the selected engine")
    var content: String = "",

    @Column(name = "description", length = 2000, comment = "Optional free-form description")
    var description: String? = null,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "tags", nullable = false, comment = "Display tags stored as a JSON array")
    var tags: MutableList<String> = mutableListOf(),

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", nullable = false, comment = "Presentation metadata such as the accent color and icon")
    var metadata: PromptTemplateMetadata = PromptTemplateMetadata(),

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
