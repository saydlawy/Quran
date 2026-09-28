package com.saydlawy.ultimatemushaf.core

import android.content.Context
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest

class OfflinePackManager(private val context: Context) {
    fun download(item: DownloadItem, onProgress: (Long, Long) -> Unit = { _, _ -> }): DownloadItem {
        val target = File(item.destination)
        target.parentFile?.mkdirs()
        val part = File(target.absolutePath + ".part")
        val connection = (URL(item.url).openConnection() as HttpURLConnection).apply {
            connectTimeout = 20_000
            readTimeout = 60_000
            instanceFollowRedirects = true
        }
        return try {
            connection.connect()
            if (connection.responseCode !in 200..299) error("HTTP ${connection.responseCode}")
            val total = connection.contentLengthLong
            var done = if (part.exists()) part.length() else 0L
            connection.inputStream.use { input ->
                part.outputStream().use { output ->
                    val buffer = ByteArray(64 * 1024)
                    while (true) {
                        val n = input.read(buffer)
                        if (n < 0) break
                        output.write(buffer, 0, n)
                        done += n
                        onProgress(done, total)
                    }
                }
            }
            if (item.sha256 != null && sha256(part) != item.sha256.lowercase()) error("SHA-256 mismatch")
            if (!part.renameTo(target)) error("Unable to finalize download")
            item.copy(bytes = done, totalBytes = total, state = "completed")
        } catch (_: Throwable) {
            item.copy(bytes = if (part.exists()) part.length() else 0L, totalBytes = connection.contentLengthLong, state = "error")
        } finally {
            connection.disconnect()
        }
    }

    private fun sha256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val buffer = ByteArray(64 * 1024)
            while (true) {
                val n = input.read(buffer)
                if (n < 0) break
                digest.update(buffer, 0, n)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }
}
