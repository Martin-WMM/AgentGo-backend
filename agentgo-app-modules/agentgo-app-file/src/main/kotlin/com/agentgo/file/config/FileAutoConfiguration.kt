package com.agentgo.file.config

import com.agentgo.commons.dto.http.response.status.error.ExceptionResponseCodeRegistry
import com.agentgo.file.controller.FileController
import com.agentgo.file.controller.FileExceptionHandler
import com.agentgo.file.controller.UploadSizeExceptionHandler
import com.agentgo.file.exception.EmptyFileException
import com.agentgo.file.exception.FileTooLargeException
import com.agentgo.file.exception.InvalidWorkspacePathException
import com.agentgo.file.exception.UnreadableUploadException
import com.agentgo.file.filter.UploadSizeExceptionFilter
import com.agentgo.file.mapper.FileMapperImpl
import com.agentgo.file.properties.FileProperties
import com.agentgo.file.repository.FileRepository
import com.agentgo.file.service.impl.FileServiceImpl
import com.agentgo.file.service.impl.WorkspaceFileServiceImpl
import com.agentgo.file.storage.MinioBucketInitializer
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.persistence.autoconfigure.EntityScan
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.core.Ordered
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
    UploadSizeExceptionHandler::class,
    FileMapperImpl::class,
    FileServiceImpl::class,
    WorkspaceFileServiceImpl::class,
)
class FileAutoConfiguration {
    init {
        ExceptionResponseCodeRegistry.register<FileTooLargeException>(2011, "FILE-011")
        ExceptionResponseCodeRegistry.register<EmptyFileException>(2012, "FILE-012")
        ExceptionResponseCodeRegistry.register<InvalidWorkspacePathException>(2013, "FILE-013")
        ExceptionResponseCodeRegistry.register<UnreadableUploadException>(2014, "FILE-014")
    }

    /** Runs outside the security chain so an early multipart rejection still returns JSON. */
    @Bean
    fun uploadSizeExceptionFilter(
        properties: FileProperties,
    ): FilterRegistrationBean<UploadSizeExceptionFilter> =
        FilterRegistrationBean(UploadSizeExceptionFilter(properties)).apply {
            order = Ordered.HIGHEST_PRECEDENCE
            addUrlPatterns("/api/files/*")
        }
}
