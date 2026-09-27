package com.agentgo.file.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.jpa.domain.support.AuditingEntityListener

@Entity
@Table(name = "tbl_file")
@EntityListeners(AuditingEntityListener::class)
class FileEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,

    @Column(name = "bucket_name", nullable = false, length = 255, comment = "MinIO bucket name")
    var bucketName: String = "",

    @Column(name = "object_key", nullable = false, length = 1024, comment = "MinIO object key")
    var objectKey: String = "",

    @Column(name = "content_type", nullable = false, length = 255, comment = "MinIO object content type")
    var contentType: String = "application/octet-stream",

    @Column(name = "size_bytes", nullable = false, comment = "MinIO object size in bytes")
    var sizeBytes: Long = 0,

    @Column(name = "etag", length = 255, comment = "MinIO object entity tag")
    var etag: String? = null,

    @Column(name = "version_id", length = 255, comment = "MinIO object version identifier")
    var versionId: String? = null,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", nullable = false, columnDefinition = "jsonb", comment = "MinIO user metadata")
    var metadata: Map<String, String> = emptyMap(),

    @CreatedBy
    @Column(name = "creator", nullable = false, updatable = false, length = 255, comment = "Identifier of the actor that created the metadata")
    var creator: String? = null,

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false, comment = "Metadata creation timestamp")
    var createdAt: Instant? = null,

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false, comment = "Metadata last update timestamp")
    var updatedAt: Instant? = null,

    @LastModifiedBy
    @Column(name = "updated_by", nullable = false, length = 255, comment = "Identifier of the actor that last updated the metadata")
    var updatedBy: String? = null,
)
