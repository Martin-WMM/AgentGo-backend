package com.agentgo.models.entity

import com.agentgo.dto.models.ModelType
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
 * Persistent representation of a user-owned AI model configuration.
 *
 * Ownership is enforced through [ownerUserId], which is always derived from the
 * authenticated security principal and never accepted from client request bodies.
 *
 * @property id persistent primary key
 * @property ownerUserId authenticated owner identifier
 * @property name display name unique per owner
 * @property provider upstream provider identifier
 * @property type model capability category
 * @property configuration provider-specific JSON settings
 * @property description optional free-form description
 * @property creator audit actor that created the row
 * @property createdAt creation timestamp
 * @property updatedAt last modification timestamp
 * @property updatedBy audit actor that last modified the row
 */
@Entity
@Table(
    name = "tbl_model",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uq_tbl_model_group_name_name_creator",
            columnNames = ["group_name", "name", "creator"],
        ),
    ],
)
@EntityListeners(AuditingEntityListener::class)
class ModelEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(name = "owner_user_id", nullable = false, updatable = false, length = 255, comment = "Authenticated owner resolved from the security context")
    var ownerUserId: String = "",

    @Column(name = "group_name", nullable = false, length = 255, comment = "Group name unique together with name and creator")
    var groupName: String = "default",

    @Column(name = "name", nullable = false, length = 255, comment = "Human-readable model name unique with group name and creator")
    var name: String = "",

    @Column(name = "provider", nullable = false, length = 255, comment = "Upstream provider identifier")
    var provider: String = "",

    @Enumerated(EnumType.STRING)
    @Column(name = "model_type", nullable = false, length = 32, comment = "Model capability category")
    var type: ModelType = ModelType.OTHER,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "configuration", nullable = false, comment = "Provider-specific JSON configuration")
    var configuration: Map<String, Any?> = emptyMap(),

    @Column(name = "description", length = 2000, comment = "Optional free-form description")
    var description: String? = null,

    @CreatedBy
    @Column(name = "creator", nullable = false, updatable = false, length = 255, comment = "Identifier of the actor that created the model")
    var creator: String? = null,

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false, comment = "Model creation timestamp")
    var createdAt: Instant? = null,

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false, comment = "Model last update timestamp")
    var updatedAt: Instant? = null,

    @LastModifiedBy
    @Column(name = "updated_by", nullable = false, length = 255, comment = "Identifier of the actor that last updated the model")
    var updatedBy: String? = null,
)
