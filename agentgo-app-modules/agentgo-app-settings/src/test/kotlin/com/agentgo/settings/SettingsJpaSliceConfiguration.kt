package com.agentgo.settings

import org.springframework.boot.SpringBootConfiguration

/**
 * Anchor for settings-module `@DataJpaTest` slices.
 *
 * The settings module has no `@SpringBootApplication`, and Spring Boot's JPA slice
 * searches upward from the test package for this marker.
 */
@SpringBootConfiguration
class SettingsJpaSliceConfiguration
