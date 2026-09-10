package com.nddfeon.demonic.player

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.regex.Pattern
import javax.inject.Inject
import javax.inject.Singleton

data class YouTubeSearchResult(
    val videoId: String,
    val title: String,
    val channel: String = "",
    val thumbnailUrl: String = "https://img.youtube.com/vi/$videoId/hqdefault.jpg"
)

@Singleton
class YouTubeSearchManager @Inject constructor() {

    companion object {
        val CATEGORIES = listOf(
            "🔥 Trending",
            "⚡ Punjabi Hits",
            "💃 Bollywood Party",
            "🎧 Lo-Fi Chill",
            "💪 Gym Phonk",
            "🌍 Global Top 50"
        )
    }

    private val curatedPlaylists: Map<String, List<YouTubeSearchResult>> = mapOf(
        "🔥 Trending" to listOf(
            YouTubeSearchResult("ekr2nIex040", "Die With A Smile", "Lady Gaga & Bruno Mars"),
            YouTubeSearchResult("euCqAq6BRa4", "BIRDS OF A FEATHER", "Billie Eilish"),
            YouTubeSearchResult("V9PVRfjEBTI", "Beautiful Things", "Benson Boone"),
            YouTubeSearchResult("kPa7bsKwL-c", "Let Me Love You", "DJ Snake ft. Justin Bieber"),
            YouTubeSearchResult("RgKAFK5djSk", "See You Again", "Wiz Khalifa ft. Charlie Puth"),
            YouTubeSearchResult("OPf0YbXqDm0", "Save Your Tears", "The Weeknd"),
            YouTubeSearchResult("JGwWNGJdvx8", "Shape of You", "Ed Sheeran")
        ),
        "⚡ Punjabi Hits" to listOf(
            YouTubeSearchResult("RLhzXpe4cfg", "Tauba Tauba (Bad Newz)", "Karan Aujla"),
            YouTubeSearchResult("cl0a3iCS4To", "Winning Speech", "Karan Aujla"),
            YouTubeSearchResult("Vns_ykYx5X8", "Lover", "Diljit Dosanjh"),
            YouTubeSearchResult("hblL16Sl_Vo", "Born to Shine", "Diljit Dosanjh"),
            YouTubeSearchResult("2Vp0c_pB3z8", "Brown Munde", "AP Dhillon"),
            YouTubeSearchResult("7ca_655q24g", "Excuses", "AP Dhillon"),
            YouTubeSearchResult("8Pf5i_r_EwE", "295", "Sidhu Moose Wala")
        ),
        "💃 Bollywood Party" to listOf(
            YouTubeSearchResult("Vbhhx8s8x8w", "Chaleya (Jawan)", "Arijit Singh & Shilpa Rao"),
            YouTubeSearchResult("BddP6PYo2gs", "Kesariya (Brahmastra)", "Arijit Singh"),
            YouTubeSearchResult("GwgJj3u05kM", "Apna Bana Le (Bhediya)", "Arijit Singh"),
            YouTubeSearchResult("II2EO3Nw4m0", "Badtameez Dil (YJHD)", "Benny Dayal"),
            YouTubeSearchResult("0_yS9pG6_rM", "Kala Chashma (Baar Baar Dekho)", "Amar Arshi & Badshah"),
            YouTubeSearchResult("zpsVpnvFfZQ", "Ghungroo (War)", "Arijit Singh & Shilpa Rao")
        ),
        "🎧 Lo-Fi Chill" to listOf(
            YouTubeSearchResult("jfKfPfyJRdk", "lofi hip hop radio - beats to relax/study to", "Lofi Girl"),
            YouTubeSearchResult("4xDzrJKXOOY", "synthwave radio - beats to chill/game to", "Lofi Girl"),
            YouTubeSearchResult("fHI8X483mQw", "Midnight City", "M83"),
            YouTubeSearchResult("kgx4WGK0oNU", "coffee breath - lofi vibes", "Chillhop Music"),
            YouTubeSearchResult("5qap5aO4i9A", "Lofi Hip Hop Chill Study Beats", "ChilledCow")
        ),
        "💪 Gym Phonk" to listOf(
            YouTubeSearchResult("w-sQRS-Mun8", "METAMORPHOSIS", "INTERWORLD"),
            YouTubeSearchResult("t6CLUTYl35U", "MURDER IN MY MIND", "KORDHELL"),
            YouTubeSearchResult("cl4XfL_344Y", "RAVE", "Dxrk ダーク"),
            YouTubeSearchResult("1-xGerv5FOk", "AUTOMOTIVO TAN TAN TAN", "Phonk Brasil"),
            YouTubeSearchResult("J3_IT_S82eM", "MONTAGEM - CORAL", "DJ Holanda")
        ),
        "🌍 Global Top 50" to listOf(
            YouTubeSearchResult("4NRXx6U8ABQ", "Blinding Lights", "The Weeknd"),
            YouTubeSearchResult("kJQP7kiw5Fk", "Despacito ft. Daddy Yankee", "Luis Fonsi"),
            YouTubeSearchResult("09R8_2nJtjg", "Sugar", "Maroon 5"),
            YouTubeSearchResult("CevxZvSJLk8", "Roar", "Katy Perry"),
            YouTubeSearchResult("YQHsXMglC9A", "Hello", "Adele"),
            YouTubeSearchResult("k2qgadSvNyU", "New Rules", "Dua Lipa")
        )
    )

    fun getTrending(): List<YouTubeSearchResult> = curatedPlaylists["🔥 Trending"] ?: emptyList()

    fun getCategorySongs(category: String): List<YouTubeSearchResult> {
        return curatedPlaylists[category] ?: curatedPlaylists["🔥 Trending"] ?: emptyList()
    }

    suspend fun fetchPlaylistVideos(playlistId: String): List<YouTubeSearchResult> = withContext(Dispatchers.IO) {
        val cleanPlaylistId = playlistId.trim()
        if (cleanPlaylistId.isEmpty()) return@withContext emptyList()

        try {
            val playlistUrl = "https://www.youtube.com/playlist?list=$cleanPlaylistId"
            val url = URL(playlistUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
            connection.setRequestProperty("Accept-Language", "en-US,en;q=0.9")
            connection.connectTimeout = 8000
            connection.readTimeout = 8000

            val reader = BufferedReader(InputStreamReader(connection.inputStream))
            val sb = StringBuilder()
            var line: String?
            var lineCount = 0
            while (reader.readLine().also { line = it } != null && lineCount < 4000) {
                sb.append(line)
                lineCount++
            }
            reader.close()

            val html = sb.toString()
            val results = mutableListOf<YouTubeSearchResult>()
            val seenIds = mutableSetOf<String>()

            // 1. YouTube lockup format: "contentId":"...","title":{"content":"..."
            val lockupPattern = Pattern.compile("\"contentId\":\"([a-zA-Z0-9_-]{11})\".*?\"title\":\\{\"content\":\"([^\"]+)\"")
            val matcher1 = lockupPattern.matcher(html)
            while (matcher1.find() && results.size < 50) {
                val videoId = matcher1.group(1)
                val rawTitle = matcher1.group(2)
                if (!videoId.isNullOrBlank() && !rawTitle.isNullOrBlank() && seenIds.add(videoId)) {
                    results.add(
                        YouTubeSearchResult(
                            videoId = videoId,
                            title = cleanHtml(rawTitle),
                            channel = "Playlist Track"
                        )
                    )
                }
            }

            // 2. Fallback: classic playlistVideoRenderer format
            if (results.isEmpty()) {
                val runPattern = Pattern.compile("\"videoId\":\"([a-zA-Z0-9_-]{11})\".*?\"title\":\\{\"runs\":\\[\\{\"text\":\"([^\"]+)\"")
                val matcher2 = runPattern.matcher(html)
                while (matcher2.find() && results.size < 50) {
                    val videoId = matcher2.group(1)
                    val rawTitle = matcher2.group(2)
                    if (!videoId.isNullOrBlank() && !rawTitle.isNullOrBlank() && seenIds.add(videoId)) {
                        results.add(
                            YouTubeSearchResult(
                                videoId = videoId,
                                title = cleanHtml(rawTitle),
                                channel = "Playlist Track"
                            )
                        )
                    }
                }
            }

            // 3. Accessibility label fallback
            if (results.isEmpty()) {
                val accessPattern = Pattern.compile("\"contentId\":\"([a-zA-Z0-9_-]{11})\".*?\"accessibilityContext\":\\{\"label\":\"([^\"]+)\"")
                val matcher3 = accessPattern.matcher(html)
                while (matcher3.find() && results.size < 50) {
                    val videoId = matcher3.group(1)
                    val rawLabel = matcher3.group(2)
                    if (!videoId.isNullOrBlank() && !rawLabel.isNullOrBlank() && seenIds.add(videoId)) {
                        val titlePart = rawLabel.split(" by ").firstOrNull() ?: rawLabel
                        results.add(
                            YouTubeSearchResult(
                                videoId = videoId,
                                title = cleanHtml(titlePart),
                                channel = "Playlist Track"
                            )
                        )
                    }
                }
            }

            results
        } catch (_: Exception) {
            emptyList()
        }
    }

    suspend fun search(query: String): List<YouTubeSearchResult> = withContext(Dispatchers.IO) {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) {
            return@withContext getTrending()
        }

        // Check if user entered a playlist link
        val playlistId = YouTubeUrlParser.extractPlaylistId(trimmed)
        if (playlistId != null) {
            val playlistItems = fetchPlaylistVideos(playlistId)
            if (playlistItems.isNotEmpty()) {
                return@withContext playlistItems
            }
        }

        // If user entered a direct video URL or 11-char ID
        val directId = YouTubeUrlParser.extractVideoId(trimmed)
        if (directId != null) {
            return@withContext listOf(
                YouTubeSearchResult(
                    videoId = directId,
                    title = "YouTube Video: $directId",
                    channel = "Direct Link"
                )
            )
        }

        try {
            val encodedQuery = URLEncoder.encode(trimmed, "UTF-8")
            val searchUrl = "https://www.youtube.com/results?search_query=$encodedQuery"
            val url = URL(searchUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
            connection.setRequestProperty("Accept-Language", "en-US,en;q=0.9")
            connection.connectTimeout = 6000
            connection.readTimeout = 6000

            val reader = BufferedReader(InputStreamReader(connection.inputStream))
            val sb = StringBuilder()
            var line: String?
            var lineCount = 0
            while (reader.readLine().also { line = it } != null && lineCount < 2500) {
                sb.append(line)
                lineCount++
            }
            reader.close()

            val html = sb.toString()
            val results = mutableListOf<YouTubeSearchResult>()
            val seenIds = mutableSetOf<String>()

            // Pattern for videoId and title inside ytInitialData
            val pattern = Pattern.compile("\"videoId\":\"([a-zA-Z0-9_-]{11})\".*?\"title\":\\{\"runs\":\\[\\{\"text\":\"([^\"]+)\"")
            val matcher = pattern.matcher(html)

            while (matcher.find() && results.size < 20) {
                val videoId = matcher.group(1)
                val rawTitle = matcher.group(2)
                if (!videoId.isNullOrBlank() && !rawTitle.isNullOrBlank() && seenIds.add(videoId)) {
                    results.add(
                        YouTubeSearchResult(
                            videoId = videoId,
                            title = cleanHtml(rawTitle),
                            channel = "YouTube"
                        )
                    )
                }
            }

            if (results.isNotEmpty()) {
                results
            } else {
                // Fallback: match filter on all curated songs
                val allCurated = curatedPlaylists.values.flatten()
                allCurated.filter {
                    it.title.contains(trimmed, ignoreCase = true) || it.channel.contains(trimmed, ignoreCase = true)
                }.ifEmpty { getTrending() }
            }
        } catch (_: Exception) {
            val allCurated = curatedPlaylists.values.flatten()
            allCurated.filter {
                it.title.contains(trimmed, ignoreCase = true) || it.channel.contains(trimmed, ignoreCase = true)
            }.ifEmpty { getTrending() }
        }
    }

    private fun cleanHtml(raw: String): String {
        return raw.replace("\\u0026", "&")
            .replace("&amp;", "&")
            .replace("\\\"", "\"")
            .replace("&quot;", "\"")
            .replace("&#39;", "'")
            .replace("&apos;", "'")
            .replace("\\", "")
            .trim()
    }
}
