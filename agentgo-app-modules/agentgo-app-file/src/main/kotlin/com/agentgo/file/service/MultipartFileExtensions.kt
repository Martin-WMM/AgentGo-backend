package com.agentgo.file.service

import com.agentgo.file.exception.UnreadableUploadException
import org.springframework.web.multipart.MultipartFile

fun MultipartFile.bytes(): ByteArray = try {
    inputStream.use { it.readAllBytes() }
} catch (exception: Exception) {
    throw UnreadableUploadException()
}
