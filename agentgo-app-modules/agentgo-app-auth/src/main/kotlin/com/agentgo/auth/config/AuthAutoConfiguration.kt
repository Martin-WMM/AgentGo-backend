package com.agentgo.auth.config

import com.agentgo.auth.controller.AuthController
import com.agentgo.auth.controller.AvatarController
import com.agentgo.auth.controller.AvatarExceptionHandler
import com.agentgo.auth.controller.ProfileExceptionHandler
import com.agentgo.auth.client.AuthentikProfileClientImpl
import com.agentgo.auth.exception.InvalidProfileRequestException
import com.agentgo.auth.mapper.UserProfileMapperImpl
import com.agentgo.auth.properties.AuthProperties
import com.agentgo.auth.repository.UserProfileRepository
import com.agentgo.auth.service.impl.AvatarServiceImpl
import com.agentgo.auth.service.impl.AuthServiceImpl
import com.agentgo.auth.service.impl.UserProfileServiceImpl
import com.agentgo.commons.dto.http.response.status.error.ExceptionResponseCodeRegistry
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.persistence.autoconfigure.EntityScan
import org.springframework.context.annotation.Import
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@AutoConfiguration
@EnableConfigurationProperties(AuthProperties::class)
@EntityScan("com.agentgo.auth.entity")
@EnableJpaRepositories(basePackageClasses = [UserProfileRepository::class])
@Import(
    AuthController::class,
    AvatarController::class,
    AvatarExceptionHandler::class,
    ProfileExceptionHandler::class,
    AuthentikProfileClientImpl::class,
    AvatarServiceImpl::class,
    AuthServiceImpl::class,
    UserProfileMapperImpl::class,
    UserProfileServiceImpl::class,
    SecurityConfig::class,
)
class AuthAutoConfiguration {
    init {
        ExceptionResponseCodeRegistry.register<InvalidProfileRequestException>(1006, "AUTH-006")
    }
}
