package com.agentgo.events

import org.springframework.boot.SpringBootConfiguration

/**
 * Anchor for system-event `@DataJpaTest` slices.
 *
 * This module has no `@SpringBootApplication`. Spring Boot's JPA slice searches
 * upward from the test package for this marker.
 */
@SpringBootConfiguration
class EventsJpaSliceConfiguration
