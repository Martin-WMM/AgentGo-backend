package com.agentgo.cli

import org.springframework.boot.SpringBootConfiguration

/**
 * This module has no `@SpringBootApplication`. Spring Boot's JPA slice searches
 * packages upwards from the test class and needs this marker configuration.
 */
@SpringBootConfiguration
class CliJpaSliceConfiguration
