package com.agentgo.settings.controller.apidoc

import com.agentgo.commons.api.ApiPaths
import com.agentgo.commons.dto.http.response.CommonHttpResponse
import com.agentgo.dto.settings.UserSettingsResponse
import com.agentgo.dto.settings.UserSettingsUpdateRequest
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping

/**
 * Springdoc contract for the authenticated user's system settings at `/api/v1/settings`.
 *
 * The resource is a singleton. Ownership is resolved from the authentication context.
 * GET returns defaults and `persisted=false` when no row exists and does not write.
 * PUT creates or replaces the row.
 */
@Tag(
    name = "Settings",
    description = "Versioned system settings for the authenticated user at /api/v1/settings. " +
        "theme is LIGHT, DARK, or SYSTEM. locale is EN or ZH_CN. sidebarCollapsed and " +
        "reduceMotion are booleans. density is COMFORTABLE or COMPACT. " +
        "GET returns those defaults with persisted=false and does not insert a row. " +
        "PUT replaces the caller's single settings row and returns persisted=true. " +
        "Success codes are 9400 SETTINGS-RETRIEVED and 9401 SETTINGS-UPDATED. " +
        "An unrecognized enum value returns HTTP 400. Missing authentication uses AUTH-002.",
)
@RequestMapping(ApiPaths.V1_SETTINGS)
interface SettingsControllerApiDoc {
    @Operation(
        summary = "Get system settings for the authenticated user",
        description = "Returns the caller's saved settings, or the documented defaults when no row " +
            "exists. This request does not write. Returns 9400 SETTINGS-RETRIEVED.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "System settings retrieved", useReturnTypeSchema = true),
            ApiResponse(responseCode = "401", description = "Authentication required"),
        ],
    )
    @GetMapping
    fun get(): CommonHttpResponse<UserSettingsResponse>

    @Operation(
        summary = "Create or replace system settings for the authenticated user",
        description = "Replaces theme, locale, sidebarCollapsed, density, and reduceMotion for the " +
            "caller. An unrecognized enum value returns HTTP 400. Returns 9401 SETTINGS-UPDATED.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "System settings updated", useReturnTypeSchema = true),
            ApiResponse(responseCode = "400", description = "Invalid settings request"),
            ApiResponse(responseCode = "401", description = "Authentication required"),
        ],
    )
    @PutMapping
    fun update(@Valid @RequestBody request: UserSettingsUpdateRequest): CommonHttpResponse<UserSettingsResponse>
}
