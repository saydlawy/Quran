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
 * Security boundary:
 * - client_secret is intentionally absent from this Android module.
 * - Content API Client Credentials must live on a trusted backend.
 * - The Android app may use a public client identifier only where the
 *   selected Quran Foundation flow explicitly permits it.
 */
class BackendQuranFoundationGateway(
    private val credentials: QfCredentials,
    private val transport: suspend (String, Map<String, String>, String) -> String
) : QuranFoundationGateway {
    override suspend fun syncResources(resources: String): Result<Unit> =
        runCatching {
            transport(credentials.backendBaseUrl + "/sync", mapOf("client_id" to credentials.clientId), resources)
        }.map { Unit }

    override suspend fun verseAudio(verseKey: String, recitationId: Int): Result<String> =
        runCatching {
            transport(credentials.backendBaseUrl + "/audio/verse", mapOf("client_id" to credentials.clientId), "$recitationId:$verseKey")
        }

    override suspend fun pageAudio(page: Int, recitationId: Int): Result<List<String>> =
        runCatching {
            listOf(transport(credentials.backendBaseUrl + "/audio/page", mapOf("client_id" to credentials.clientId), "$recitationId:$page"))
        }

    override suspend fun translations(resourceId: Int, verseKeys: List<String>): Result<Map<String, String>> =
        runCatching {
            emptyMap()
        }

    override suspend fun tafsir(resourceId: Int, verseKeys: List<String>): Result<Map<String, String>> =
        runCatching {
            emptyMap()
        }
}
