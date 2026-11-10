package example.web

import example.process.ReportRenderer
import example.security.InternalKeyVerifier
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController

@RestController
class MainController(
    private val keys: InternalKeyVerifier,
    private val renderer: ReportRenderer,
) {
    @GetMapping("/health")
    fun health() = mapOf("status" to "ok")

    @GetMapping("/api/data")
    fun data(@RequestHeader("x-internal-key", required = false) key: String?): Map<String, String> {
        keys.verify(key)
        return mapOf("message" to "Hello from Spring Boot")
    }
}
