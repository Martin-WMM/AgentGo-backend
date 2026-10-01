package com.agentgo.file.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "agentgo.file")
data class FileProperties(
    val minioEndpoint: String = "http://localhost:9001",
    val minioAccessKey: String = "minioadmin",
    val minioSecretKey: String = "local-minio-password-change-me",
    val bucketName: String = "agentgo-bucket",
    val avatarPrefix: String = "Avatars",
    val workspacePrefix: String = "workspace",
    val maxFileSizeBytes: Long = 10L * 1024 * 1024,
)
