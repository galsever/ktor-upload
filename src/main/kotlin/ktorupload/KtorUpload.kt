package org.srino.ktorupload

import aws.sdk.kotlin.runtime.auth.credentials.StaticCredentialsProvider
import aws.sdk.kotlin.services.s3.S3Client
import aws.smithy.kotlin.runtime.auth.awscredentials.Credentials
import aws.smithy.kotlin.runtime.net.url.Url

class KtorUpload internal constructor(builder: Builder) {

    init {
        requireNotNull(builder.endpoint) { "endpoint must be provided" }
        requireNotNull(builder.region) { "region must be provided" }
        requireNotNull(builder.accessKey) { "accessKey must be provided" }
        requireNotNull(builder.secretKey) { "secretKey must be provided" }
        requireNotNull(builder.websiteEndpoint) { "websiteEndpoint must be provided" }
    }

    val websiteEndpoint: String = builder.websiteEndpoint!!

    val s3Client: S3Client = S3Client {
        credentialsProvider = StaticCredentialsProvider(
            Credentials(builder.accessKey!!, builder.secretKey!!)
        )
        region = builder.region!!
        endpointUrl = Url.parse(builder.endpoint!!)
    }

    companion object {
        operator fun invoke(block: Builder.() -> Unit): KtorUpload = Builder().apply(block).build()
    }

    class Builder {
        var endpoint: String? = null
        var region: String? = null
        var accessKey: String? = null
        var secretKey: String? = null

        var websiteEndpoint: String? = null

        fun build(): KtorUpload {
            return KtorUpload(this)
        }
    }
}