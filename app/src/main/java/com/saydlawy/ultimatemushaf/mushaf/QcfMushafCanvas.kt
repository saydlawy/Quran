package com.saydlawy.ultimatemushaf.mushaf

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.abs
import kotlin.math.max

class QcfMushafCanvas @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {
    private val repo = QcfRepository(context)
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG)
    private val fonts = object : LinkedHashMap<Int, Typeface>(4, 0.75f, true) {
        override fun removeEldestEntry(e: MutableMap.MutableEntry<Int, Typeface>?) = size > 3
    }

    private var pageNumber = 1
    private var dark = false
    private var page = repo.page(1)
    private var downX = 0f
    private var listener: ((Int) -> Unit)? = null

    fun setPageChangedListener(l: (Int) -> Unit) { listener = l }
    fun setDark(value: Boolean) { dark = value; invalidate() }
    fun setPage(value: Int) {
        pageNumber = value.coerceIn(1, 604)
        page = repo.page(pageNumber)
        invalidate()
    }
    fun page() = pageNumber

    override fun onDraw(canvas: Canvas) {
        val width = width.toFloat()
        val height = height.toFloat()
        val paper = if (dark) Color.rgb(29, 27, 23) else Color.rgb(247, 241, 222)
        val ink = if (dark) Color.rgb(239, 232, 211) else Color.rgb(30, 28, 24)

        canvas.drawColor(if (dark) Color.rgb(12, 12, 11) else Color.rgb(225, 219, 204))

        val pageWidth = minOf(width * 0.94f, height * 0.705f)
        val pageHeight = pageWidth / 0.705f
        val left = (width - pageWidth) / 2f
        val top = (height - pageHeight) / 2f
        val rect = RectF(left, top, left + pageWidth, top + pageHeight)

        paint.style = Paint.Style.FILL
        paint.color = paper
        canvas.drawRect(rect, paint)

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = max(1f, pageWidth / 900f)
        paint.color = if (dark) Color.rgb(104, 97, 82) else Color.rgb(126, 112, 85)
        canvas.drawRect(rect, paint)

        val qcf = fonts[pageNumber] ?: repo.qcfFont(pageNumber).also { fonts[pageNumber] = it }
        val uthmanic = repo.uthmanicFont()
        val lineLeft = left + pageWidth * 0.075f
        val lineRight = left + pageWidth * 0.925f
        val lineTop = top + pageHeight * 0.105f
        val lineBottom = top + pageHeight * 0.885f
        val lineHeight = (lineBottom - lineTop) / 15f

        for (line in page.lines) {
            if (line.number !in 1..15 || line.words.isEmpty()) continue

            var textSize = pageWidth * 0.057f
            val maxLineWidth = lineRight - lineLeft

            // QCF is page-specific; measure each glyph run with the exact page font.
            paint.typeface = qcf
            paint.textSize = textSize
            var qcfWidth = line.words.filter { it.type != "end" }.sumOf {
                paint.measureText(it.glyph).toDouble()
            }.toFloat()

            while (textSize > pageWidth * 0.038f && qcfWidth > maxLineWidth * 0.96f) {
                textSize *= 0.985f
                paint.textSize = textSize
                qcfWidth = line.words.filter { it.type != "end" }.sumOf {
                    paint.measureText(it.glyph).toDouble()
                }.toFloat()
            }

            val baseline = run {
                val fm = paint.fontMetrics
                lineTop + (line.number - 0.5f) * lineHeight - (fm.ascent + fm.descent) / 2f
            }

            // API word order is canonical Mushaf order. Paint each word from the
            // right edge toward the left so verse-end markers can use Uthmanic Hafs.
            var x = (lineLeft + lineRight + qcfWidth) / 2f
            for (word in line.words) {
                val runPaint = if (word.type == "end") {
                    paint.apply { typeface = uthmanic; textSize = textSize * 0.70f }
                } else {
                    paint.apply { typeface = qcf; textSize = textSize }
                }

                val glyph = if (word.type == "end") word.text else word.glyph
                val advance = runPaint.measureText(glyph)
                x -= advance
                runPaint.color = ink
                canvas.drawText(glyph, x, baseline, runPaint)
            }
        }

        paint.typeface = Typeface.DEFAULT
        paint.textAlign = Paint.Align.CENTER
        paint.textSize = pageWidth * 0.022f
        paint.color = if (dark) Color.rgb(180, 173, 153) else Color.rgb(105, 95, 76)
        canvas.drawText(pageNumber.toString(), width / 2f, top + pageHeight * 0.965f, paint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                downX = event.x
                return true
            }
            MotionEvent.ACTION_UP -> {
                val dx = event.x - downX
                if (abs(dx) > width * 0.12f) {
                    val next = if (dx < 0) pageNumber + 1 else pageNumber - 1
                    if (next in 1..604) {
                        setPage(next)
                        listener?.invoke(next)
                    }
                }
                return true
            }
        }
        return true
    }
}
