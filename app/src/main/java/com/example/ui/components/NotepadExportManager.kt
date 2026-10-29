package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class NotepadStats(
    val wordCount: Int,
    val charCount: Int,
    val charCountNoSpaces: Int,
    val paragraphCount: Int,
    val readingTimeMinutes: Int
)

object NotepadExportManager {

    fun calculateStats(text: String): NotepadStats {
        if (text.isBlank()) {
            return NotepadStats(0, 0, 0, 0, 0)
        }
        val words = text.trim().split("\\s+".toRegex()).filter { it.isNotBlank() }
        val wordCount = words.size
        val charCount = text.length
        val charCountNoSpaces = text.count { !it.isWhitespace() }
        val paragraphs = text.split("\n").filter { it.isNotBlank() }.size.coerceAtLeast(1)
        val readingTime = (wordCount / 200).coerceAtLeast(1)
        return NotepadStats(
            wordCount = wordCount,
            charCount = charCount,
            charCountNoSpaces = charCountNoSpaces,
            paragraphCount = paragraphs,
            readingTimeMinutes = if (wordCount < 100) 1 else readingTime
        )
    }

    fun copyToClipboard(context: Context, text: String, label: String = "Athena Document") {
        try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText(label, text)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(context, "Copied document (${text.length} chars) to clipboard", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Could not copy: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    fun getClipboardText(context: Context): String? {
        return try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = clipboard.primaryClip
            if (clip != null && clip.itemCount > 0) {
                clip.getItemAt(0).text?.toString()
            } else null
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Export as formatted Plain Text (.txt)
     */
    fun exportPlainText(context: Context, title: String, text: String) {
        try {
            val exportsDir = File(context.cacheDir, "exports").apply { mkdirs() }
            val cleanTitle = sanitizeFilename(title)
            val file = File(exportsDir, "${cleanTitle}_${System.currentTimeMillis()}.txt")
            val formattedContent = buildString {
                appendLine("==================================================")
                appendLine(title.ifBlank { "Untitled Document" })
                appendLine("Athena Workspace • ${SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())}")
                val stats = calculateStats(text)
                appendLine("Words: ${stats.wordCount} | Characters: ${stats.charCount}")
                appendLine("==================================================")
                appendLine()
                append(text)
            }
            file.writeText(formattedContent, Charsets.UTF_8)

            shareFile(context, file, "text/plain", "Share Plain Text Document")
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to export text: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Export as clean HTML document (.html)
     */
    fun exportHtml(context: Context, title: String, text: String) {
        try {
            val exportsDir = File(context.cacheDir, "exports").apply { mkdirs() }
            val cleanTitle = sanitizeFilename(title)
            val file = File(exportsDir, "${cleanTitle}_${System.currentTimeMillis()}.html")
            val now = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
            val stats = calculateStats(text)

            val safeTitle = escapeHtml(title.ifBlank { "Athena Document" })
            val safeBody = escapeHtml(text)

            val htmlContent = """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>$safeTitle</title>
                    <style>
                        :root {
                            --bg: #0b1320;
                            --card: #111e31;
                            --accent: #00e5ff;
                            --text: #f0f6fc;
                            --muted: #94a3b8;
                            --border: #1e3a5f;
                        }
                        body {
                            background-color: var(--bg);
                            color: var(--text);
                            font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
                            line-height: 1.7;
                            margin: 0;
                            padding: 24px;
                            display: flex;
                            justify-content: center;
                        }
                        .container {
                            max-width: 820px;
                            width: 100%;
                            background: var(--card);
                            border: 1px solid var(--border);
                            border-radius: 16px;
                            padding: 36px 40px;
                            box-shadow: 0 12px 32px rgba(0, 0, 0, 0.4);
                        }
                        .header {
                            border-bottom: 2px solid var(--border);
                            padding-bottom: 18px;
                            margin-bottom: 24px;
                        }
                        h1 {
                            margin: 0 0 10px 0;
                            font-size: 26px;
                            color: var(--accent);
                            letter-spacing: -0.5px;
                        }
                        .meta-bar {
                            display: flex;
                            flex-wrap: wrap;
                            gap: 16px;
                            font-size: 13px;
                            color: var(--muted);
                        }
                        .badge {
                            background: rgba(0, 229, 255, 0.12);
                            color: var(--accent);
                            padding: 3px 8px;
                            border-radius: 6px;
                            font-weight: 600;
                        }
                        .content {
                            font-size: 16px;
                            white-space: pre-wrap;
                            word-break: break-word;
                            color: #e2e8f0;
                        }
                        .footer {
                            margin-top: 36px;
                            padding-top: 18px;
                            border-top: 1px solid var(--border);
                            font-size: 12px;
                            color: var(--muted);
                            text-align: right;
                        }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>$safeTitle</h1>
                            <div class="meta-bar">
                                <span>📅 $now</span>
                                <span class="badge">Words: ${stats.wordCount}</span>
                                <span class="badge">Characters: ${stats.charCount}</span>
                                <span>⏱ ~${stats.readingTimeMinutes} min read</span>
                            </div>
                        </div>
                        <div class="content">$safeBody</div>
                        <div class="footer">
                            Athena Workspace • Pro Notepad Studio Document
                        </div>
                    </div>
                </body>
                </html>
            """.trimIndent()

            file.writeText(htmlContent, Charsets.UTF_8)
            shareFile(context, file, "text/html", "Share HTML Document")
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to export HTML: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Export as clean, paginated PDF (.pdf) using android.graphics.pdf.PdfDocument
     */
    fun exportPdf(context: Context, title: String, text: String) {
        var pdfDocument: PdfDocument? = null
        try {
            val exportsDir = File(context.cacheDir, "exports").apply { mkdirs() }
            val cleanTitle = sanitizeFilename(title)
            val file = File(exportsDir, "${cleanTitle}_${System.currentTimeMillis()}.pdf")

            pdfDocument = PdfDocument()

            // Page size: Standard US Letter (612 x 792 points at 72 dpi)
            val pageWidth = 612
            val pageHeight = 792
            val margin = 50
            val printableWidth = pageWidth - (margin * 2)

            val textPaint = TextPaint().apply {
                color = Color.rgb(30, 41, 59)
                textSize = 12f
                isAntiAlias = true
            }

            val titlePaint = Paint().apply {
                color = Color.rgb(2, 132, 199)
                textSize = 18f
                isFakeBoldText = true
                isAntiAlias = true
            }

            val metaPaint = Paint().apply {
                color = Color.rgb(100, 116, 139)
                textSize = 9.5f
                isAntiAlias = true
            }

            val dividerPaint = Paint().apply {
                color = Color.rgb(226, 232, 240)
                strokeWidth = 1.5f
            }

            val now = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
            val stats = calculateStats(text)

            // Layout the body text with StaticLayout
            val bodyStaticLayout = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                StaticLayout.Builder.obtain(text, 0, text.length, textPaint, printableWidth)
                    .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                    .setLineSpacing(3f, 1.15f)
                    .setIncludePad(true)
                    .build()
            } else {
                @Suppress("DEPRECATION")
                StaticLayout(
                    text,
                    textPaint,
                    printableWidth,
                    Layout.Alignment.ALIGN_NORMAL,
                    1.15f,
                    3f,
                    true
                )
            }

            val totalLines = bodyStaticLayout.lineCount
            var currentLineIndex = 0
            var pageNumber = 1

            // Multi-page loop
            while (currentLineIndex < totalLines || pageNumber == 1) {
                val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                val page = pdfDocument.startPage(pageInfo)
                val canvas = page.canvas

                var cursorY = margin.toFloat()

                // Header on every page
                if (pageNumber == 1) {
                    canvas.drawText(title.ifBlank { "Athena Workspace Note" }, margin.toFloat(), cursorY + 16f, titlePaint)
                    cursorY += 24f
                    val metaLine = "Date: $now  |  Words: ${stats.wordCount}  |  Characters: ${stats.charCount}"
                    canvas.drawText(metaLine, margin.toFloat(), cursorY + 12f, metaPaint)
                    cursorY += 20f
                    canvas.drawLine(margin.toFloat(), cursorY, (pageWidth - margin).toFloat(), cursorY, dividerPaint)
                    cursorY += 16f
                } else {
                    val subTitle = "${title.ifBlank { "Athena Note" }} (cont.)"
                    canvas.drawText(subTitle, margin.toFloat(), cursorY + 12f, metaPaint)
                    cursorY += 18f
                    canvas.drawLine(margin.toFloat(), cursorY, (pageWidth - margin).toFloat(), cursorY, dividerPaint)
                    cursorY += 14f
                }

                val bottomLimit = (pageHeight - margin - 25).toFloat()
                val pageStartLine = currentLineIndex

                while (currentLineIndex < totalLines) {
                    val lineBottom = bodyStaticLayout.getLineBottom(currentLineIndex)
                    val lineTop = bodyStaticLayout.getLineTop(pageStartLine)
                    val currentHeight = lineBottom - lineTop

                    if (cursorY + currentHeight > bottomLimit && currentLineIndex > pageStartLine) {
                        break
                    }
                    currentLineIndex++
                }

                // Render lines for this page
                canvas.save()
                canvas.translate(margin.toFloat(), cursorY)
                val startOffset = bodyStaticLayout.getLineTop(pageStartLine)
                canvas.clipRect(0, 0, printableWidth, (bottomLimit - cursorY).toInt())
                canvas.translate(0f, -startOffset.toFloat())
                bodyStaticLayout.draw(canvas)
                canvas.restore()

                // Footer with page numbering
                val footerText = "Athena Pro Notepad Studio • Page $pageNumber"
                canvas.drawText(footerText, margin.toFloat(), (pageHeight - margin + 15).toFloat(), metaPaint)

                pdfDocument.finishPage(page)
                pageNumber++

                if (currentLineIndex >= totalLines) break
            }

            FileOutputStream(file).use { out ->
                pdfDocument.writeTo(out)
            }

            shareFile(context, file, "application/pdf", "Share PDF Document")
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to generate PDF: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        } finally {
            try {
                pdfDocument?.close()
            } catch (_: Exception) {}
        }
    }

    private fun shareFile(context: Context, file: File, mimeType: String, chooserTitle: String) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, file.nameWithoutExtension)
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
        }

        val chooser = Intent.createChooser(intent, chooserTitle).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(chooser)
    }

    private fun sanitizeFilename(name: String): String {
        val clean = name.trim().replace("[^a-zA-Z0-9_-]".toRegex(), "_")
        return if (clean.isBlank()) "Athena_Document" else clean.take(40)
    }

    private fun escapeHtml(input: String): String {
        return input.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;")
    }
}
