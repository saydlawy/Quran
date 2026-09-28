package com.saydlawy.ultimatemushaf.content

data class OfflineStudyPack(
    val id: String,
    val version: String,
    val language: String,
    val kind: String,
    val source: String,
    val license: String,
    val sha256: String,
    val fileName: String
)

interface StudyPackProvider {
    fun available(): List<OfflineStudyPack>
    fun install(pack: OfflineStudyPack): Boolean
    fun remove(packId: String): Boolean
}

enum class ReadingLayer { QURAN, TRANSLATION, TAFSIR, TAJWEED, WORD_ANALYSIS, NOTES }
