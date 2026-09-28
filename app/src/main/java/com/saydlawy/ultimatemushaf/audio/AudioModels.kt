package com.saydlawy.ultimatemushaf.audio
data class AudioSegment(
 val verseKey:String,
 val startMs:Long,
 val endMs:Long,
 val page:Int,
 val wordPosition:Int?=null
)
data class Reciter(val id:String,val name:String,val riwayah:String,val source:String,val license:String)
interface AudioRepository{
 suspend fun resolve(segment:AudioSegment):String?
 suspend fun isDownloaded(verseKey:String):Boolean
}