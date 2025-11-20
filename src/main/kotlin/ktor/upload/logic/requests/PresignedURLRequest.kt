package org.srino.ktor.upload.logic.requests

open class PresignedURLRequest(
    val contentType: String,
    val size: Long,
) {
}