package example.controller

import example.service.FastapiClient
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class DefaultController(private val fastapi: FastapiClient) {

    // Static routes
    @GetMapping("/api/data")
    fun data(): ResponseEntity<Any?> = fastapi.get("/api/data")

    @GetMapping("/api/index")
    fun index(): ResponseEntity<Any?> = fastapi.get("/api/index")
}
