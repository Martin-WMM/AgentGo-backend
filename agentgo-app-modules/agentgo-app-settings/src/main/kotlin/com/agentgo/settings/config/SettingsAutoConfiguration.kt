package com.agentgo.settings.config

import com.agentgo.settings.controller.SettingsController
import com.agentgo.settings.controller.SettingsExceptionHandler
import com.agentgo.settings.mapper.UserSettingsMapperImpl
import com.agentgo.settings.repository.UserSettingsRepository
import com.agentgo.settings.security.SettingsUserIdResolver
import com.agentgo.settings.service.impl.UserSettingsServiceImpl
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.persistence.autoconfigure.EntityScan
import org.springframework.context.annotation.Import
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

/**
 * Auto-configuration entry point for per-user system settings.
 *
 * Registers persistence scanning and the HTTP adapter. JPA auditing is provided by
 * the file module's existing auditor, which is already active in the application.
 */
@AutoConfiguration
@EntityScan("com.agentgo.settings.entity")
@EnableJpaRepositories(basePackageClasses = [UserSettingsRepository::class])
@Import(
    SettingsController::class,
    SettingsExceptionHandler::class,
    UserSettingsMapperImpl::class,
    UserSettingsServiceImpl::class,
    SettingsUserIdResolver::class,
)
class SettingsAutoConfiguration
