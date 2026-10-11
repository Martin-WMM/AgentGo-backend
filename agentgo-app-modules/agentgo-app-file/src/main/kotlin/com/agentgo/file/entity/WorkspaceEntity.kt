package com.agentgo.file.entity

import com.agentgo.commons.validation.NullOrTrimmedSize
import com.agentgo.commons.validation.TrimmedNotBlank
import com.agentgo.commons.validation.TrimmedSize
import com.agentgo.dto.workspace.WorkspaceMetadata
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.validation.Valid
import java.time.Instant
import java.util.UUID
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener

/**
 * Persistent representation of a user-owned file workspace.
 *
 * Ownership is enforced through [ownerUserId], which is always derived from the
 * authenticated security principal and never accepted from client request bodies.
 *
 * Bean Validation annotations mirror the DTO contract so JPA pre-persist / pre-update
 * checks reject invalid rows even when callers bypass the HTTP layer.
 *
 * @property id persistent primary key
 * @property ownerUserId authenticated owner identifier
 * @property name display name unique per owner
 * @property description optional free-form description
 * @property metadata presentation metadata, including the accent color
 * @property creator audit actor that created the row
 * @property createdAt creation timestamp
 * @property updatedAt last modification timestamp
 * @property updatedBy audit actor that last modified the row
 */
@Entity
@Table(name = "tbl_workspace")
@EntityListeners(AuditingEntityListener::class)
class WorkspaceEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,

    @field:TrimmedNotBlank
    @field:TrimmedSize(min = 1, max = 255)
    @Column(name = "owner_user_id", nullable = false, updatable = false, length = 255, comment = "Authenticated owner resolved from the security context")
    var ownerUserId: String = "",

    @field:TrimmedNotBlank
    @field:TrimmedSize(min = 1, max = 255)
    @Column(name = "name", nullable = false, length = 255, comment = "Human-readable workspace name unique per owner")
    var name: String = "",

    @field:NullOrTrimmedSize(max = 2000)
    @Column(name = "description", length = 2000, comment = "Optional free-form description")
    var description: String? = null,

    @field:Valid
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", nullable = false, comment = "Presentation metadata such as the accent color")
    var metadata: WorkspaceMetadata = WorkspaceMetadata(),

    @CreatedBy
    @Column(name = "creator", nullable = false, updatable = false, length = 255, comment = "Identifier of the actor that created the workspace")
    var creator: String? = null,

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false, comment = "Workspace creation timestamp")
    var createdAt: Instant? = null,

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false, comment = "Workspace last update timestamp")
    var updatedAt: Instant? = null,

    @LastModifiedBy
    @Column(name = "updated_by", nullable = false, length = 255, comment = "Identifier of the actor that last updated the workspace")
    var updatedBy: String? = null,
)
