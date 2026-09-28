package com.saydlawy.ultimatemushaf.mushaf

import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface

data class WordBounds(
    val verseKey: String,
    val position: Int,
    val bounds: RectF,
    val line: Int,
    val page: Int
)

data class LineGeometry(
    val line: Int,
    val baseline: Float,
    val left: Float,
    val right: Float
)

class QcfGeometryEngine {
    fun layout(
        page: MushafPage,
        pageNumber: Int,
        pageRect: RectF,
        qcf: Typeface,
        uthmanic: Typeface,
        paint: Paint
    ): Pair<List<LineGeometry>, List<WordBounds>> {
        val lineLeft = pageRect.left + pageRect.width() * 0.075f
        val lineRight = pageRect.right - pageRect.width() * 0.075f
        val lineTop = pageRect.top + pageRect.height() * 0.105f
        val lineBottom = pageRect.top + pageRect.height() * 0.885f
        val lineHeight = (lineBottom - lineTop) / 15f
        val lines = mutableListOf<LineGeometry>()
        val words = mutableListOf<WordBounds>()

        for (line in page.lines) {
            if (line.number !in 1..15 || line.words.isEmpty()) continue
            val textSize = pageRect.width() * 0.057f
            paint.typeface = qcf
            paint.textSize = textSize
            val qcfWidth = line.words.filter { it.type != "end" }
                .sumOf { paint.measureText(it.glyph).toDouble() }.toFloat()
            val fm = paint.fontMetrics
            val baseline = lineTop + (line.number - 0.5f) * lineHeight - (fm.ascent + fm.descent) / 2f
            var x = (lineLeft + lineRight + qcfWidth) / 2f

            for (word in line.words) {
                paint.typeface = if (word.type == "end") uthmanic else qcf
                paint.textSize = if (word.type == "end") textSize * 0.70f else textSize
                val glyph = if (word.type == "end") word.text else word.glyph
                val advance = paint.measureText(glyph)
                x -= advance
                words += WordBounds(
                    verseKey = word.verseKey,
                    position = word.position,
                    bounds = RectF(x, baseline + paint.fontMetrics.ascent, x + advance, baseline + paint.fontMetrics.descent),
                    line = line.number,
                    page = pageNumber
                )
            }
            lines += LineGeometry(line.number, baseline, lineLeft, lineRight)
        }
        return lines to words
    }
}
