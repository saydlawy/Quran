package com.saydlawy.ultimatemushaf.core

data class Bookmark(
    val id: Long = 0L,
    val verseKey: String? = null,
    val page: Int,
    val title: String = "",
    val folder: String = "عام",
    val tags: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val permanent: Boolean = true
)

data class Annotation(
    val id: Long = 0L,
    val page: Int,
    val verseKey: String? = null,
    val type: String,
    val payload: String,
    val x: Float = 0f,
    val y: Float = 0f,
    val width: Float = 0f,
    val height: Float = 0f,
    val zIndex: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

data class ReadingPosition(
    val page: Int = 1,
    val verseKey: String? = null,
    val line: Int? = null,
    val updatedAt: Long = System.currentTimeMillis()
)

data class HifzSession(
    val id: Long = 0L,
    val name: String,
    val startVerse: String,
    val endVerse: String,
    val repetitions: Int = 1,
    val currentVerse: String? = null,
    val startedAt: Long = System.currentTimeMillis(),
    val completed: Boolean = false
)

data class KhatmaPlan(
    val id: Long = 0L,
    val name: String,
    val startPage: Int = 1,
    val endPage: Int = 604,
    val targetDays: Int = 30,
    val currentPage: Int = 1,
    val createdAt: Long = System.currentTimeMillis(),
    val completed: Boolean = false
)

data class DownloadItem(
    val id: String,
    val url: String,
    val destination: String,
    val bytes: Long = 0L,
    val totalBytes: Long = -1L,
    val state: String = "queued",
    val sha256: String? = null,
    val packVersion: String = "1",
    val license: String = ""
)
