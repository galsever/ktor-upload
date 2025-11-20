package org.srino.ktor.upload.logic.results

enum class PresignedUrlResult {
    SUCCESS,
    TOO_BIG,
    INVALID_CONTENT_TYPE;
}