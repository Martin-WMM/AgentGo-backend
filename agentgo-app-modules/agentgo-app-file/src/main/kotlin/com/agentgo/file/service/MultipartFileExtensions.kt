package com.agentgo.file.service

import com.agentgo.file.exception.FileException
import org.springframework.http.HttpStatus
import org.springframework.web.multipart.MultipartFile

fun MultipartFile.bytes(): ByteArray = try {
    inputStream.use { it.readAllBytes() }
} catch (exception: Exception) {
    throw FileException(HttpStatus.BAD_REQUEST, "Unable to read uploaded file")
}
