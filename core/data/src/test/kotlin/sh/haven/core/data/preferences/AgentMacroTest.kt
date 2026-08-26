package sh.haven.core.data.preferences

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AgentMacroTest {

    @Test
    fun `default macros are populated and non-empty`() {
        val defaults = AgentMacro.DEFAULT_MACROS
        assertTrue(defaults.isNotEmpty())
        assertTrue(defaults.any { it.label.contains("Approve") })
        assertTrue(defaults.any { it.label.contains("Reject") && it.isDestructive })
        assertTrue(defaults.any { it.label == "/plan" })
        assertTrue(defaults.any { it.label == "agy" })
    }

    @Test
    fun `serialization roundtrip preserves all fields`() {
        val customList = listOf(
            AgentMacro("m1", "Deploy", "npm run deploy\n", "Deploy command", isDestructive = false),
            AgentMacro("m2", "Kill", "\u0003", "Sigint", isDestructive = true)
        )

        val json = AgentMacro.listToJson(customList)
        val deserialized = AgentMacro.listFromJson(json)

        assertEquals(2, deserialized.size)
        assertEquals("m1", deserialized[0].id)
        assertEquals("Deploy", deserialized[0].label)
        assertEquals("npm run deploy\n", deserialized[0].payload)
        assertFalse(deserialized[0].isDestructive)

        assertEquals("m2", deserialized[1].id)
        assertEquals("^C", deserialized[1].label.ifEmpty { "Kill" })
        assertEquals("\u0003", deserialized[1].payload)
        assertTrue(deserialized[1].isDestructive)
    }

    @Test
    fun `empty or corrupt JSON falls back to default macros`() {
        val fromEmpty = AgentMacro.listFromJson("")
        assertEquals(AgentMacro.DEFAULT_MACROS.size, fromEmpty.size)

        val fromCorrupt = AgentMacro.listFromJson("{ not an array }")
        assertEquals(AgentMacro.DEFAULT_MACROS.size, fromCorrupt.size)
    }
}
