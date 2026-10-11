package com.agentgo.prompts

import org.springframework.boot.SpringBootConfiguration

/**
 * Anchor for prompt-template `@DataJpaTest` slices.
 *
 * This module has no `@SpringBootApplication`, and Spring Boot's JPA slice searches
 * upward from the test package for this marker.
 */
@SpringBootConfiguration
class PromptTemplatesJpaSliceConfiguration
