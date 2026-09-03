package com.nddfeon.demonic.player

import java.util.regex.Pattern

object YouTubeUrlParser {

    private val VIDEO_ID_PATTERN = Pattern.compile(
        "^.*(?:(?:youtu\\.be\\/|v\\/|vi\\/|u\\/\\w\\/|embed\\/|shorts\\/)|(?:(?:watch)?\\?v(?:i)?=|\\&v(?:i)?=))([^#\\&\\?]*).*"
    )

    fun extractVideoId(input: String?): String? {
        if (input.isNullOrBlank()) return null
        val trimmed = input.trim()

        // Direct 11-character ID
        if (trimmed.length == 11 && trimmed.matches(Regex("^[a-zA-Z0-9_-]{11}$"))) {
            return trimmed
        }

        val matcher = VIDEO_ID_PATTERN.matcher(trimmed)
        if (matcher.matches()) {
            val id = matcher.group(1)
            if (!id.isNullOrBlank() && id.length == 11) {
                return id
            }
        }
        return null
    }

    fun getThumbnailUrl(videoId: String, quality: String = "hqdefault"): String {
        return "https://img.youtube.com/vi/$videoId/$quality.jpg"
    }
}
