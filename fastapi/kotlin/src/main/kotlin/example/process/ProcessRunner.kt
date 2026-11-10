package example.process

import org.springframework.stereotype.Component

@Component
class ProcessRunner {
    /**
     * Runs a command and returns stdout. Mirrors the Python behaviour:
     * exit code is ignored, and if the process can't be started the
     * command line itself is returned.
     */
    fun run(command: List<String>): String =
        try {
            val p = ProcessBuilder(command)
                .redirectError(ProcessBuilder.Redirect.DISCARD)
                .start()
            p.outputStream.close()
            val out = p.inputStream.bufferedReader().use { it.readText() }
            p.waitFor()
            out
        } catch (e: Exception) {
            command.joinToString(" ")
        }
}
