package example.process

object ColumnNormalizer {
    private fun dataType(value: Any?): String = when (value) {
        is Boolean -> "boolean"
        is Number -> "number"
        is String -> "string"
        else -> "unknown"
    }

    /** Same semantics as Python's str.title(). */
    private fun title(s: String): String {
        val sb = StringBuilder()
        var prevLetter = false
        for (c in s) {
            sb.append(if (prevLetter) c.lowercaseChar() else c.uppercaseChar())
            prevLetter = c.isLetter()
        }
        return sb.toString()
    }

    private fun columnsOf(row: Map<*, *>): List<Map<String, String>> =
        row.entries.map { (k, v) ->
            mapOf("key" to k.toString(), "label" to title(k.toString()), "type" to dataType(v))
        }

    fun collectColumnName(response: MutableMap<String, Any?>, keyField: String) {
        val columns = when (val value = response[keyField]) {
            is List<*> -> (value.firstOrNull() as? Map<*, *>)?.let(::columnsOf) ?: emptyList()
            is Map<*, *> -> columnsOf(value)
            else -> emptyList()
        }
        response["${keyField}_columns"] = columns
    }
}
