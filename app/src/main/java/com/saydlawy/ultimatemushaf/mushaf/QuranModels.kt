package com.saydlawy.ultimatemushaf.mushaf
data class MushafWord(val verseKey:String,val position:Int,val type:String,val glyph:String,val text:String)
data class MushafLine(val number:Int,val words:List<MushafWord>)
data class MushafPage(val number:Int,val lines:List<MushafLine>)
data class VerseRef(val surah:Int,val ayah:Int,val page:Int,val juz:Int)
