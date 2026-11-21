package com.agentgo.file

import org.springframework.boot.SpringBootConfiguration

/**
 * Anchor for file-module `@DataJpaTest` slices.
 *
 * This module has no `@SpringBootApplication`, and Spring Boot's JPA slice searches
 * upward from the test package for this marker.
 */
@SpringBootConfiguration
class FileJpaSliceConfiguration
