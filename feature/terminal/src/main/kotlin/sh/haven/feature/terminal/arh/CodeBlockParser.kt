package sh.haven.feature.terminal.arh

/**
 * Represents a parsed code block or git diff extracted from the terminal scrollback buffer.
 */
data class ExtractedCodeBlock(
    val id: String,
    val language: String,
    val code: String,
    val lineCount: Int,
    val isDiff: Boolean = false,
)

object CodeBlockParser {
    private val CODE_FENCE_REGEX = Regex("```([a-zA-Z0-9_#-]*)\\s*\\n([\\s\\S]*?)```")
    private val DIFF_HEADER_REGEX = Regex("(?:^|\\n)(diff --git a/.*?\\n(?:---|\\+\\+\\+|@@)[\\s\\S]*?)(?=\\n(?:diff --git|$))")

    /**
     * Extracts all markdown code fences and git diff sections from [bufferText].
     * Returns the blocks in reverse chronological order (newest snippet first).
     */
    fun extract(bufferText: String): List<ExtractedCodeBlock> {
        if (bufferText.isBlank()) return emptyList()

        val results = mutableListOf<ExtractedCodeBlock>()
        var counter = 1

        // 1. Extract markdown code blocks
        CODE_FENCE_REGEX.findAll(bufferText).forEach { match ->
            val lang = match.groupValues[1].trim().ifEmpty { "TEXT" }
            val content = match.groupValues[2].trimEnd()
            if (content.isNotBlank()) {
                val lines = content.lines()
                results.add(
                    ExtractedCodeBlock(
                        id = "code_${counter++}",
                        language = lang.uppercase(),
                        code = content,
                        lineCount = lines.size,
                        isDiff = lang.equals("diff", ignoreCase = true)
                    )
                )
            }
        }

        // 2. Extract raw git diff sections if not wrapped in fences
        DIFF_HEADER_REGEX.findAll(bufferText).forEach { match ->
            val diffContent = match.groupValues[1].trim()
            if (diffContent.isNotBlank() && results.none { it.code.contains(diffContent) }) {
                val lines = diffContent.lines()
                results.add(
                    ExtractedCodeBlock(
                        id = "diff_${counter++}",
                        language = "DIFF",
                        code = diffContent,
                        lineCount = lines.size,
                        isDiff = true
                    )
                )
            }
        }

        return results.reversed()
    }
}
