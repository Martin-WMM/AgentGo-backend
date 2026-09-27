package com.agentgo.file.config

import com.agentgo.file.controller.FileController
import com.agentgo.file.controller.FileExceptionHandler
import com.agentgo.file.mapper.FileMapperImpl
import com.agentgo.file.properties.FileProperties
import com.agentgo.file.repository.FileRepository
import com.agentgo.file.service.impl.FileServiceImpl
import com.agentgo.file.service.impl.WorkspaceFileServiceImpl
import com.agentgo.file.storage.MinioBucketInitializer
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.persistence.autoconfigure.EntityScan
import org.springframework.context.annotation.Import
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@AutoConfiguration
@EnableConfigurationProperties(FileProperties::class)
@EntityScan("com.agentgo.file.entity")
@EnableJpaRepositories(basePackageClasses = [FileRepository::class])
@Import(
    MinioConfiguration::class,
    MinioBucketInitializer::class,
    JpaAuditingConfiguration::class,
    FileController::class,
    FileExceptionHandler::class,
    FileMapperImpl::class,
    FileServiceImpl::class,
    WorkspaceFileServiceImpl::class,
)
class FileAutoConfiguration
