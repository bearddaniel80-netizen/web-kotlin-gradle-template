package example.web

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.server.ResponseStatusException

/** Keeps FastAPI's {"detail": "..."} error shape so the frontend doesn't change. */
@RestControllerAdvice
class ApiExceptionHandler {
    @ExceptionHandler(ResponseStatusException::class)
    fun handle(e: ResponseStatusException): ResponseEntity<Map<String, String?>> =
        ResponseEntity.status(e.statusCode).body(mapOf("detail" to e.reason))
}
