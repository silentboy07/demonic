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

    private val defaultTrendingSongs = listOf(
        YouTubeSearchResult("jfKfPfyJRdk", "lofi hip hop radio 📚 beats to relax/study to", "Lofi Girl"),
        YouTubeSearchResult("4xDzrJKXOOY", "synthwave radio 🌌 beats to chill/game to", "Lofi Girl"),
        YouTubeSearchResult("fHI8X483mQw", "Midnight City - M83", "M83"),
        YouTubeSearchResult("dQw4w9WgXcQ", "Never Gonna Give You Up", "Rick Astley"),
        YouTubeSearchResult("kJQP7kiw5Fk", "Despacito ft. Daddy Yankee", "Luis Fonsi"),
        YouTubeSearchResult("JGwWNGJdvx8", "Shape of You", "Ed Sheeran"),
        YouTubeSearchResult("OPf0YbXqDm0", "Uptown Funk ft. Bruno Mars", "Mark Ronson"),
        YouTubeSearchResult("9bZkp7q19f0", "Gangnam Style", "PSY")
    )

    fun getTrending(): List<YouTubeSearchResult> = defaultTrendingSongs

    suspend fun search(query: String): List<YouTubeSearchResult> = withContext(Dispatchers.IO) {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) {
            return@withContext defaultTrendingSongs
        }

        // If user entered a direct URL or 11-char ID
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

            while (matcher.find() && results.size < 15) {
                val videoId = matcher.group(1)
                val rawTitle = matcher.group(2)
                if (!videoId.isNullOrBlank() && !rawTitle.isNullOrBlank() && !seenIds.contains(videoId)) {
                    seenIds.add(videoId)
                    val cleanTitle = rawTitle.replace("\\u0026", "&")
                        .replace("\\\"", "\"")
                        .replace("&#39;", "'")
                    results.add(
                        YouTubeSearchResult(
                            videoId = videoId,
                            title = cleanTitle,
                            channel = "YouTube"
                        )
                    )
                }
            }

            if (results.isNotEmpty()) {
                results
            } else {
                // Fallback: match filter on trending
                defaultTrendingSongs.filter {
                    it.title.contains(trimmed, ignoreCase = true) || it.channel.contains(trimmed, ignoreCase = true)
                }.ifEmpty { defaultTrendingSongs }
            }
        } catch (_: Exception) {
            // Safe fallback if offline or request blocked
            defaultTrendingSongs.filter {
                it.title.contains(trimmed, ignoreCase = true) || it.channel.contains(trimmed, ignoreCase = true)
            }.ifEmpty { defaultTrendingSongs }
        }
    }
}
