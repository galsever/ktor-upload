package org.srino.ktorupload.logic.requests

open class PresignedURLRequest(
    val contentType: String,
    val size: Long,
) {
}