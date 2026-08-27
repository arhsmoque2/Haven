package sh.haven.feature.terminal.arh

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

/**
 * Continuous Disk-Streaming Session Journaler.
 *
 * Streams raw terminal PTY stdout into persistent Markdown files on disk in
 * 10,000-character chunks or on a 3-second debounce timer. This enables
 * retaining 100,000+ lines of multi-day session history while keeping the
 * active in-memory rendering viewport under 5 MB RAM.
 */
object SessionJournaler {
    private const val TAG = "SessionJournaler"
    private const val FLUSH_THRESHOLD_CHARS = 10_000
    private const val DEBOUNCE_FLUSH_MS = 3_000L

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val bufferMap = ConcurrentHashMap<String, StringBuilder>()
    private val debounceJobs = ConcurrentHashMap<String, Job>()
    private val fileLocks = ConcurrentHashMap<String, Mutex>()

    fun append(context: Context, sessionId: String, sessionTitle: String, host: String? = null, chunk: String) {
        if (chunk.isEmpty()) return
        val buffer = bufferMap.computeIfAbsent(sessionId) { StringBuilder() }

        synchronized(buffer) {
            buffer.append(chunk)
            if (buffer.length >= FLUSH_THRESHOLD_CHARS) {
                val textToFlush = buffer.toString()
                buffer.clear()
                debounceJobs[sessionId]?.cancel()
                debounceJobs.remove(sessionId)
                flushToDisk(context, sessionId, sessionTitle, host, textToFlush)
                return
            }
        }

        // Debounce timer for sub-threshold chunks
        debounceJobs[sessionId]?.cancel()
        debounceJobs[sessionId] = scope.launch {
            delay(DEBOUNCE_FLUSH_MS)
            val textToFlush: String
            synchronized(buffer) {
                textToFlush = buffer.toString()
                buffer.clear()
            }
            if (textToFlush.isNotEmpty()) {
                flushToDisk(context, sessionId, sessionTitle, host, textToFlush)
            }
            debounceJobs.remove(sessionId)
        }
    }

    fun flushSync(context: Context, sessionId: String, sessionTitle: String, host: String? = null) {
        val buffer = bufferMap[sessionId] ?: return
        val textToFlush: String
        synchronized(buffer) {
            textToFlush = buffer.toString()
            buffer.clear()
        }
        if (textToFlush.isNotEmpty()) {
            flushToDisk(context, sessionId, sessionTitle, host, textToFlush)
        }
    }

    private fun flushToDisk(
        context: Context,
        sessionId: String,
        sessionTitle: String,
        host: String?,
        content: String,
    ) {
        scope.launch {
            val mutex = fileLocks.computeIfAbsent(sessionId) { Mutex() }
            mutex.withLock {
                try {
                    val dir = File(context.filesDir, "transcripts").apply { mkdirs() }
                    val file = File(dir, "transcript_${sanitize(sessionId)}.md")
                    val isNew = !file.exists() || file.length() == 0L

                    FileOutputStream(file, true).bufferedWriter().use { writer ->
                        if (isNew) {
                            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                            val now = dateFormat.format(Date())
                            writer.write("# 📜 Haven Terminal Session Journal: $sessionTitle\n\n")
                            if (!host.isNullOrBlank()) writer.write("- **Host / Target**: `$host`\n")
                            writer.write("- **Session ID**: `$sessionId`\n")
                            writer.write("- **Started At**: `$now`\n\n")
                            writer.write("---\n\n")
                            writer.write("```terminal\n")
                        }
                        writer.write(content)
                        writer.flush()
                    }
                    Log.d(TAG, "Flushed ${content.length} chars to ${file.name}")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to flush session journal: ${e.message}", e)
                }
            }
        }
    }

    fun getJournalFile(context: Context, sessionId: String): File? {
        val file = File(File(context.filesDir, "transcripts"), "transcript_${sanitize(sessionId)}.md")
        return if (file.exists()) file else null
    }

    fun readJournal(context: Context, sessionId: String): String {
        val file = getJournalFile(context, sessionId) ?: return ""
        return try {
            val raw = file.readText(Charsets.UTF_8)
            if (raw.endsWith("```terminal\n")) raw else "$raw\n```"
        } catch (_: Exception) {
            ""
        }
    }

    private fun sanitize(s: String): String = s.replace(Regex("[^a-zA-Z0-9_-]"), "_")
}
