package com.agentgo.prompts.entity

import com.agentgo.dto.prompts.PromptEngine
import com.agentgo.dto.prompts.PromptTemplateMetadata
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
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener

/**
 * Immutable commit for one prompt template, stored like a git revision.
 *
 * [revisionNumber] starts at 1. [parentRevisionNumber] is null on the first commit.
 * [diff] is a unified diff of the metadata file and the content file. The remaining
 * fields are the full tree after the commit so a revision can be read without replay.
 *
 * @property id persistent primary key
 * @property templateId owning template identifier
 * @property revisionNumber one-based revision number
 * @property parentRevisionNumber previous revision, or null for the first commit
 * @property message commit comment
 * @property author authenticated principal that recorded the commit
 * @property diff unified diff against the parent snapshot
 * @property groupName group name after this commit
 * @property name template name after this commit
 * @property engine engine after this commit
 * @property content template body after this commit
 * @property description description after this commit
 * @property tags tags after this commit
 * @property metadata presentation metadata after this commit
 * @property createdAt commit timestamp
 */
@Entity
@Table(
    name = "tbl_prompt_template_revision",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uq_tbl_prompt_template_revision_template_revision",
            columnNames = ["template_id", "revision_number"],
        ),
    ],
)
@EntityListeners(AuditingEntityListener::class)
class PromptTemplateRevisionEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(name = "template_id", nullable = false, updatable = false, comment = "Owning prompt template identifier")
    var templateId: Long? = null,

    @Column(name = "revision_number", nullable = false, updatable = false, comment = "One-based revision number")
    var revisionNumber: Int = 1,

    @Column(name = "parent_revision_number", updatable = false, comment = "Previous revision number, null on the first commit")
    var parentRevisionNumber: Int? = null,

    @Column(name = "message", nullable = false, updatable = false, length = 2000, comment = "Commit comment")
    var message: String = "",

    @Column(name = "author", nullable = false, updatable = false, length = 255, comment = "Authenticated principal that recorded the commit")
    var author: String = "",

    @Column(name = "diff", nullable = false, updatable = false, columnDefinition = "text", comment = "Unified diff of metadata and content")
    var diff: String = "",

    @Column(name = "group_name", nullable = false, updatable = false, length = 255, comment = "Group name after this commit")
    var groupName: String = "",

    @Column(name = "name", nullable = false, updatable = false, length = 255, comment = "Template name after this commit")
    var name: String = "",

    @Enumerated(EnumType.STRING)
    @Column(name = "engine", nullable = false, updatable = false, length = 32, comment = "Template engine after this commit")
    var engine: PromptEngine = PromptEngine.SIMPLE,

    @Column(name = "content", nullable = false, updatable = false, columnDefinition = "text", comment = "Template body after this commit")
    var content: String = "",

    @Column(name = "description", updatable = false, length = 2000, comment = "Description after this commit")
    var description: String? = null,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "tags", nullable = false, updatable = false, comment = "Tags after this commit")
    var tags: MutableList<String> = mutableListOf(),

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", nullable = false, updatable = false, comment = "Presentation metadata after this commit")
    var metadata: PromptTemplateMetadata = PromptTemplateMetadata(),

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false, comment = "Commit timestamp")
    var createdAt: Instant? = null,
)
