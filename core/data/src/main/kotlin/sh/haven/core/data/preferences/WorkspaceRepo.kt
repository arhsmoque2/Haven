package sh.haven.core.data.preferences

import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

/**
 * Represents a configured repository or project workspace path.
 */
data class WorkspaceRepo(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val localPath: String,
    val gitUrl: String = "",
    val branch: String = "main",
    val description: String = "",
    val lastUsedMs: Long = System.currentTimeMillis()
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("name", name)
        put("localPath", localPath)
        put("gitUrl", gitUrl)
        put("branch", branch)
        put("description", description)
        put("lastUsedMs", lastUsedMs)
    }

    companion object {
        fun fromJson(obj: JSONObject): WorkspaceRepo = WorkspaceRepo(
            id = obj.optString("id", UUID.randomUUID().toString()),
            name = obj.optString("name", "Project"),
            localPath = obj.optString("localPath", "~/"),
            gitUrl = obj.optString("gitUrl", ""),
            branch = obj.optString("branch", "main"),
            description = obj.optString("description", ""),
            lastUsedMs = obj.optLong("lastUsedMs", System.currentTimeMillis())
        )

        fun listToJson(list: List<WorkspaceRepo>): String {
            val array = JSONArray()
            list.forEach { array.put(it.toJson()) }
            return array.toString()
        }

        fun listFromJson(jsonStr: String): List<WorkspaceRepo> {
            if (jsonStr.isBlank()) return DEFAULT_REPOS
            return try {
                val array = JSONArray(jsonStr)
                val list = mutableListOf<WorkspaceRepo>()
                for (i in 0 until array.length()) {
                    list.add(fromJson(array.getJSONObject(i)))
                }
                if (list.isEmpty()) DEFAULT_REPOS else list
            } catch (_: Exception) {
                DEFAULT_REPOS
            }
        }

        val DEFAULT_REPOS = listOf(
            WorkspaceRepo(
                id = "repo-haven-os",
                name = "Haven (ARH Fork)",
                localPath = "~/repos/Haven",
                gitUrl = "https://github.com/arhsmoque2/Haven.git",
                branch = "feat/arh-terminal-port",
                description = "Primary Android Terminal & Agent Host"
            ),
            WorkspaceRepo(
                id = "repo-arh-agent-os",
                name = "ARH-AGENT-OS",
                localPath = "D:/_ARH-AGENT-OS",
                gitUrl = "https://github.com/arhsmoque2/ARH-Poket-AI.git",
                branch = "main",
                description = "Core Agent OS Canonical Workspace"
            ),
            WorkspaceRepo(
                id = "repo-scratchpad",
                name = "Lab Scratchpad",
                localPath = "~/.arh-scratch",
                gitUrl = "",
                branch = "main",
                description = "Ephemeral Dev Workspace"
            )
        )
    }
}
