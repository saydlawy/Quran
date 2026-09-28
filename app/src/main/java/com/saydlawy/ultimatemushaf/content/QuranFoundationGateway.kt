package com.saydlawy.ultimatemushaf.content

data class QfCredentials(
    val clientId: String,
    val backendBaseUrl: String
)

interface QuranFoundationGateway {
    suspend fun syncResources(resources: String): Result<Unit>
    suspend fun verseAudio(verseKey: String, recitationId: Int): Result<String>
    suspend fun pageAudio(page: Int, recitationId: Int): Result<List<String>>
    suspend fun translations(resourceId: Int, verseKeys: List<String>): Result<Map<String, String>>
    suspend fun tafsir(resourceId: Int, verseKeys: List<String>): Result<Map<String, String>>
}

/*
 * Android security boundary:
 * Content API client credentials belong on a trusted backend.
 * The Android APK/source must never contain client_secret.
 * The backend adapter is intentionally injected so production deployment
 * can use the user's approved Quran Foundation client and its server-side
 * authentication flow without coupling secrets to the mobile binary.
 */
