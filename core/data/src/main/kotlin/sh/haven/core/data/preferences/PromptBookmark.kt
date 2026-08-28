package sh.haven.core.data.preferences

import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * Represents a pinned prompt or user landmark in the terminal session timeline.
 * Inspired by Stream Chat PinnedMessageList and Element X PR #3392.
 */
data class PromptBookmark(
    val id: String = UUID.randomUUID().toString(),
    val sessionId: String = "",
    val lineIndex: Int,
    val promptText: String,
    val timestampMs: Long = System.currentTimeMillis()
) {
    val formattedTime: String
        get() {
            val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            return sdf.format(Date(timestampMs))
        }

    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("sessionId", sessionId)
        put("lineIndex", lineIndex)
        put("promptText", promptText)
        put("timestampMs", timestampMs)
    }

    companion object {
        fun fromJson(obj: JSONObject): PromptBookmark = PromptBookmark(
            id = obj.optString("id", UUID.randomUUID().toString()),
            sessionId = obj.optString("sessionId", ""),
            lineIndex = obj.optInt("lineIndex", 0),
            promptText = obj.optString("promptText", ""),
            timestampMs = obj.optLong("timestampMs", System.currentTimeMillis())
        )

        fun listToJson(list: List<PromptBookmark>): String {
            val array = JSONArray()
            list.forEach { array.put(it.toJson()) }
            return array.toString()
        }

        fun listFromJson(jsonStr: String): List<PromptBookmark> {
            if (jsonStr.isBlank()) return emptyList()
            return try {
                val array = JSONArray(jsonStr)
                val list = mutableListOf<PromptBookmark>()
                for (i in 0 until array.length()) {
                    list.add(fromJson(array.getJSONObject(i)))
                }
                list
            } catch (_: Exception) {
                emptyList()
            }
        }
    }
}
