package com.agentgo.file.repository

import com.agentgo.file.entity.FileEntity
import java.util.UUID
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.repository.query.QueryByExampleExecutor

interface FileRepository :
    JpaRepository<FileEntity, UUID>,
    JpaSpecificationExecutor<FileEntity>,
    QueryByExampleExecutor<FileEntity> {
    fun findByBucketNameAndObjectKey(bucketName: String, objectKey: String): FileEntity?

    fun findByBucketNameAndObjectKeyStartingWith(bucketName: String, objectKeyPrefix: String): List<FileEntity>
}
