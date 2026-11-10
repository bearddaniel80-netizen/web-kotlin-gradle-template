package example.controller

import example.service.FastapiClient
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.server.ResponseStatusException

@RestController
class MinioController(private val fastapi: FastapiClient) {

    @PostMapping("/api/uploads")
    fun upload(
        @RequestPart(value = "file", required = false) file: MultipartFile?,
        @RequestParam(value = "description", required = false) description: String?,
    ): ResponseEntity<Any?> {
        if (file == null || file.isEmpty) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "No file uploaded")
        }

        val fields: Map<String, Any> =
            if (description == null) emptyMap() else mapOf("description" to description)

        try {
            return fastapi.uploadMetadata(
                "/api/uploads",
                file.originalFilename ?: "upload",
                file.contentType ?: "application/octet-stream",
                fields,
            )
        } catch (e: Exception) {
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Upload failed", e)
        }
    }
}
