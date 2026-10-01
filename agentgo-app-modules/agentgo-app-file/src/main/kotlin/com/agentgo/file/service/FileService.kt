package com.agentgo.file.service

import com.agentgo.file.storage.StoredFile

/** Provides framework-independent operations against the configured MinIO bucket. */
interface FileService {
    /** Creates the configured bucket and its logical directory markers when necessary. */
    fun ensureBucket()

    /** Stores an object and returns the storage provider's entity tag when available. */
    fun putObject(
        objectKey: String,
        bytes: ByteArray,
        contentType: String,
        metadata: Map<String, String> = emptyMap(),
    ): String?

    /** Reads an object and returns it with the response metadata needed by HTTP clients. */
    fun getObject(objectKey: String, contentType: String, filename: String): StoredFile

    /** Removes an object from the configured bucket. */
    fun deleteObject(objectKey: String)
}
