package org.srino.ktorupload.logic.responses

import org.srino.ktorupload.logic.BucketObject
import org.srino.ktorupload.logic.results.CopyResult

class FinishUploadResponse(
    val bucketObject: BucketObject?,
    val any: Any?,
    val result: CopyResult,
) {
}