package dev.voice.offline.ai.functioncalling

data class RawFunctionCall(
    val name: String,
    val arguments: Map<String, String>
)

/**
 * Deterministic model output parser supporting the FunctionGemma
 * function-call response format:
 * `<start_function_call>call:name{param1:<escape>value1<escape>}<end_function_call>`
 */
object FunctionCallParser {

    private val CALL_REGEX = Regex(
        """<start_function_call>\s*call:([a-zA-Z0-9_]+)\s*\{(.*?)\}\s*(?:<end_function_call>)?""",
        RegexOption.DOT_MATCHES_ALL
    )

    fun parse(modelOutput: String): RawFunctionCall? {
        val trimmed = modelOutput.trim()
        val match = CALL_REGEX.find(trimmed) ?: return null

        val functionName = match.groupValues[1].trim()
        val argsBlock = match.groupValues[2].trim()

        if (argsBlock.isEmpty()) {
            return RawFunctionCall(functionName, emptyMap())
        }

        val arguments = mutableMapOf<String, String>()
        val argRegex = Regex(
            """([a-zA-Z0-9_]+)\s*:\s*(?:<escape>(.*?)<escape>|([^,{}]+))""",
            RegexOption.DOT_MATCHES_ALL
        )

        for (m in argRegex.findAll(argsBlock)) {
            val key = m.groupValues[1].trim()
            val escapedVal = m.groupValues[2]
            val plainVal = m.groupValues[3]

            val value = if (escapedVal.isNotEmpty()) escapedVal else plainVal.trim()
            arguments[key] = value
        }

        return RawFunctionCall(functionName, arguments)
    }
}
