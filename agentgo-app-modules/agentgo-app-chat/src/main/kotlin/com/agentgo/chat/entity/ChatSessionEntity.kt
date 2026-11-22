package com.agentgo.chat.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.time.Instant
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener

/**
 * Persistent representation of a user-owned chat session.
 *
 * @property id persistent primary key
 * @property ownerUserId authenticated owner identifier
 * @property title display title shown in the session list
 * @property threadId LangGraph thread id used by the agent stream
 * @property createdAt creation timestamp
 * @property updatedAt last modification timestamp
 */
@Entity
@Table(
    name = "tbl_chat_session",
    uniqueConstraints = [
        UniqueConstraint(name = "uq_tbl_chat_session_thread_id", columnNames = ["thread_id"]),
    ],
)
@EntityListeners(AuditingEntityListener::class)
class ChatSessionEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(name = "owner_user_id", nullable = false, updatable = false, length = 255)
    var ownerUserId: String = "",

    @Column(name = "title", nullable = false, length = 255)
    var title: String = "",

    @Column(name = "thread_id", nullable = false, updatable = false, length = 200)
    var threadId: String = "",

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: Instant? = null,

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant? = null,
)
