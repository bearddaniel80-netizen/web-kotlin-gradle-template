package example.web

import com.fasterxml.jackson.annotation.JsonProperty

data class FileList(val name: String) {
    fun toDict(): Map<String, String> = mapOf("name" to name)
}

data class UploadUrlRequest(
    @JsonProperty("content_type") val contentType: String,
    val filename: String,
)
