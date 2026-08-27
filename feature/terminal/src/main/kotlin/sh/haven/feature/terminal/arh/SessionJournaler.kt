package sh.haven.feature.terminal.arh

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.io.RandomAccessFile
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
 *
 * Writes for each session are executed through a single-threaded dispatcher
 * (Dispatchers.IO.limitedParallelism(1)) to guarantee strict sequential FIFO order.
 */
object SessionJournaler {
    private const val TAG = "SessionJournaler"
    private const val FLUSH_THRESHOLD_CHARS = 10_000
    private const val DEBOUNCE_FLUSH_MS = 3_000L
    private const val DEFAULT_PREVIEW_MAX_BYTES = 256 * 1024 // 256 KB preview limit

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val bufferMap = ConcurrentHashMap<String, StringBuilder>()
    private val debounceJobs = ConcurrentHashMap<String, Job>()
    private val sessionDispatchers = ConcurrentHashMap<String, CoroutineDispatcher>()

    private fun getSessionDispatcher(sessionId: String): CoroutineDispatcher {
        return sessionDispatchers.computeIfAbsent(sessionId) {
            Dispatchers.IO.limitedParallelism(1)
        }
    }

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
        debounceJobs[sessionId] = scope.launch(getSessionDispatcher(sessionId)) {
            delay(DEBOUNCE_FLUSH_MS)
            val textToFlush: String
            synchronized(buffer) {
                textToFlush = buffer.toString()
                buffer.clear()
            }
            if (textToFlush.isNotEmpty()) {
                writeChunkDirectly(context, sessionId, sessionTitle, host, textToFlush)
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
        scope.launch(getSessionDispatcher(sessionId)) {
            writeChunkDirectly(context, sessionId, sessionTitle, host, content)
        }
    }

    private fun writeChunkDirectly(
        context: Context,
        sessionId: String,
        sessionTitle: String,
        host: String?,
        content: String,
    ) {
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

    fun getJournalFile(context: Context, sessionId: String): File? {
        val file = File(File(context.filesDir, "transcripts"), "transcript_${sanitize(sessionId)}.md")
        return if (file.exists()) file else null
    }

    /**
     * Reads the session journal with an upper bound to prevent OutOfMemoryError on multi-day sessions.
     * For large files, reads the tail up to [maxBytes] bytes.
     */
    fun readJournal(context: Context, sessionId: String, maxBytes: Int = DEFAULT_PREVIEW_MAX_BYTES): String {
        val file = getJournalFile(context, sessionId) ?: return ""
        return try {
            val length = file.length()
            val raw = if (length <= maxBytes) {
                file.readText(Charsets.UTF_8)
            } else {
                // Read the tail of the large file
                RandomAccessFile(file, "r").use { raf ->
                    val seekPos = length - maxBytes
                    raf.seek(seekPos)
                    val buffer = ByteArray(maxBytes)
                    val bytesRead = raf.read(buffer)
                    val content = String(buffer, 0, bytesRead, Charsets.UTF_8)
                    // Prepend note if truncated
                    "# 📜 [Session Journal Preview - Truncated; showing last ${(bytesRead / 1024)} KB]\n\n```terminal\n$content"
                }
            }
            if (raw.endsWith("```terminal\n")) raw else "$raw\n```"
        } catch (_: Exception) {
            ""
        }
    }

    /**
     * Streams the entire session journal lines sequentially without loading the whole file into RAM.
     */
    fun forEachJournalLine(context: Context, sessionId: String, action: (String) -> Unit) {
        val file = getJournalFile(context, sessionId) ?: return
        try {
            BufferedReader(InputStreamReader(FileInputStream(file), Charsets.UTF_8)).useLines { lines ->
                lines.forEach(action)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error streaming journal lines: ${e.message}", e)
        }
    }

    private fun sanitize(s: String): String = s.replace(Regex("[^a-zA-Z0-9_-]"), "_")
}
