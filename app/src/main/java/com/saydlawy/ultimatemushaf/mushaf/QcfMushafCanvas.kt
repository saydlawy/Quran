package com.saydlawy.ultimatemushaf.mushaf

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.abs
import kotlin.math.max

class QcfMushafCanvas @JvmOverloads constructor(c:Context,a:AttributeSet?=null):View(c,a){
 private val repo=QcfRepository(c); private val paint=Paint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG)
 private var pageNumber=1; private var dark=false; private var page=repo.page(1); private var downX=0f
 private var listener:((Int)->Unit)?=null
 private val fonts=object:LinkedHashMap<Int,Typeface>(4,.75f,true){override fun removeEldestEntry(e:MutableMap.MutableEntry<Int,Typeface>?)=size>3}
 fun setPageChangedListener(l:(Int)->Unit){listener=l}; fun setDark(v:Boolean){dark=v;invalidate()}
 fun setPage(v:Int){pageNumber=v.coerceIn(1,604);page=repo.page(pageNumber);invalidate()}
 fun page()=pageNumber
 override fun onDraw(c:Canvas){
  val w=width.toFloat();val h=height.toFloat();val paper=if(dark)Color.rgb(29,27,23)else Color.rgb(247,241,222);val ink=if(dark)Color.rgb(239,232,211)else Color.rgb(30,28,24)
  c.drawColor(if(dark)Color.rgb(12,12,11)else Color.rgb(225,219,204))
  val pw=minOf(w*.94f,h*.705f);val ph=pw/.705f;val l=(w-pw)/2f;val t=(h-ph)/2f;val r=RectF(l,t,l+pw,t+ph)
  paint.style=Paint.Style.FILL;paint.color=paper;c.drawRect(r,paint)
  paint.style=Paint.Style.STROKE;paint.strokeWidth=max(1f,pw/900f);paint.color=if(dark)Color.rgb(104,97,82)else Color.rgb(126,112,85);c.drawRect(r,paint)
  paint.typeface=fonts[pageNumber]?:repo.font(pageNumber).also{fonts[pageNumber]=it};paint.style=Paint.Style.FILL;paint.color=ink;paint.textAlign=Paint.Align.CENTER;paint.textSize=pw*.057f
  val left=l+pw*.075f;val right=l+pw*.925f;val top=t+ph*.105f;val bottom=t+ph*.885f;val lh=(bottom-top)/15f
  for(line in page.lines){
   if(line.number !in 1..15)continue
   val glyphs=line.words.joinToString(""){it.glyph};if(glyphs.isEmpty())continue
   val maxWidth=right-left;var size=pw*.057f
   while(size>pw*.038f){paint.textSize=size;if(paint.measureText(glyphs)<=maxWidth)break;size*=.985f}
   val fm=paint.fontMetrics;val y=top+(line.number-.5f)*lh-(fm.ascent+fm.descent)/2f
   c.drawText(glyphs,(left+right)/2f,y,paint)
  }
  paint.typeface=Typeface.DEFAULT;paint.textSize=pw*.022f;paint.color=if(dark)Color.rgb(180,173,153)else Color.rgb(105,95,76);c.drawText(pageNumber.toString(),w/2f,t+ph*.965f,paint)
 }
 override fun onTouchEvent(e:MotionEvent):Boolean{
  when(e.actionMasked){MotionEvent.ACTION_DOWN->{downX=e.x;return true};MotionEvent.ACTION_UP->{val dx=e.x-downX;if(abs(dx)>width*.12f){val n=if(dx<0)pageNumber+1 else pageNumber-1;if(n in 1..604){setPage(n);listener?.invoke(n)}};return true}}
  return true
 }
}
