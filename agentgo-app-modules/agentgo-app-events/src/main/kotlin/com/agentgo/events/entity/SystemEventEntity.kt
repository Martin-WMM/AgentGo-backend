package com.agentgo.events.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import java.time.Instant
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener

/**
 * Persistent representation of one global system event.
 *
 * Rows are not owned by a single user. [operatorName] records the person related
 * to the event. Retention deletes rows older than three months and keeps at most
 * the newest 1,000 remaining rows.
 *
 * @property id persistent primary key
 * @property name human-readable event name
 * @property domain business domain that produced the event
 * @property operatorName person related to the event
 * @property comment optional free-form comment
 * @property occurredAt instant when the event happened
 * @property creator audit actor that created the row
 * @property createdAt creation timestamp
 * @property updatedAt last modification timestamp
 * @property updatedBy audit actor that last modified the row
 */
@Entity
@Table(
    name = "tbl_system_event",
    indexes = [
        Index(name = "idx_tbl_system_event_occurred_at_id", columnList = "occurred_at, id"),
        Index(name = "idx_tbl_system_event_domain", columnList = "domain"),
    ],
)
@EntityListeners(AuditingEntityListener::class)
class SystemEventEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(name = "name", nullable = false, length = 255, comment = "Human-readable event name")
    var name: String = "",

    @Column(name = "domain", nullable = false, length = 255, comment = "Business domain that produced the event")
    var domain: String = "",

    @Column(name = "operator_name", nullable = false, length = 255, comment = "Person related to the event")
    var operatorName: String = "",

    @Column(name = "comment", columnDefinition = "text", comment = "Optional free-form comment")
    var comment: String? = null,

    @Column(name = "occurred_at", nullable = false, comment = "Instant when the event happened")
    var occurredAt: Instant = Instant.EPOCH,

    @CreatedBy
    @Column(name = "creator", nullable = false, updatable = false, length = 255, comment = "Identifier of the actor that created the row")
    var creator: String? = null,

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false, comment = "Row creation timestamp")
    var createdAt: Instant? = null,

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false, comment = "Row last update timestamp")
    var updatedAt: Instant? = null,

    @LastModifiedBy
    @Column(name = "updated_by", nullable = false, length = 255, comment = "Identifier of the actor that last updated the row")
    var updatedBy: String? = null,
)
