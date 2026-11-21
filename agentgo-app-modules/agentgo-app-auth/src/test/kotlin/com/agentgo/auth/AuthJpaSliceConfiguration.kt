package com.agentgo.auth

import org.springframework.boot.SpringBootConfiguration

/**
 * Anchor for auth-module `@DataJpaTest` slices.
 *
 * The auth module has no `@SpringBootApplication`, and Spring Boot's JPA slice
 * searches upward from the test package for this marker.
 */
@SpringBootConfiguration
class AuthJpaSliceConfiguration
