package sh.haven.feature.terminal.arh

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import sh.haven.feature.terminal.R
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object TerminalMarkdownExporter {

    fun generateMarkdown(
        sessionTitle: String,
        host: String? = null,
        lines: List<String>
    ): String = buildString {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val now = dateFormat.format(Date())

        appendLine("# 📜 Haven ARH Terminal Transcript: $sessionTitle")
        appendLine()
        if (!host.isNullOrBlank()) {
            appendLine("- **Host / Target**: `$host`")
        }
        appendLine("- **Timestamp**: `$now`")
        appendLine("- **Total Lines**: ${lines.size}")
        appendLine()
        appendLine("---")
        appendLine()

        val fullText = lines.joinToString("\n").trim()
        val extractedBlocks = CodeBlockParser.extract(fullText)

        if (extractedBlocks.isNotEmpty()) {
            appendLine("## 📦 Detected Code Blocks & Diffs (${extractedBlocks.size})")
            appendLine()
            extractedBlocks.forEachIndexed { index, block ->
                appendLine("### Snippet ${index + 1} (${block.language}, ${block.lineCount} lines)")
                appendLine("```${block.language.lowercase()}")
                appendLine(block.code)
                appendLine("```")
                appendLine()
            }
            appendLine("---")
            appendLine()
        }

        appendLine("## 🖥️ Raw Terminal Buffer")
        appendLine()
        appendLine("```terminal")
        appendLine(fullText)
        appendLine("```")
    }

    fun shareMarkdown(
        context: Context,
        sessionTitle: String,
        markdown: String
    ) {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, markdown)
            putExtra(Intent.EXTRA_TITLE, "$sessionTitle-transcript.md")
            type = "text/markdown"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Share Terminal Transcript")
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(shareIntent)
    }

    fun saveToDownloads(
        context: Context,
        sessionTitle: String,
        markdown: String
    ): Uri? {
        val sanitizedTitle = sessionTitle.replace(Regex("[^a-zA-Z0-9_-]"), "_")
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val fileName = "${sanitizedTitle}_transcript_$timestamp.md"

        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "text/markdown")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }
                val resolver = context.contentResolver
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                if (uri != null) {
                    resolver.openOutputStream(uri)?.use { stream ->
                        stream.write(markdown.toByteArray(Charsets.UTF_8))
                    }
                    Toast.makeText(context, context.getString(R.string.terminal_markdown_saved_to_downloads, fileName), Toast.LENGTH_LONG).show()
                }
                uri
            } else {
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val targetFile = File(downloadsDir, fileName)
                targetFile.writeText(markdown, Charsets.UTF_8)
                Toast.makeText(context, context.getString(R.string.terminal_markdown_saved_to_downloads, fileName), Toast.LENGTH_LONG).show()
                Uri.fromFile(targetFile)
            }
        } catch (e: Exception) {
            Toast.makeText(context, context.getString(R.string.terminal_markdown_save_failed, e.message ?: ""), Toast.LENGTH_LONG).show()
            null
        }
    }
}
