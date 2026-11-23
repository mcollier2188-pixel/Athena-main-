package com.example.model

import android.content.Context
import java.io.File

data class ManualChapter(
    val number: Int,
    val id: String,
    val title: String,
    val subtitle: String,
    val content: String,
    val summaryBullets: List<String>,
    val tags: List<String>
)

/**
 * Offline repository for the ATHENA AI STUDIO // MASTER OPERATIONAL & TECHNICAL MANUAL (2.0-PROD).
 * Reads verbatim Markdown from bundled Android assets (`assets/docs/MASTER_OPERATIONAL_MANUAL.md`)
 * or repository `docs/MASTER_OPERATIONAL_MANUAL.md` during pure-JVM tests, parsing all 10 chapters
 * dynamically so the Archive Reader and Workspace Documentation / FAQ tabs stay 100% synchronized.
 */
object MasterManualRepository {
    const val ASSET_PATH = "docs/MASTER_OPERATIONAL_MANUAL.md"
    const val DOCUMENT_TITLE = "ATHENA AI STUDIO // MASTER OPERATIONAL & TECHNICAL MANUAL"
    const val DOCUMENT_SUBTITLE = "Version 2.0-PROD • 2026 Continuum • Local-First / Zero-Allocation Edge"
    const val DOCUMENT_VERSION = "2.0-PROD"
    const val TARGET_RELEASE = "2026 Continuum"

    @Volatile
    private var cachedMarkdown: String? = null

    @Volatile
    private var cachedChapters: List<ManualChapter>? = null

    fun initialize(context: Context): String {
        cachedMarkdown?.let { return it }
        synchronized(this) {
            cachedMarkdown?.let { return it }
            val loaded = runCatching {
                context.assets.open(ASSET_PATH).bufferedReader(Charsets.UTF_8).use { it.readText() }
            }.getOrNull()
            if (!loaded.isNullOrBlank()) {
                cachedMarkdown = loaded
                cachedChapters = parseChapters(loaded)
                return loaded
            }
            return getFullManualMarkdown(null)
        }
    }

    fun getFullManualMarkdown(context: Context? = null): String {
        if (context != null) {
            return initialize(context)
        }
        cachedMarkdown?.let { return it }
        synchronized(this) {
            cachedMarkdown?.let { return it }
            val candidates = listOf(
                File("docs/MASTER_OPERATIONAL_MANUAL.md"),
                File("../docs/MASTER_OPERATIONAL_MANUAL.md"),
                File("app/src/main/assets/docs/MASTER_OPERATIONAL_MANUAL.md"),
                File("../app/src/main/assets/docs/MASTER_OPERATIONAL_MANUAL.md")
            )
            val fromDisk = candidates.firstOrNull { it.isFile }?.readText(Charsets.UTF_8)
            val resolved = if (!fromDisk.isNullOrBlank()) fromDisk else FALLBACK_HEADER
            cachedMarkdown = resolved
            cachedChapters = parseChapters(resolved)
            return resolved
        }
    }

    fun getChapters(context: Context? = null): List<ManualChapter> {
        val markdown = getFullManualMarkdown(context)
        return cachedChapters ?: parseChapters(markdown).also { cachedChapters = it }
    }

    internal fun parseChapters(markdown: String): List<ManualChapter> {
        val chapterRegex = Regex("(?m)^## CHAPTER (\\d+):\\s*(.+)$")
        val matches = chapterRegex.findAll(markdown).toList()
        if (matches.isEmpty()) return emptyList()

        return matches.mapIndexed { idx, match ->
            val number = match.groupValues[1].toIntOrNull() ?: (idx + 1)
            val rawTitle = match.groupValues[2].trim()
            val startIndex = match.range.first
            val endIndex = if (idx + 1 < matches.size) matches[idx + 1].range.first else markdown.length
            val rawBody = markdown.substring(startIndex, endIndex)
                .trim()
                .removeSuffix("---")
                .trim()

            val subheadings = Regex("(?m)^###\\s+\\d+\\.\\d+\\s+(.+)$")
                .findAll(rawBody)
                .map { it.groupValues[1].trim().replace("`", "") }
                .toList()

            val subtitle = if (subheadings.isNotEmpty()) {
                subheadings.joinToString(" · ")
            } else {
                "ATHENA 2.0-PROD Specification"
            }

            val bullets = rawBody.lineSequence()
                .map { it.trim() }
                .filter { it.startsWith("* ") || it.matches(Regex("^\\d+\\.\\s+.*")) }
                .map {
                    it.removePrefix("* ")
                        .replace(Regex("^\\d+\\.\\s+"), "")
                        .replace("**", "")
                        .replace("`", "")
                        .trim()
                }
                .filter { it.isNotEmpty() }
                .take(8)
                .toList()

            val baseTags = buildList {
                add("2.0-prod")
                add("manual")
                add("chapter $number")
                addAll(
                    rawTitle.lowercase()
                        .split(Regex("[^a-z0-9']+"))
                        .filter { it.length >= 3 }
                )
                subheadings.forEach { sub ->
                    addAll(
                        sub.lowercase()
                            .split(Regex("[^a-z0-9'.£-]+"))
                            .filter { it.length >= 3 }
                    )
                }
            }.distinct()

            ManualChapter(
                number = number,
                id = "master_manual_ch_${number.toString().padStart(2, '0')}",
                title = rawTitle,
                subtitle = subtitle,
                content = rawBody,
                summaryBullets = bullets,
                tags = baseTags
            )
        }
    }

    private const val FALLBACK_HEADER =
        "# ATHENA AI STUDIO // MASTER OPERATIONAL & TECHNICAL MANUAL\n**Document Version:** 2.0-PROD • **Target Release:** 2026 Continuum"
}
