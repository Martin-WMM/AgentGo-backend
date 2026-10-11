package com.agentgo.starter.i18n

import java.util.Locale
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.stereotype.Component

/**
 * Resolves localized API messages from the shared Spring [MessageSource].
 *
 * Controllers and exception handlers should use this helper for the `message`
 * field of [com.agentgo.commons.dto.http.response.CommonHttpResponse] so the
 * value follows the request `Accept-Language` header.
 *
 * @property messageSource Spring message bundle
 */
@Component
class AgentGoMessages(
    private val messageSource: MessageSource,
) {
    /**
     * Resolves [code] for the current request locale.
     *
     * @param code message key such as `workspace.created`
     * @param args optional format arguments
     * @param defaultMessage fallback when the key is missing
     * @return localized message text
     */
    fun get(
        code: String,
        vararg args: Any,
        defaultMessage: String = code,
    ): String = get(code, args, LocaleContextHolder.getLocale(), defaultMessage)

    /**
     * Resolves [code] for an explicit [locale].
     *
     * @param code message key
     * @param args format arguments
     * @param locale target locale
     * @param defaultMessage fallback when the key is missing
     * @return localized message text
     */
    fun get(
        code: String,
        args: Array<out Any>,
        locale: Locale,
        defaultMessage: String = code,
    ): String = messageSource.getMessage(code, args, defaultMessage, locale) ?: defaultMessage
}
