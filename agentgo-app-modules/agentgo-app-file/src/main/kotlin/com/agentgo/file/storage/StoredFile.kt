package com.agentgo.file.storage

data class StoredFile(
    val bytes: ByteArray,
    val contentType: String,
    val filename: String,
)
