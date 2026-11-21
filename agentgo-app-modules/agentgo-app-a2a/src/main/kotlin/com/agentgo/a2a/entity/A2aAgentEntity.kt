package com.agentgo.a2a.entity

import com.agentgo.dto.a2a.A2aAgentStatus
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
 * Persistent representation of a user-owned A2A sub-agent.
 *
 * Ownership is enforced through [ownerUserId], which is always derived from the
 * authenticated security principal and never accepted from client request bodies.
 */
@Entity
@Table(
    name = "tbl_a2a_agent",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uq_tbl_a2a_agent_group_name_name_creator",
            columnNames = ["group_name", "name", "creator"],
        ),
    ],
)
@EntityListeners(AuditingEntityListener::class)
class A2aAgentEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(name = "owner_user_id", nullable = false, updatable = false, length = 255)
    var ownerUserId: String = "",

    @Column(name = "card_url", nullable = false, length = 2048)
    var cardUrl: String = "",

    @Column(name = "group_name", nullable = false, length = 255)
    var groupName: String = "",

    @Column(name = "name", nullable = false, length = 255)
    var name: String = "",

    @Column(name = "description", length = 4000)
    var description: String? = null,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "tags", nullable = false)
    var tags: MutableList<String> = mutableListOf(),

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", nullable = false)
    var metadata: MutableMap<String, Any?> = mutableMapOf(),

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "description_vector", nullable = false)
    var descriptionVector: MutableList<Double> = mutableListOf(),

    @Column(name = "enabled", nullable = false)
    var enabled: Boolean = true,

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    var status: A2aAgentStatus = A2aAgentStatus.UNKNOWN,

    @CreatedBy
    @Column(name = "creator", nullable = false, updatable = false, length = 255)
    var creator: String? = null,

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: Instant? = null,

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant? = null,

    @LastModifiedBy
    @Column(name = "updated_by", nullable = false, length = 255)
    var updatedBy: String? = null,
)
