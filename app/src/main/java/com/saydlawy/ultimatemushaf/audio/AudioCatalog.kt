package com.saydlawy.ultimatemushaf.audio

import android.content.Context
import java.io.File

data class ReciterProfile(
    val id: String,
    val name: String,
    val style: String,
    val riwayah: String,
    val source: String,
    val license: String
)

interface AudioUrlResolver {
    fun remoteUrl(reciter: ReciterProfile, verseKey: String): String?
}

class LocalFirstAudioRepository(
    private val context: Context,
    private val resolver: AudioUrlResolver
) : AudioRepository {
    override suspend fun resolve(segment: AudioSegment): String? {
        val local = localFile(segment.verseKey)
        return if (local.isFile && local.length() > 0L) local.toURI().toString() else null
    }

    fun resolveLocalOrRemote(reciter: ReciterProfile, verseKey: String): String? {
        val local = localFile(verseKey)
        if (local.isFile && local.length() > 0L) return local.toURI().toString()
        return resolver.remoteUrl(reciter, verseKey)
    }

    override suspend fun isDownloaded(verseKey: String): Boolean = localFile(verseKey).isFile

    fun destination(reciterId: String, verseKey: String): File {
        val safe = verseKey.replace(":", "_")
        return File(context.filesDir, "audio/$reciterId/$safe.mp3")
    }

    private fun localFile(verseKey: String): File {
        val safe = verseKey.replace(":", "_")
        return File(context.filesDir, "audio/default/$safe.mp3")
    }
}
