package org.srino.ktor.upload.logic

import org.srino.ktor.upload.logic.results.PresignedUrlResult

class PresignedUrlResponse(
    val url: String?,
    val result: PresignedUrlResult
)