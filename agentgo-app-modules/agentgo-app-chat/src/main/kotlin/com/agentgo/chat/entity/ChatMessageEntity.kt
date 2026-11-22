package com.agentgo.chat.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener

/**
 * Persistent representation of one chat message belonging to a session.
 *
 * @property id persistent primary key
 * @property sessionId owning session identifier
 * @property role message role: user, assistant, or system
 * @property content message text shown to the user
 * @property plan optional plan text stored with an assistant turn
 * @property createdAt creation timestamp
 */
@Entity
@Table(name = "tbl_chat_message")
@EntityListeners(AuditingEntityListener::class)
class ChatMessageEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(name = "session_id", nullable = false, updatable = false)
    var sessionId: Long = 0L,

    @Column(name = "role", nullable = false, updatable = false, length = 32)
    var role: String = "",

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    var content: String = "",

    @Column(name = "plan", columnDefinition = "TEXT")
    var plan: String? = null,

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: Instant? = null,
)
