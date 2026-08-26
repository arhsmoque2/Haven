package sh.haven.feature.terminal.arh

import org.junit.Assert.assertTrue
import org.junit.Test

class TerminalMarkdownExporterTest {

    @Test
    fun `generateMarkdown includes session title, code blocks, and raw buffer`() {
        val lines = listOf(
            "user@haven:~$ ls -la",
            "Here is the diff:",
            "```diff",
            "+added line",
            "-removed line",
            "```",
            "Done."
        )

        val md = TerminalMarkdownExporter.generateMarkdown(
            sessionTitle = "DevSession-1",
            host = "100.64.0.1",
            lines = lines
        )

        assertTrue(md.contains("# 📜 Haven ARH Terminal Transcript: DevSession-1"))
        assertTrue(md.contains("- **Host / Target**: `100.64.0.1`"))
        assertTrue(md.contains("## 📦 Detected Code Blocks & Diffs"))
        assertTrue(md.contains("```diff"))
        assertTrue(md.contains("+added line"))
        assertTrue(md.contains("## 🖥️ Raw Terminal Buffer"))
        assertTrue(md.contains("user@haven:~$ ls -la"))
    }
}
