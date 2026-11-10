package example.service

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.util.StringUtils
import org.springframework.web.client.RestClient
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path

@Service
class FastapiClient(
    builder: RestClient.Builder,
    private val objectMapper: ObjectMapper,
    @Value("\${fastapi.url}") fastapiUrl: String,
    @Value("\${fastapi.secret-file:/run/secrets/fastapi_internal_key}") secretFile: String,
) {
    private val restClient: RestClient

    init {
        check(StringUtils.hasText(fastapiUrl)) { "FASTAPI_URL must be configured" }

        val internalKey = try {
            Files.readString(Path.of(secretFile), StandardCharsets.UTF_8).trim()
        } catch (e: IOException) {
            throw IllegalStateException("Could not read FastAPI internal secret: $secretFile", e)
        }

        check(StringUtils.hasText(internalKey)) { "FastAPI internal secret is empty: $secretFile" }

        restClient = builder
            .baseUrl(fastapiUrl.replace(Regex("/+$"), ""))
            .defaultHeader("X-Internal-Key", internalKey)
            .defaultHeader(HttpHeaders.ACCEPT, MediaType.ALL_VALUE)
            .build()
    }

    fun get(path: String): ResponseEntity<Any?> = get(path, null, emptyMap())

    fun get(path: String, body: Any?, headers: Map<String, String>): ResponseEntity<Any?> =
        execute(HttpMethod.GET, path, body, headers)

    fun post(path: String, body: Any?, headers: Map<String, String>): ResponseEntity<Any?> =
        execute(HttpMethod.POST, path, body, headers)

    /**
     * Mirrors the original Node helper: sends filename/content-type/fields as JSON.
     * It intentionally does not forward the uploaded file bytes.
     */
    fun uploadMetadata(
        path: String,
        filename: String,
        contentType: String,
        fields: Map<String, Any>?,
    ): ResponseEntity<Any?> {
        val payload = linkedMapOf<String, Any>(
            "filename" to filename,
            "content_type" to contentType,
        )
        fields?.let { payload.putAll(it) }
        return post(path, payload, emptyMap())
    }

    private fun execute(
        method: HttpMethod,
        path: String,
        body: Any?,
        extraHeaders: Map<String, String>?,
    ): ResponseEntity<Any?> {
        val request = restClient.method(method).uri(path)
        extraHeaders?.forEach { (name, value) -> request.header(name, value) }
        if (body != null) {
            request.contentType(MediaType.APPLICATION_JSON)
            request.body(body)
        }

        return request.exchange { _, response ->
            val status = response.statusCode
            val contentType = response.headers.contentType
            val bytes = response.body.readAllBytes()
            val isJson = contentType != null &&
                (MediaType.APPLICATION_JSON.isCompatibleWith(contentType) ||
                    contentType.subtype.endsWith("+json"))

            val data: Any? = if (isJson) {
                if (bytes.isEmpty()) {
                    null
                } else {
                    try {
                        objectMapper.readValue(bytes, Any::class.java)
                    } catch (_: Exception) {
                        String(bytes, StandardCharsets.UTF_8)
                    }
                }
            } else {
                String(bytes, StandardCharsets.UTF_8)
            }

            ResponseEntity.status(status).body(data)
        }!!
    }
}
