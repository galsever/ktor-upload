package org.srino.ktorupload.logic

import org.srino.ktorupload.KtorUpload

class BucketObject(
    val bucket: String,
    val key: String,
) {

    fun url(ktorUpload: KtorUpload) = "${bucket}${ktorUpload.websiteEndpoint}/${key}"

}