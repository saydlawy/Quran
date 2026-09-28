package com.saydlawy.ultimatemushaf.navigation

data class MushafDestination(val page: Int? = null, val verseKey: String? = null)

object MushafDeepLinks {
    fun parse(uri: String): MushafDestination? {
        val normalized = uri.trim()
        Regex("^(?:https?://)?(?:www\\.)?quran\\.com/(?:[0-9]+/)?([0-9]+)(?::([0-9]+))?$").matchEntire(normalized)?.let {
            val ayah = it.groupValues[2].toIntOrNull()
            return MushafDestination(verseKey = if (ayah == null) null else it.groupValues[1] + ":" + ayah)
        }
        Regex("^ultimatemushaf://page/(\\d+)$").matchEntire(normalized)?.let {
            return MushafDestination(page = it.groupValues[1].toIntOrNull()?.coerceIn(1, 604))
        }
        Regex("^ultimatemushaf://ayah/(\\d+):(\\d+)$").matchEntire(normalized)?.let {
            return MushafDestination(verseKey = it.groupValues[1] + ":" + it.groupValues[2])
        }
        return null
    }
}
