package sh.haven.core.data.preferences

import org.json.JSONArray
import org.json.JSONObject

/**
 * A user-configurable agent macro / quick-action button.
 *
 * @property id Unique identifier.
 * @property label Display title on the chip/button (e.g. "Approve (y)").
 * @property payload Exact character / byte sequence sent to the terminal (e.g. "y\n", "\u0003").
 * @property description Human-readable tooltip or explanation in the settings manager.
 * @property isDestructive Renders the button with an error/warning tint (e.g. Ctrl+C or Reject).
 */
data class AgentMacro(
    val id: String,
    val label: String,
    val payload: String,
    val description: String = "",
    val isDestructive: Boolean = false,
) {
    fun toJsonObject(): JSONObject = JSONObject().apply {
        put("id", id)
        put("label", label)
        put("payload", payload)
        put("description", description)
        put("isDestructive", isDestructive)
    }

    companion object {
        fun fromJsonObject(json: JSONObject): AgentMacro? {
            val id = json.optString("id", "")
            val label = json.optString("label", "")
            val payload = json.optString("payload", "")
            if (id.isEmpty() || label.isEmpty() || payload.isEmpty()) return null
            return AgentMacro(
                id = id,
                label = label,
                payload = payload,
                description = json.optString("description", ""),
                isDestructive = json.optBoolean("isDestructive", false)
            )
        }

        val DEFAULT_MACROS = listOf(
            AgentMacro("approve", "Approve (y)", "y\n", "Send approval confirmation", isDestructive = false),
            AgentMacro("reject", "Reject (n)", "n\n", "Send rejection / cancellation", isDestructive = true),
            AgentMacro("ctrl_c", "^C", "\u0003", "Ctrl+C interrupt signal", isDestructive = true),
            AgentMacro("plan", "/plan", "/plan ", "Agent plan slash command", isDestructive = false),
            AgentMacro("goal", "/goal", "/goal ", "Agent goal slash command", isDestructive = false),
            AgentMacro("agy", "agy", "agy\n", "Launch Antigravity agent CLI", isDestructive = false),
            AgentMacro("arh_agent", "arh-agent", "arh-agent\n", "Launch ARH local agent", isDestructive = false),
        )

        fun listToJson(macros: List<AgentMacro>): String {
            val arr = JSONArray()
            macros.forEach { arr.put(it.toJsonObject()) }
            return arr.toString(2)
        }

        fun listFromJson(json: String): List<AgentMacro> {
            if (json.isBlank()) return DEFAULT_MACROS
            return try {
                val arr = JSONArray(json)
                val list = mutableListOf<AgentMacro>()
                for (i in 0 until arr.length()) {
                    val obj = arr.optJSONObject(i) ?: continue
                    fromJsonObject(obj)?.let { list.add(it) }
                }
                if (list.isEmpty()) DEFAULT_MACROS else list
            } catch (_: Exception) {
                DEFAULT_MACROS
            }
        }
    }
}
