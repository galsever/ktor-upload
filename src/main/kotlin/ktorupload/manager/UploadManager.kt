package org.srino.ktorupload.manager

import aws.sdk.kotlin.services.s3.model.CopyObjectRequest
import aws.sdk.kotlin.services.s3.model.GetObjectRequest
import aws.sdk.kotlin.services.s3.model.PutObjectRequest
import aws.sdk.kotlin.services.s3.model.S3Exception
import aws.sdk.kotlin.services.s3.presigners.presignPutObject
import aws.smithy.kotlin.runtime.content.ByteStream
import aws.smithy.kotlin.runtime.content.toByteArray
import org.srino.ktorupload.KtorUpload
import org.srino.ktorupload.logic.BucketObject
import org.srino.ktorupload.logic.responses.PresignedUrlResponse
import org.srino.ktorupload.logic.Upload
import org.srino.ktorupload.logic.requests.FinishUploadRequest
import org.srino.ktorupload.logic.responses.FinishUploadResponse
import org.srino.ktorupload.logic.results.CopyResult
import org.srino.ktorupload.logic.results.PresignedUrlResult
import kotlin.time.Duration

abstract class UploadManager(
    val ktorUpload: KtorUpload,
    val temporaryBucketName: String,
    val bucketName: String,
    val acceptedContentTypes: List<String>,
    val validityWindow: Duration,
    val maxSize: Long,
    val objectMetadata: Map<String, String>? = null,
) {

    val uploads: MutableMap<String, Upload> = mutableMapOf()

    suspend fun presignedUrl(objectKey: String, fileSize: Long, fileContentType: String, createUpload: (fileContentType: String, fileSize: Long) -> Upload = {fileContentType, fileSize ->
        Upload(fileContentType, fileSize)
    }): PresignedUrlResponse {

        if (fileSize > maxSize) return PresignedUrlResponse(null, PresignedUrlResult.TOO_BIG)
        if (fileContentType !in acceptedContentTypes) return PresignedUrlResponse(
            null,
            PresignedUrlResult.INVALID_CONTENT_TYPE
        )

        val putObjectRequest = PutObjectRequest {
            bucket = temporaryBucketName
            key = objectKey
            contentType = fileContentType
            metadata = objectMetadata
            contentLength = fileSize
        }

        val presigned = ktorUpload.s3Client.presignPutObject(putObjectRequest, validityWindow)
        val url = presigned.url.toString()

        val upload = createUpload(fileContentType, fileSize)
        uploads[objectKey] = upload

        return PresignedUrlResponse(url, PresignedUrlResult.SUCCESS)
    }

    suspend fun finishedUpload(objectKey: String, afterUpload: ((upload: Upload, bucketObject: BucketObject) -> Any?)? = null, processImage: ((ByteArray) -> ByteArray)? = null): FinishUploadResponse {

        val upload = uploads[objectKey] ?: return FinishUploadResponse(null, null, CopyResult.ALREADY_PROCESSED)
        uploads.remove(objectKey)

        val result = copyToOfficialBucket(objectKey, objectKey)
        if (result != CopyResult.SUCCESS) return FinishUploadResponse(null, null, result)

        if (processImage != null) {
            val downloaded = bytes(objectKey) ?: return FinishUploadResponse(null, null, CopyResult.KEY_NOT_FOUND)
            val processed = processImage(downloaded)

            val putObjectRequest = PutObjectRequest {
                bucket = bucketName
                key = objectKey
                contentType = upload.contentType
                body = ByteStream.fromBytes(processed)
            }

            ktorUpload.s3Client.putObject(putObjectRequest)

        }

        val toReturn = afterUpload?.invoke(upload, BucketObject(bucketName, objectKey))

        return FinishUploadResponse(BucketObject(bucketName, objectKey), toReturn, CopyResult.SUCCESS)

    }

    private suspend fun copyToOfficialBucket(oldKey: String, newKey: String): CopyResult {
        val copyRequest = CopyObjectRequest {
            bucket = bucketName
            key = newKey
            copySource = "$temporaryBucketName/$oldKey"
        }

        try {
            ktorUpload.s3Client.copyObject(copyRequest)
            return CopyResult.SUCCESS
        } catch (e: S3Exception) {
            when (e.message) {
                "Key not found" -> return CopyResult.KEY_NOT_FOUND
            }
            return CopyResult.OTHER
        }


    }

    private suspend fun bytes(objectKey: String): ByteArray? {
        val getObjectRequest = GetObjectRequest {
            bucket = bucketName
            key = objectKey
        }
        return ktorUpload.s3Client.getObject(getObjectRequest) { response ->
            response.body?.toByteArray()
        }
    }
}

public inline val Int.mb: Long get() = this * 1024L * 1024L