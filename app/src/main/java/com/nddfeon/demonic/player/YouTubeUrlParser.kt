package com.nddfeon.demonic.player

import java.util.regex.Pattern

object YouTubeUrlParser {

    private val PATTERNS = listOf(
        // youtu.be/ID
        Pattern.compile("youtu\\.be/([a-zA-Z0-9_-]{11})"),
        // ?v=ID or &v=ID
        Pattern.compile("[?&]v=([a-zA-Z0-9_-]{11})"),
        // /embed/ID
        Pattern.compile("/embed/([a-zA-Z0-9_-]{11})"),
        // /shorts/ID
        Pattern.compile("/shorts/([a-zA-Z0-9_-]{11})"),
        // /v/ID or /vi/ID
        Pattern.compile("/vi?/([a-zA-Z0-9_-]{11})"),
        // direct 11-character alphanumeric string
        Pattern.compile("^[a-zA-Z0-9_-]{11}$")
    )

    fun extractVideoId(input: String?): String? {
        if (input.isNullOrBlank()) return null
        val trimmed = input.trim()

        for (pattern in PATTERNS) {
            val matcher = pattern.matcher(trimmed)
            if (matcher.find()) {
                val groupCount = matcher.groupCount()
                val id = if (groupCount >= 1) matcher.group(1) else matcher.group(0)
                if (!id.isNullOrBlank() && id.length == 11) {
                    return id
                }
            }
        }
        return null
    }

    fun getThumbnailUrl(videoId: String, quality: String = "hqdefault"): String {
        return "https://img.youtube.com/vi/$videoId/$quality.jpg"
    }
}
