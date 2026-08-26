package sh.haven.feature.terminal.arh

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CodeBlockParserTest {

    @Test
    fun `extracts multiple markdown code fences in reverse chronological order`() {
        val sampleTerminalOutput = """
            $ git status
            Here is the proposed fix:
            ```kotlin
            fun helloWorld() {
                println("Hello ARH Haven")
            }
            ```
            And here is a bash command:
            ```bash
            ./gradlew assembleArm64TerminalDebug
            ```
            Done.
        """.trimIndent()

        val blocks = CodeBlockParser.extract(sampleTerminalOutput)

        assertEquals(2, blocks.size)
        // Newest first
        assertEquals("BASH", blocks[0].language)
        assertEquals("./gradlew assembleArm64TerminalDebug", blocks[0].code)
        assertEquals(1, blocks[0].lineCount)
        assertFalse(blocks[0].isDiff)

        assertEquals("KOTLIN", blocks[1].language)
        assertTrue(blocks[1].code.contains("fun helloWorld()"))
        assertEquals(3, blocks[1].lineCount)
    }

    @Test
    fun `extracts raw git diff headers`() {
        val sampleDiffOutput = """
            diff --git a/app/build.gradle.kts b/app/build.gradle.kts
            --- a/app/build.gradle.kts
            +++ b/app/build.gradle.kts
            @@ -1,3 +1,3 @@
            -applicationId = "sh.haven"
            +applicationId = "com.arh.haven"
        """.trimIndent()

        val blocks = CodeBlockParser.extract(sampleDiffOutput)

        assertEquals(1, blocks.size)
        assertEquals("DIFF", blocks[0].language)
        assertTrue(blocks[0].isDiff)
        assertTrue(blocks[0].code.contains("applicationId = \"com.arh.haven\""))
    }

    @Test
    fun `returns empty list for plain terminal text without code blocks`() {
        val plainText = "user@devbox:~$ ls -la\ntotal 0\ndrwxr-xr-x 2 user user 64 Aug 27 02:00 ."
        val blocks = CodeBlockParser.extract(plainText)
        assertTrue(blocks.isEmpty())
    }
}
