package com.agentgo.file.config

import com.agentgo.file.properties.FileProperties
import io.minio.MinioClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class MinioConfiguration {
    @Bean
    fun minioClient(properties: FileProperties): MinioClient = MinioClient.builder()
        .endpoint(properties.minioEndpoint)
        .credentials(properties.minioAccessKey, properties.minioSecretKey)
        .build()
}
