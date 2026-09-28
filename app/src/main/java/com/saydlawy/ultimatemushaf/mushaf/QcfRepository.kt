package com.saydlawy.ultimatemushaf.mushaf

import android.content.Context
import android.graphics.Typeface
import org.json.JSONObject
import java.util.concurrent.ConcurrentHashMap

class QcfRepository(private val context:Context){
 private val cache=ConcurrentHashMap<Int,MushafPage>(); private var root:JSONObject?=null
 @Synchronized private fun data():JSONObject{
  root?.let{return it}
  val b=context.assets.open("quran/data/quran_qcf_v2.json").use{it.readBytes()}
  return JSONObject(String(b,Charsets.UTF_8)).also{root=it}
 }
 fun page(n:Int)=cache[n]?:parsePage(n).also{cache[n]=it}
 fun font(n:Int):Typeface=Typeface.createFromAsset(context.assets,"quran/fonts/qcf/v2/p$n.ttf")
 fun verse(k:String):VerseRef?{
  val o=data().optJSONObject("verses")?.optJSONObject(k)?:return null
  return VerseRef(k.substringBefore(":").toInt(),k.substringAfter(":").toInt(),o.optInt("page"),o.optInt("juz"))
 }
 private fun parsePage(n:Int):MushafPage{
  val lines=data().getJSONObject("pages").getJSONObject(n.toString()).getJSONObject("lines")
  val out=mutableListOf<MushafLine>(); val it=lines.keys()
  while(it.hasNext()){
   val key=it.next(); val a=lines.getJSONArray(key); val words=ArrayList<MushafWord>(a.length())
   for(i in 0 until a.length()){val w=a.getJSONObject(i);words+=MushafWord(w.getString("verse"),w.optInt("position"),w.optString("type","word"),w.getString("glyph"),w.optString("text"))}
   out+=MushafLine(key.toInt(),words)
  }
  return MushafPage(n,out.sortedBy{it.number})
 }
}
