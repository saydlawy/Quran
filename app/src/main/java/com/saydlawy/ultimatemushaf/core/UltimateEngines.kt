package com.saydlawy.ultimatemushaf.core

import java.text.Normalizer
import java.util.Locale

object ArabicSearchNormalizer {
    private val tashkeel = Regex("[\\u0610-\\u061A\\u064B-\\u065F\\u0670\\u06D6-\\u06ED]")
    fun normalize(input: String): String = Normalizer.normalize(input, Normalizer.Form.NFKC)
        .replace(tashkeel, "")
        .replace('أ','ا').replace('إ','ا').replace('آ','ا')
        .replace('ى','ي').replace('ة','ه').replace('ـ','')
        .lowercase(Locale.ROOT).trim()
}

class HifzEngine {
    fun nextRepetition(current: Int, total: Int): Int = (current + 1).coerceAtMost(total)
    fun progress(completed: Int, total: Int): Float = if (total <= 0) 0f else (completed.toFloat() / total).coerceIn(0f, 1f)
}

class KhatmaEngine {
    fun recommendedPagesPerDay(startPage: Int, endPage: Int, days: Int): Int {
        require(endPage >= startPage && days > 0)
        return kotlin.math.ceil((endPage - startPage + 1).toDouble() / days).toInt()
    }
    fun todayRange(currentPage: Int, endPage: Int, pagesPerDay: Int): IntRange {
        if (currentPage > endPage) return IntRange.EMPTY
        return currentPage..minOf(endPage, currentPage + pagesPerDay - 1)
    }
}

data class VerseSearchResult(val verseKey: String, val page: Int, val text: String)

class OfflineSearchIndex(private val entries: List<VerseSearchResult>) {
    fun search(query: String, limit: Int = 50): List<VerseSearchResult> {
        val q = ArabicSearchNormalizer.normalize(query)
        if (q.isBlank()) return emptyList()
        return entries.asSequence()
            .filter { ArabicSearchNormalizer.normalize(it.text).contains(q) }
            .take(limit).toList()
    }
}
