package dev.voice.offline.ai.functioncalling

/**
 * Formats user speech transcripts into the exact Gemma chat-template and
 * FunctionGemma 270M Mobile Actions tool-declaration prompt.
 */
object FunctionCallingPromptBuilder {

    private const val BOS = "<bos>"
    private const val START_TURN = "<start_of_turn>"
    private const val END_TURN = "<end_of_turn>"

    fun buildPrompt(
        transcript: String,
        declarations: List<String> = ActionFunctionDeclaration.ALL_DECLARATIONS
    ): String {
        val sb = StringBuilder()
        sb.append(BOS)
        sb.append(START_TURN).append("developer\n")
        sb.append("You are a model that can do function calling with the following functions\n")
        for (decl in declarations) {
            sb.append(decl).append("\n")
        }
        sb.append(END_TURN).append("\n")
        sb.append(START_TURN).append("user\n")
        sb.append(transcript.trim()).append("\n")
        sb.append(END_TURN).append("\n")
        sb.append(START_TURN).append("model\n")
        return sb.toString()
    }
}
