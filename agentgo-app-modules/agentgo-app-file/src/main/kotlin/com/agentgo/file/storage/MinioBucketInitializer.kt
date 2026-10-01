package com.agentgo.file.storage

import com.agentgo.file.service.FileService
import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener

class MinioBucketInitializer(
    private val fileService: FileService,
) {
    private val logger = LoggerFactory.getLogger(MinioBucketInitializer::class.java)

    @EventListener(ApplicationReadyEvent::class)
    fun initialize() {
        try {
            fileService.ensureBucket()
        } catch (exception: Exception) {
            logger.warn("MinIO bucket initialization deferred until the first file operation", exception)
        }
    }
}
