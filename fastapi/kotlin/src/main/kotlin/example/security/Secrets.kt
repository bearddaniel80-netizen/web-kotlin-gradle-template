package example.security

import java.nio.file.Files
import java.nio.file.Path

object Secrets {
    /** Docker secret first, then env var (local development outside Docker). */
    fun read(name: String): String {
        val path = Path.of("/run/secrets/$name")
        if (Files.exists(path)) return Files.readString(path).trim()
        return System.getenv(name) ?: error("Missing secret or environment variable: $name")
    }
}
