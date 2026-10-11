package com.agentgo.settings.controller

import com.agentgo.commons.annotation.AgentGoLogApi
import com.agentgo.commons.dto.http.response.CommonHttpResponse
import com.agentgo.commons.dto.http.response.status.success.SettingsResponseCodeRegistry
import com.agentgo.dto.settings.UserSettingsResponse
import com.agentgo.dto.settings.UserSettingsUpdateRequest
import com.agentgo.settings.controller.apidoc.SettingsControllerApiDoc
import com.agentgo.settings.service.UserSettingsService
import com.agentgo.starter.i18n.AgentGoMessages
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.RestController

/**
 * HTTP adapter for the authenticated user's system settings.
 *
 * @property userSettingsService user-scoped settings application service
 * @property messages localized API message resolver
 */
@RestController
class SettingsController(
    private val userSettingsService: UserSettingsService,
    private val messages: AgentGoMessages,
) : SettingsControllerApiDoc {
    @AgentGoLogApi(name = "settings.get")
    override fun get(): CommonHttpResponse<UserSettingsResponse> =
        CommonHttpResponse(
            code = SettingsResponseCodeRegistry.RETRIEVED,
            responseType = SettingsResponseCodeRegistry.RETRIEVED_TYPE,
            message = messages.get("settings.retrieved", defaultMessage = "System settings retrieved"),
            data = userSettingsService.get(),
        )

    @AgentGoLogApi(name = "settings.update")
    override fun update(@Valid request: UserSettingsUpdateRequest): CommonHttpResponse<UserSettingsResponse> =
        CommonHttpResponse(
            code = SettingsResponseCodeRegistry.UPDATED,
            responseType = SettingsResponseCodeRegistry.UPDATED_TYPE,
            message = messages.get("settings.updated", defaultMessage = "System settings updated"),
            data = userSettingsService.update(request),
        )
}
