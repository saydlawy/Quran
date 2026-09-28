package com.saydlawy.ultimatemushaf.mushaf

import android.content.Context
import android.graphics.Typeface
import org.json.JSONObject
import java.nio.charset.StandardCharsets
import java.util.concurrent.ConcurrentHashMap

class QcfRepository(private val context: Context) {
    private val cache = ConcurrentHashMap<Int, MushafPage>()
    private val qcfFonts = ConcurrentHashMap<Int, Typeface>()
    private var root: JSONObject? = null

    @Synchronized
    private fun data(): JSONObject {
        root?.let { return it }
        val bytes = context.assets.open("quran/data/quran_qcf_v2.json").use { it.readBytes() }
        return JSONObject(String(bytes, StandardCharsets.UTF_8)).also { root = it }
    }

    fun page(n: Int): MushafPage =
        cache[n] ?: parsePage(n).also { cache[n] = it }

    fun qcfFont(n: Int): Typeface =
        qcfFonts[n] ?: Typeface.createFromAsset(context.assets, "quran/fonts/qcf/v2/p$n.ttf")
            .also { qcfFonts[n] = it }

    fun uthmanicFont(): Typeface =
        Typeface.createFromAsset(context.assets, "quran/fonts/uthmanic/UthmanicHafs1Ver18.ttf")

    fun verse(k: String): VerseRef? {
        val o = data().optJSONObject("verses")?.optJSONObject(k) ?: return null
        return VerseRef(
            k.substringBefore(":").toInt(),
            k.substringAfter(":").toInt(),
            o.optInt("page"),
            o.optInt("juz")
        )
    }

    private fun parsePage(n: Int): MushafPage {
        val lines = data().getJSONObject("pages").getJSONObject(n.toString()).getJSONObject("lines")
        val out = mutableListOf<MushafLine>()
        val it = lines.keys()
        while (it.hasNext()) {
            val key = it.next()
            val a = lines.getJSONArray(key)
            val words = ArrayList<MushafWord>(a.length())
            for (i in 0 until a.length()) {
                val w = a.getJSONObject(i)
                words += MushafWord(
                    w.getString("verse"),
                    w.optInt("position"),
                    w.optString("type", "word"),
                    w.getString("glyph"),
                    w.optString("text")
                )
            }
            out += MushafLine(key.toInt(), words)
        }
        return MushafPage(n, out.sortedBy { it.number })
    }
}
