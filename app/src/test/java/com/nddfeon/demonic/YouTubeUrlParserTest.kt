package com.nddfeon.demonic

import com.nddfeon.demonic.player.YouTubeUrlParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class YouTubeUrlParserTest {

    @Test
    fun testDirectVideoId() {
        val id = "dQw4w9WgXcQ"
        assertEquals("dQw4w9WgXcQ", YouTubeUrlParser.extractVideoId(id))
    }

    @Test
    fun testStandardWatchUrl() {
        val url = "https://www.youtube.com/watch?v=dQw4w9WgXcQ"
        assertEquals("dQw4w9WgXcQ", YouTubeUrlParser.extractVideoId(url))
    }

    @Test
    fun testStandardWatchUrlWithExtraParams() {
        val url = "https://www.youtube.com/watch?v=dQw4w9WgXcQ&t=42s&feature=share"
        assertEquals("dQw4w9WgXcQ", YouTubeUrlParser.extractVideoId(url))
    }

    @Test
    fun testShortUrl() {
        val url = "https://youtu.be/dQw4w9WgXcQ"
        assertEquals("dQw4w9WgXcQ", YouTubeUrlParser.extractVideoId(url))
    }

    @Test
    fun testShortsUrl() {
        val url = "https://www.youtube.com/shorts/dQw4w9WgXcQ"
        assertEquals("dQw4w9WgXcQ", YouTubeUrlParser.extractVideoId(url))
    }

    @Test
    fun testMusicUrl() {
        val url = "https://music.youtube.com/watch?v=dQw4w9WgXcQ"
        assertEquals("dQw4w9WgXcQ", YouTubeUrlParser.extractVideoId(url))
    }

    @Test
    fun testInvalidUrl() {
        assertNull(YouTubeUrlParser.extractVideoId("not a url"))
        assertNull(YouTubeUrlParser.extractVideoId(""))
        assertNull(YouTubeUrlParser.extractVideoId(null))
        assertNull(YouTubeUrlParser.extractVideoId("https://vimeo.com/12345"))
    }
}
