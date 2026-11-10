package example.web

import example.security.InternalKeyVerifier
import io.minio.GetPresignedObjectUrlArgs
import io.minio.MinioClient
import io.minio.http.Method
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import java.util.UUID
import java.util.concurrent.TimeUnit

@RestController
class MinioController(
    private val minio: MinioClient,
    private val keys: InternalKeyVerifier,
    @Value("\${minio.bucket}") private val bucket: String,
) {
    @PostMapping("/api/uploads")
    fun createUploadUrl(
        @RequestBody file: UploadUrlRequest,
        @RequestHeader("x-internal-key", required = false) key: String?,
    ): Map<String, String> {
        keys.verify(key)
        val extension = if ("." in file.filename) "." + file.filename.substringAfterLast(".") else ""
        val objectName = "${UUID.randomUUID()}$extension"
        return try {
            val url = minio.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                    .method(Method.PUT)
                    .bucket(bucket)
                    .`object`(objectName)
                    .expiry(1, TimeUnit.DAYS)
                    .build()
            )
            mapOf("object_name" to objectName, "url" to url)
        } catch (e: Exception) {
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.message)
        }
    }

    // NOTE: unauthenticated, exactly like the Python original.
    @GetMapping("/api/downloads/{*objectName}")
    fun createDownloadUrl(@PathVariable objectName: String): Map<String, String> =
        try {
            val url = minio.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                    .method(Method.GET)
                    .bucket(bucket)
                    .`object`(objectName.removePrefix("/"))
                    .expiry(10, TimeUnit.MINUTES)
                    .build()
            )
            mapOf("url" to url)
        } catch (e: Exception) {
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.message)
        }
}
