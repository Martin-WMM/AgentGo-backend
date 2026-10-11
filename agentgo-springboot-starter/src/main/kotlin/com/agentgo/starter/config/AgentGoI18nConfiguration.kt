package com.agentgo.starter.config

import com.agentgo.starter.i18n.AgentGoMessages
import java.util.Locale
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.MessageSource
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.context.support.ReloadableResourceBundleMessageSource
import org.springframework.web.servlet.LocaleResolver
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver

/**
 * Configures Accept-Language driven locale resolution and shared message bundles.
 *
 * Frontend clients send `Accept-Language: en` or `Accept-Language: zh-CN`. Controllers
 * resolve response `message` text through [AgentGoMessages].
 */
@AutoConfiguration
@Import(AgentGoMessages::class)
class AgentGoI18nConfiguration {
    /**
     * Builds the shared message source for API response text.
     *
     * @return UTF-8 message source rooted at `classpath:i18n/messages`
     */
    @Bean
    @ConditionalOnMissingBean(MessageSource::class)
    fun agentGoMessageSource(): MessageSource =
        ReloadableResourceBundleMessageSource().apply {
            setBasenames("classpath:i18n/messages")
            setDefaultEncoding("UTF-8")
            setFallbackToSystemLocale(false)
            setDefaultLocale(Locale.ENGLISH)
        }

    /**
     * Resolves the request locale from the `Accept-Language` header.
     *
     * @return locale resolver limited to English and Simplified Chinese
     */
    @Bean
    @ConditionalOnMissingBean(LocaleResolver::class)
    fun localeResolver(): LocaleResolver =
        AcceptHeaderLocaleResolver().apply {
            supportedLocales = listOf(Locale.ENGLISH, Locale.SIMPLIFIED_CHINESE, Locale.forLanguageTag("zh-CN"))
            setDefaultLocale(Locale.ENGLISH)
        }
}
