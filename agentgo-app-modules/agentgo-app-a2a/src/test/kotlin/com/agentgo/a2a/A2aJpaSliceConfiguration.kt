package com.agentgo.a2a

import org.springframework.boot.SpringBootConfiguration

/**
 * Anchor for A2A `@DataJpaTest` slices.
 *
 * This module has no `@SpringBootApplication`. Spring Boot's JPA slice searches
 * upward from the test package for this marker.
 */
@SpringBootConfiguration
class A2aJpaSliceConfiguration
