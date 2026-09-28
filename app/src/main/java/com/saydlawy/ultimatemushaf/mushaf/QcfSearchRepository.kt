package com.saydlawy.ultimatemushaf.mushaf

import android.content.Context
import com.saydlawy.ultimatemushaf.core.ArabicSearchNormalizer
import org.json.JSONObject

data class MushafSearchHit(
    val verseKey: String,
    val page: Int,
    val line: Int,
    val text: String
)

class QcfSearchRepository(context: Context) {
    private val root: JSONObject = context.assets.open("quran/data/quran_qcf_v2.json")
        .use { JSONObject(String(it.readBytes(), Charsets.UTF_8)) }

    fun search(query: String, limit: Int = 50): List<MushafSearchHit> {
        val q = ArabicSearchNormalizer.normalize(query)
        if (q.isBlank()) return emptyList()
        val pages = root.getJSONObject("pages")
        val out = mutableListOf<MushafSearchHit>()
        for (page in 1..604) {
            if (out.size >= limit) break
            val lines = pages.getJSONObject(page.toString()).getJSONObject("lines")
            val keys = lines.keys()
            while (keys.hasNext() && out.size < limit) {
                val lineNo = keys.next().toInt()
                val words = lines.getJSONArray(lineNo.toString())
                val grouped = linkedMapOf<String, MutableList<String>>()
                for (i in 0 until words.length()) {
                    val w = words.getJSONObject(i)
                    if (w.optString("type", "word") == "end") continue
                    val verse = w.getString("verse")
                    grouped.getOrPut(verse) { mutableListOf() }.add(w.optString("text"))
                }
                for ((verse, parts) in grouped) {
                    val text = parts.joinToString(" ")
                    if (ArabicSearchNormalizer.normalize(text).contains(q)) {
                        out += MushafSearchHit(verse, page, lineNo, text)
                        if (out.size >= limit) break
                    }
                }
            }
        }
        return out
    }
}
