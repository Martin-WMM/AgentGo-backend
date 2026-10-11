package com.agentgo.auth.service.impl

import com.agentgo.auth.client.AuthentikProfileClient
import com.agentgo.auth.entity.UserProfileEntity
import com.agentgo.auth.exception.InvalidProfileRequestException
import com.agentgo.auth.mapper.UserProfileMapper
import com.agentgo.auth.repository.UserProfileRepository
import com.agentgo.auth.service.UserProfileService
import com.agentgo.dto.auth.ProfileResponse
import com.agentgo.dto.auth.ProfileUpdateRequest
import java.net.URI
import java.net.URISyntaxException
import java.time.DateTimeException
import java.time.ZoneId
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate

/**
 * Default [UserProfileService] that keeps Authentik as the identity source and
 * `tbl_user_profile` as the agent-readable personal record.
 *
 * @property authentikProfileClient Authentik Core API client
 * @property userProfileRepository local profile persistence
 * @property userProfileMapper identity and detail mapping
 */
@Service
class UserProfileServiceImpl(
    private val authentikProfileClient: AuthentikProfileClient,
    private val userProfileRepository: UserProfileRepository,
    private val userProfileMapper: UserProfileMapper,
    transactionManager: PlatformTransactionManager,
) : UserProfileService {
    private val writeTransaction = TransactionTemplate(transactionManager)

    override fun currentProfile(subject: String, fallbackEmail: String?): ProfileResponse {
        val user = authentikProfileClient.findBySubject(subject, fallbackEmail)
        val saved = saveMerged(subject) { entity ->
            userProfileMapper.applyIdentity(entity, user)
        }
        return withAvatarFallback(userProfileMapper.toResponse(saved), user.avatar)
    }

    override fun updateProfile(
        subject: String,
        fallbackEmail: String?,
        request: ProfileUpdateRequest,
    ): ProfileResponse {
        val normalized = normalize(request)
        val current = authentikProfileClient.findBySubject(subject, fallbackEmail)
        val updated = authentikProfileClient.update(current, normalized)
        val saved = saveMerged(subject) { entity ->
            userProfileMapper.applyIdentity(entity, updated)
            userProfileMapper.applyDetails(entity, normalized)
        }
        return withAvatarFallback(userProfileMapper.toResponse(saved), updated.avatar ?: normalized.avatarUrl)
    }

    private fun saveMerged(subject: String, merge: (UserProfileEntity) -> Unit): UserProfileEntity =
        try {
            write {
                val entity = userProfileRepository.findByOwnerUserId(subject)
                    ?: UserProfileEntity(ownerUserId = subject)
                merge(entity)
                userProfileRepository.save(entity)
            }
        } catch (conflict: DataIntegrityViolationException) {
            write {
                val entity = userProfileRepository.findByOwnerUserId(subject) ?: throw conflict
                merge(entity)
                userProfileRepository.save(entity)
            }
        }

    private fun <T> write(action: () -> T): T =
        writeTransaction.execute { action() } ?: error("Profile transaction returned null")

    private fun normalize(request: ProfileUpdateRequest): ProfileUpdateRequest {
        val displayName = request.displayName.trim()
        if (displayName.isEmpty()) {
            invalid("profile.display-name.required", "Display name must not be blank.")
        }
        if (displayName.length > 150) {
            invalid("profile.display-name.too-long", "Display name must be at most 150 characters.")
        }
        return ProfileUpdateRequest().apply {
            this.displayName = displayName
            email = optionalEmail(request.email)
            avatarUrl = optionalAvatarUrl(request.avatarUrl)
            phoneNumber = optionalPhone(request.phoneNumber)
            description = optionalText(
                request.description,
                2000,
                "profile.description.too-long",
                "Description must be at most 2000 characters.",
            )
            occupation = optionalText(
                request.occupation,
                128,
                "profile.occupation.too-long",
                "Occupation must be at most 128 characters.",
            )
            organization = optionalText(
                request.organization,
                255,
                "profile.organization.too-long",
                "Organization must be at most 255 characters.",
            )
            homepageUrl = optionalHttpUrl(
                request.homepageUrl,
                "profile.homepage.invalid",
                "Homepage must be an absolute http or https URL.",
            )
            location = optionalText(
                request.location,
                255,
                "profile.location.too-long",
                "Location must be at most 255 characters.",
            )
            timezone = optionalTimezone(request.timezone)
            preferredLanguage = optionalLanguage(request.preferredLanguage)
        }
    }

    /**
     * Accepts http(s) URLs, AgentGo-relative avatar paths, and Authentik `data:image/` avatars.
     * Oversized `data:` values are still forwarded to Authentik but are not persisted locally.
     */
    private fun optionalAvatarUrl(value: String?): String? {
        val avatar = value?.trim()?.ifEmpty { null } ?: return null
        if (avatar.startsWith("data:image/", ignoreCase = true)) {
            return avatar
        }
        if (avatar.startsWith("/")) {
            if (avatar.length > 2048) {
                invalid("profile.avatar.invalid", "Avatar URL must be at most 2048 characters.")
            }
            return avatar
        }
        return optionalHttpUrl(
            avatar,
            "profile.avatar.invalid",
            "Avatar URL must be an absolute http or https URL, an AgentGo path, or a data image.",
        )
    }

    private fun withAvatarFallback(profile: ProfileResponse, fallbackAvatar: String?): ProfileResponse {
        if (!profile.avatarUrl.isNullOrBlank()) {
            return profile
        }
        val fallback = fallbackAvatar?.trim()?.ifEmpty { null } ?: return profile
        return profile.copy(avatarUrl = fallback)
    }

    private fun optionalEmail(value: String?): String? {
        val email = value?.trim()?.ifEmpty { null } ?: return null
        if (email.length > 254 || !EMAIL.matches(email)) {
            invalid("profile.email.invalid", "Email address is invalid.")
        }
        return email
    }

    private fun optionalPhone(value: String?): String? {
        val phone = value?.trim()?.ifEmpty { null } ?: return null
        if (phone.length > 32 || !phone.any(Char::isDigit) || !PHONE.matches(phone)) {
            invalid("profile.phone.invalid", "Phone number is invalid.")
        }
        return phone
    }

    private fun optionalText(value: String?, max: Int, code: String, message: String): String? {
        val text = value?.trim()?.ifEmpty { null } ?: return null
        if (text.length > max) invalid(code, message)
        return text
    }

    private fun optionalHttpUrl(value: String?, code: String, message: String): String? {
        val url = value?.trim()?.ifEmpty { null } ?: return null
        if (url.length > 2048 || !isHttpUrl(url)) invalid(code, message)
        return url
    }

    private fun optionalTimezone(value: String?): String? {
        val timezone = value?.trim()?.ifEmpty { null } ?: return null
        if (timezone.length > 64) {
            invalid("profile.timezone.invalid", "Time zone is invalid.")
        }
        try {
            return ZoneId.of(timezone).id
        } catch (_: DateTimeException) {
            invalid("profile.timezone.invalid", "Time zone is invalid.")
        }
    }

    private fun optionalLanguage(value: String?): String? {
        val language = value?.trim()?.ifEmpty { null } ?: return null
        if (!LANGUAGE.matches(language)) {
            invalid("profile.language.invalid", "Preferred language must be a BCP 47 tag such as zh-CN.")
        }
        return language
    }

    private fun isHttpUrl(value: String): Boolean {
        val uri = try {
            URI(value)
        } catch (_: URISyntaxException) {
            return false
        }
        val scheme = uri.scheme?.lowercase()
        return (scheme == "http" || scheme == "https") && !uri.host.isNullOrBlank()
    }

    private fun invalid(code: String, message: String): Nothing =
        throw InvalidProfileRequestException(code, message)

    private companion object {
        val EMAIL = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")
        val PHONE = Regex("^[+0-9(][0-9 ().-]{0,31}$")
        val LANGUAGE = Regex("^[A-Za-z]{2,3}(-[A-Za-z0-9]{2,8}){0,3}$")
    }
}
