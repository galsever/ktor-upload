package org.srino.ktorupload.logic.responses

import org.srino.ktorupload.logic.results.PresignedUrlResult

class PresignedUrlResponse(
    val url: String?,
    val result: PresignedUrlResult
)