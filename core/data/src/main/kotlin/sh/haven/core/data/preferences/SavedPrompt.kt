package sh.haven.core.data.preferences

import org.json.JSONArray
import org.json.JSONObject

/**
 * A reusable prompt template in the user's Prompt Book.
 *
 * @property id Unique identifier.
 * @property title Display title (e.g. "Review Code", "Unit Test Generator").
 * @property content Full prompt body or command text to inject or execute.
 * @property category Optional grouping tag (e.g. "Dev", "Agent", "Git", "Debug").
 */
data class SavedPrompt(
    val id: String,
    val title: String,
    val content: String,
    val category: String = "General",
) {
    fun toJsonObject(): JSONObject = JSONObject().apply {
        put("id", id)
        put("title", title)
        put("content", content)
        put("category", category)
    }

    companion object {
        fun fromJsonObject(json: JSONObject): SavedPrompt? {
            val id = json.optString("id", "")
            val title = json.optString("title", "")
            val content = json.optString("content", "")
            if (id.isEmpty() || title.isEmpty() || content.isEmpty()) return null
            return SavedPrompt(
                id = id,
                title = title,
                content = content,
                category = json.optString("category", "General")
            )
        }

        val DEFAULT_PROMPTS = listOf(
            SavedPrompt(
                id = "p1",
                title = "Review Changes & Suggest Fixes",
                content = "Please review the current git diff and changes, identify potential bugs or edge cases, and propose concrete fixes.",
                category = "Code Review"
            ),
            SavedPrompt(
                id = "p2",
                title = "Write Unit Tests",
                content = "Generate comprehensive unit tests for the selected module covering positive cases, error cases, and edge cases.",
                category = "Testing"
            ),
            SavedPrompt(
                id = "p3",
                title = "Diagnose Stacktrace / Error",
                content = "Analyze this failure log/stacktrace, identify the root cause, and explain the step-by-step fix with file links.",
                category = "Debug"
            ),
            SavedPrompt(
                id = "p4",
                title = "Git Status & Worktree Clean",
                content = "git status -s && git diff --stat",
                category = "Git"
            ),
            SavedPrompt(
                id = "p5",
                title = "Launch ARH Agent Fleet",
                content = "arh-agent --fleet --deliberate",
                category = "Agent"
            )
        )

        fun listToJson(prompts: List<SavedPrompt>): String {
            val arr = JSONArray()
            prompts.forEach { arr.put(it.toJsonObject()) }
            return arr.toString(2)
        }

        fun listFromJson(json: String): List<SavedPrompt> {
            if (json.isBlank()) return DEFAULT_PROMPTS
            return try {
                val arr = JSONArray(json)
                val list = mutableListOf<SavedPrompt>()
                for (i in 0 until arr.length()) {
                    val obj = arr.optJSONObject(i) ?: continue
                    fromJsonObject(obj)?.let { list.add(it) }
                }
                if (list.isEmpty()) DEFAULT_PROMPTS else list
            } catch (_: Exception) {
                DEFAULT_PROMPTS
            }
        }
    }
}
