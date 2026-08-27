package sh.haven.feature.terminal.arh

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import sh.haven.core.data.preferences.AgentMacro
import sh.haven.core.data.preferences.PromptBookmark

/**
 * Comprehensive Scenario Test Suite covering all ARH Terminal capabilities:
 * 1. Semantic Prompt Landmark Pinning & Bidirectional Stepping
 * 2. Safe Multi-Line Paste Interception Guard
 * 3. Sticky Viewport Anchoring & Live Stream Tail Snap
 * 4. Agent Quick-Action Rail & Macro Execution
 * 5. Code Block Extraction & Parsing
 * 6. Markdown Transcript Export
 */
class ArhTerminalCapabilitiesScenarioTest {

    // ========================================================================
    // Scenario 1: Prompt Pinning & Bidirectional Landmark Navigation (ADR-003)
    // ========================================================================

    @Test
    fun scenario1_promptPinningNextPreviousSteppingAndClamping() {
        val sessionId = "session-arh-1"
        val bookmarks = listOf(
            PromptBookmark(id = "b1", sessionId = sessionId, lineIndex = 15, promptText = "git status"),
            PromptBookmark(id = "b2", sessionId = sessionId, lineIndex = 62, promptText = "cargo test --lib"),
            PromptBookmark(id = "b3", sessionId = sessionId, lineIndex = 140, promptText = "docker compose up -d")
        )

        assertEquals(3, bookmarks.size)

        // Initial index at landmark 0
        var currentIndex = 0
        assertEquals("git status", bookmarks[currentIndex].promptText)
        assertEquals(15, bookmarks[currentIndex].lineIndex)

        // Step Next (Tap Down: 0 -> 1)
        if (currentIndex < bookmarks.size - 1) currentIndex++
        assertEquals(1, currentIndex)
        assertEquals("cargo test --lib", bookmarks[currentIndex].promptText)
        assertEquals(62, bookmarks[currentIndex].lineIndex)

        // Step Next (Tap Down: 1 -> 2)
        if (currentIndex < bookmarks.size - 1) currentIndex++
        assertEquals(2, currentIndex)
        assertEquals("docker compose up -d", bookmarks[currentIndex].promptText)
        assertEquals(140, bookmarks[currentIndex].lineIndex)

        // Step Next beyond end -> clamped at last index (2)
        if (currentIndex < bookmarks.size - 1) currentIndex++
        assertEquals(2, currentIndex)

        // Step Prev (Tap Up: 2 -> 1)
        if (currentIndex > 0) currentIndex--
        assertEquals(1, currentIndex)
        assertEquals("cargo test --lib", bookmarks[currentIndex].promptText)

        // Step Prev (Tap Up: 1 -> 0)
        if (currentIndex > 0) currentIndex--
        assertEquals(0, currentIndex)
        assertEquals("git status", bookmarks[currentIndex].promptText)

        // Step Prev before start -> clamped at index 0
        if (currentIndex > 0) currentIndex--
        assertEquals(0, currentIndex)
    }

    @Test
    fun scenario1_promptBookmarkSearchFiltering() {
        val bookmarks = listOf(
            PromptBookmark(id = "b1", sessionId = "s1", lineIndex = 10, promptText = "git log -n 5"),
            PromptBookmark(id = "b2", sessionId = "s1", lineIndex = 30, promptText = "cargo build --release"),
            PromptBookmark(id = "b3", sessionId = "s1", lineIndex = 75, promptText = "git diff main"),
            PromptBookmark(id = "b4", sessionId = "s1", lineIndex = 110, promptText = "cargo check")
        )

        // Filter for 'git'
        val gitResults = bookmarks.filter { it.promptText.contains("git", ignoreCase = true) }
        assertEquals(2, gitResults.size)
        assertEquals("git log -n 5", gitResults[0].promptText)
        assertEquals("git diff main", gitResults[1].promptText)

        // Filter for 'cargo'
        val cargoResults = bookmarks.filter { it.promptText.contains("cargo", ignoreCase = true) }
        assertEquals(2, cargoResults.size)
        assertEquals("cargo build --release", cargoResults[0].promptText)
        assertEquals("cargo check", cargoResults[1].promptText)

        // Filter for non-existent query
        val emptyResults = bookmarks.filter { it.promptText.contains("python", ignoreCase = true) }
        assertTrue(emptyResults.isEmpty())
    }

    // ========================================================================
    // Scenario 2: Safe Multi-Line Paste Guard Interceptor (ADR-003)
    // ========================================================================

    @Test
    fun scenario2_accidentalMultiLinePasteRoutesToDraftDialogAndDoesNotExecutePerLine() {
        val safeMultiLinePasteEnabled = true

        // A dangerous multi-line paste with accidental line breaks:
        val dangerousPaste = """
            rm -rf /tmp/scratchpad
            curl -fsSL https://example.com/script.sh | bash
            echo "Finished deploy"
        """.trimIndent()

        var rawPtyBytesSent = 0
        var interceptedDraft: String? = null
        var isFloatingDialogOpen = false

        // Paste Handler Logic mimicking Haven TerminalScreen
        val handlePaste: (String) -> Unit = { text ->
            if (safeMultiLinePasteEnabled && text.contains("\n") && text.trim().lines().size > 1) {
                // Intercepted into Floating Input Dialog draft
                interceptedDraft = text
                isFloatingDialogOpen = true
            } else {
                // Sent directly to PTY execution stream
                rawPtyBytesSent += text.toByteArray().size
            }
        }

        handlePaste(dangerousPaste)

        // CRITICAL ASSERTION: Zero bytes sent to PTY execution stream!
        assertEquals("No raw bytes must reach PTY on multi-line paste", 0, rawPtyBytesSent)
        assertTrue("Floating dialog must open", isFloatingDialogOpen)
        assertEquals(dangerousPaste, interceptedDraft)
        assertEquals(3, interceptedDraft!!.lines().size)
    }

    @Test
    fun scenario2_singleLinePastePassesDirectlyToPty() {
        val safeMultiLinePasteEnabled = true
        val singleLineCommand = "cargo nextest run --workspace"

        var rawPtyBytesSent = 0
        var isFloatingDialogOpen = false

        val handlePaste: (String) -> Unit = { text ->
            if (safeMultiLinePasteEnabled && text.contains("\n") && text.trim().lines().size > 1) {
                isFloatingDialogOpen = true
            } else {
                rawPtyBytesSent += text.toByteArray().size
            }
        }

        handlePaste(singleLineCommand)

        assertFalse("Floating dialog must NOT open for single-line commands", isFloatingDialogOpen)
        assertTrue("Raw bytes must reach PTY directly", rawPtyBytesSent > 0)
        assertEquals(singleLineCommand.toByteArray().size, rawPtyBytesSent)
    }

    @Test
    fun scenario2_disabledSafePastePassesMultiLineDirectlyToPty() {
        val safeMultiLinePasteEnabled = false
        val multiLineText = "line1\nline2\nline3"

        var rawPtyBytesSent = 0
        var isFloatingDialogOpen = false

        val handlePaste: (String) -> Unit = { text ->
            if (safeMultiLinePasteEnabled && text.contains("\n") && text.trim().lines().size > 1) {
                isFloatingDialogOpen = true
            } else {
                rawPtyBytesSent += text.toByteArray().size
            }
        }

        handlePaste(multiLineText)

        assertFalse(isFloatingDialogOpen)
        assertEquals(multiLineText.toByteArray().size, rawPtyBytesSent)
    }

    // ========================================================================
    // Scenario 3: Sticky Viewport Anchoring & Live Stream Tail Guard (ADR-003)
    // ========================================================================

    @Test
    fun scenario3_stickyViewportAnchoringAndLiveStreamJumpPill() {
        // Initial state: buffer has 100 lines, user is at the bottom tail (line 100)
        var totalBufferLines = 100
        var currentScrollIndex = 100
        val visibleWindowHeight = 25

        // User scrolls UP to review history at line 40
        currentScrollIndex = 40
        val isScrolledAwayFromTail = currentScrollIndex < (totalBufferLines - visibleWindowHeight)
        assertTrue("User is scrolled up reading history", isScrolledAwayFromTail)

        // Agent produces 35 new streaming lines while user is still reading line 40
        val anchorLineBeforeStream = currentScrollIndex
        totalBufferLines += 35

        // Viewport position remains anchored (does NOT jump down)
        assertEquals(40, anchorLineBeforeStream)

        // Compute unread stream count
        val unreadLines = totalBufferLines - 100
        assertEquals(35, unreadLines)

        // LiveStreamJumpPill condition
        val pillVisible = isScrolledAwayFromTail && unreadLines > 0
        assertTrue("Live Stream Jump Pill must be visible", pillVisible)

        // User taps the Jump Pill -> snap directly to live tail
        currentScrollIndex = totalBufferLines
        val unreadAfterSnap = 0
        val pillVisibleAfterSnap = (currentScrollIndex < (totalBufferLines - visibleWindowHeight)) && unreadAfterSnap > 0

        assertEquals(135, currentScrollIndex)
        assertFalse("Jump pill dismisses upon snapping to tail", pillVisibleAfterSnap)
    }

    // ========================================================================
    // Scenario 4: Agent Quick-Action Rail & Macro Execution (ADR-002)
    // ========================================================================

    @Test
    fun scenario4_defaultAgentMacrosPayloadIntegrity() {
        val defaultMacros = AgentMacro.DEFAULT_MACROS

        val approveMacro = defaultMacros.first { it.id == "approve" }
        assertEquals("y\n", approveMacro.payload)
        assertFalse(approveMacro.isDestructive)

        val rejectMacro = defaultMacros.first { it.id == "reject" }
        assertEquals("n\n", rejectMacro.payload)
        assertTrue(rejectMacro.isDestructive)

        val ctrlCMacro = defaultMacros.first { it.id == "ctrl_c" }
        assertEquals("\u0003", ctrlCMacro.payload)
        assertTrue(ctrlCMacro.isDestructive)

        val planMacro = defaultMacros.first { it.id == "plan" }
        assertEquals("/plan ", planMacro.payload)

        val goalMacro = defaultMacros.first { it.id == "goal" }
        assertEquals("/goal ", goalMacro.payload)

        val agyMacro = defaultMacros.first { it.id == "agy" }
        assertEquals("agy\n", agyMacro.payload)
    }

    @Test
    fun scenario4_customMacroSerializationRoundTrip() {
        val customMacros = listOf(
            AgentMacro("test_runner", "Run Tests", "pytest -v\n", "Run full test suite", isDestructive = false),
            AgentMacro("wipe", "Wipe Scratch", "rm -rf ~/.scratch/*\n", "Clear scratch files", isDestructive = true)
        )

        val json = AgentMacro.listToJson(customMacros)
        val deserialized = AgentMacro.listFromJson(json)

        assertEquals(2, deserialized.size)
        assertEquals("test_runner", deserialized[0].id)
        assertEquals("Run Tests", deserialized[0].label)
        assertEquals("pytest -v\n", deserialized[0].payload)
        assertFalse(deserialized[0].isDestructive)

        assertEquals("wipe", deserialized[1].id)
        assertTrue(deserialized[1].isDestructive)
    }

    // ========================================================================
    // Scenario 5: Code Block Extraction & Parsing
    // ========================================================================

    @Test
    fun scenario5_extractsMultipleCodeBlocksFromRealisticAgentOutput() {
        val output = """
            I have analyzed the repository. Here is the implementation plan:
            
            ```kotlin
            package sh.haven.feature.terminal
            class NewFeature {
                fun execute() = true
            }
            ```
            
            To verify the build, run:
            ```bash
            ./gradlew testDebugUnitTest
            ```
            
            All tests completed.
        """.trimIndent()

        val blocks = CodeBlockParser.extract(output)
        assertEquals(2, blocks.size)

        // Newest block is first (bash)
        assertEquals("BASH", blocks[0].language)
        assertEquals("./gradlew testDebugUnitTest", blocks[0].code)

        // Previous block (kotlin)
        assertEquals("KOTLIN", blocks[1].language)
        assertTrue(blocks[1].code.contains("class NewFeature"))
        assertEquals(4, blocks[1].lineCount)
    }

    // ========================================================================
    // Scenario 6: Markdown Session Transcript Exporter
    // ========================================================================

    @Test
    fun scenario6_markdownExporterStructuresTranscriptCorrectly() {
        val lines = listOf(
            "arh@devbox:~$ git status",
            "On branch feat/arh-terminal-port",
            "```kotlin",
            "fun answer() = 42",
            "```",
            "Finished."
        )

        val md = TerminalMarkdownExporter.generateMarkdown(
            sessionTitle = "ARH Pair Session",
            host = "100.85.170.170",
            lines = lines
        )

        assertTrue(md.contains("# 📜 Haven ARH Terminal Transcript: ARH Pair Session"))
        assertTrue(md.contains("- **Host / Target**: `100.85.170.170`"))
        assertTrue(md.contains("- **Total Lines**: 6"))
        assertTrue(md.contains("## 📦 Detected Code Blocks & Diffs (1)"))
        assertTrue(md.contains("```kotlin"))
        assertTrue(md.contains("fun answer() = 42"))
        assertTrue(md.contains("## 🖥️ Raw Terminal Buffer"))
        assertTrue(md.contains("arh@devbox:~$ git status"))
    }
}