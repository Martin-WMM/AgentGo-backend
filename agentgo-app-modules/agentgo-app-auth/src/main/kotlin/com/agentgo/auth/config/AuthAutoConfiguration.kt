package com.agentgo.auth.config

import com.agentgo.auth.controller.AuthController
import com.agentgo.auth.controller.AvatarController
import com.agentgo.auth.controller.AvatarExceptionHandler
import com.agentgo.auth.client.AuthentikProfileClientImpl
import com.agentgo.auth.properties.AuthProperties
import com.agentgo.auth.service.impl.AvatarServiceImpl
import com.agentgo.auth.service.impl.AuthServiceImpl
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Import

@AutoConfiguration
@EnableConfigurationProperties(AuthProperties::class)
@Import(
    AuthController::class,
    AvatarController::class,
    AvatarExceptionHandler::class,
    AuthentikProfileClientImpl::class,
    AvatarServiceImpl::class,
    AuthServiceImpl::class,
    SecurityConfig::class,
)
class AuthAutoConfiguration
