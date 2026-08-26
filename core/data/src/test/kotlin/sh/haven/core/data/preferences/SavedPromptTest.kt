package sh.haven.core.data.preferences

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SavedPromptTest {

    @Test
    fun `default prompts list is populated`() {
        val defaults = SavedPrompt.DEFAULT_PROMPTS
        assertTrue(defaults.isNotEmpty())
        assertTrue(defaults.any { it.title.contains("Review") })
        assertTrue(defaults.any { it.title.contains("Unit Tests") })
    }

    @Test
    fun `serialization roundtrip preserves all fields`() {
        val customList = listOf(
            SavedPrompt("p10", "Custom Prompt", "Do something awesome", "CustomTag")
        )
        val json = SavedPrompt.listToJson(customList)
        val parsed = SavedPrompt.listFromJson(json)

        assertEquals(1, parsed.size)
        assertEquals("p10", parsed[0].id)
        assertEquals("Custom Prompt", parsed[0].title)
        assertEquals("Do something awesome", parsed[0].content)
        assertEquals("CustomTag", parsed[0].category)
    }

    @Test
    fun `empty json returns defaults`() {
        val parsed = SavedPrompt.listFromJson("")
        assertEquals(SavedPrompt.DEFAULT_PROMPTS.size, parsed.size)
    }
}
