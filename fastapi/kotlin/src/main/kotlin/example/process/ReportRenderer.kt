package example.process

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.stereotype.Service
import java.nio.file.Files
import java.nio.file.Path

@Service
class ReportRenderer(
    private val runner: ProcessRunner,
    private val mapper: ObjectMapper,
) {
    private val cwd: Path = Path.of("").toAbsolutePath()

    fun run(command: List<String>): String = runner.run(command)

    fun convertMarkdown(inputFile: Path, outputFile: Path) {
        runner.run(
            listOf("pandoc", "-f", "markdown-yaml_metadata_block", inputFile.toString(), "-o", outputFile.toString())
        )
    }

    private fun isNonEmpty(v: Any?): Boolean = when (v) {
        is Collection<*> -> v.isNotEmpty()
        is Map<*, *> -> v.isNotEmpty()
        else -> false
    }

    fun renderJson(command: List<String>): Map<String, Any?> {
        runner.run(command)
        val response: MutableMap<String, Any?> = mapper.readValue(cwd.resolve("report.json").toFile())
        ColumnNormalizer.collectColumnName(response, "summary")
        ColumnNormalizer.collectColumnName(response, "results")
        if (isNonEmpty(response["query_performance"])) ColumnNormalizer.collectColumnName(response, "query_performance")
        if (isNonEmpty(response["failures"])) ColumnNormalizer.collectColumnName(response, "failures")
        return response
    }

    fun renderGithub(command: List<String>): String = renderMarkdownFile("github-report", command)

    fun renderMarkdown(command: List<String>): String = renderMarkdownFile("report", command)

    private fun renderMarkdownFile(name: String, command: List<String>): String {
        val input = cwd.resolve("$name.md")
        val output = cwd.resolve("$name.html")
        runner.run(command)
        convertMarkdown(input, output)
        return Files.readString(output)
    }

    fun renderHtml(command: List<String>): String {
        runner.run(command)
        return Files.readString(cwd.resolve("index.html"))
    }

    fun renderPdf(command: List<String>): String {
        val input = cwd.resolve("report.pdf")
        val output = cwd.resolve("report.html")
        runner.run(command)
        runner.run(listOf("pdftohtml", input.toString(), output.toString()))
        return Files.readString(output)
    }

    fun renderReadme(): String {
        val input = cwd.resolve("README.md")
        val output = cwd.resolve("README.html")
        convertMarkdown(input, output)
        return Files.readString(output)
    }
}
