package com.nuvio.tv.ui.screens.player

import androidx.media3.common.MimeTypes
import androidx.media3.common.Format
import com.nuvio.tv.data.local.HdrPlaybackCompatibilityMode
import com.nuvio.tv.data.local.PlayerSettings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PlayerMediaSourceFactoryTest {

    @Test
    fun `inferMimeType prefers response content type for manifest urls without extension`() {
        val mimeType = PlayerMediaSourceFactory.inferMimeType(
            url = "https://example.com/playback?id=42",
            filename = null,
            responseHeaders = mapOf("Content-Type" to "application/vnd.apple.mpegurl; charset=UTF-8")
        )

        assertEquals(MimeTypes.APPLICATION_M3U8, mimeType)
    }

    @Test
    fun `inferMimeType uses content disposition filename when content type is missing`() {
        val mimeType = PlayerMediaSourceFactory.inferMimeType(
            url = "https://example.com/download?id=42",
            filename = null,
            responseHeaders = mapOf("Content-Disposition" to "attachment; filename=manifest.mpd")
        )

        assertEquals(MimeTypes.APPLICATION_MPD, mimeType)
    }

    @Test
    fun `inferMimeType ignores generic playlist path without manifest evidence`() {
        val mimeType = PlayerMediaSourceFactory.inferMimeType(
            url = "https://example.com/api/playlist/stream",
            filename = null
        )

        assertNull(mimeType)
    }

    @Test
    fun `inferMimeType recognizes explicit format query values`() {
        val mimeType = PlayerMediaSourceFactory.inferMimeType(
            url = "https://example.com/playback?format=m3u8",
            filename = null
        )

        assertEquals(MimeTypes.APPLICATION_M3U8, mimeType)
    }

    @Test
    fun `normalizeMimeType recognizes redirected matroska file responses`() {
        val mimeType = PlayerMediaSourceFactory.normalizeMimeType("video/x-matroska")

        assertEquals(MimeTypes.VIDEO_MATROSKA, mimeType)
    }

    @Test
    fun `inferMimeType uses filename star content disposition for octet stream responses`() {
        val mimeType = PlayerMediaSourceFactory.inferMimeType(
            url = "https://example.com/extract?id=42",
            filename = null,
            responseHeaders = mapOf(
                "Content-Type" to "application/octet-stream",
                "Content-Disposition" to "attachment; filename*=UTF-8''episode-04.mkv"
            )
        )

        assertEquals(MimeTypes.VIDEO_MATROSKA, mimeType)
    }

    @Test
    fun `isDolbyVisionProfile7 matches dvhe profile 7 codecs`() {
        val format = Format.Builder()
            .setSampleMimeType(MimeTypes.VIDEO_DOLBY_VISION)
            .setCodecs("dvhe.07.06")
            .build()

        assertTrue(isDolbyVisionProfile7(format))
    }

    @Test
    fun `isDolbyVisionProfile7 ignores non profile 7 dolby vision codecs`() {
        val format = Format.Builder()
            .setSampleMimeType(MimeTypes.VIDEO_DOLBY_VISION)
            .setCodecs("dvh1.05.06")
            .build()

        assertFalse(isDolbyVisionProfile7(format))
    }

    @Test
    fun `isHdrVideo treats dolby vision as hdr`() {
        val format = Format.Builder()
            .setSampleMimeType(MimeTypes.VIDEO_DOLBY_VISION)
            .build()

        assertTrue(isHdrVideo(format))
    }

    @Test
    fun `shouldForceDolbyAudioCompatibility enables pcm for dolby vision when hdr mode is active`() {
        val settings = PlayerSettings(
            hdrPlaybackCompatibilityMode = HdrPlaybackCompatibilityMode.TONE_MAP_HDR_TO_SDR
        )

        assertTrue(
            shouldForceDolbyAudioCompatibility(
                settings = settings,
                currentVideoIsDolbyVision = true
            )
        )
    }

    @Test
    fun `shouldForceDolbyAudioCompatibility keeps normal audio path for non dolby hdr content`() {
        val settings = PlayerSettings(
            hdrPlaybackCompatibilityMode = HdrPlaybackCompatibilityMode.TONE_MAP_HDR_TO_SDR
        )

        assertFalse(
            shouldForceDolbyAudioCompatibility(
                settings = settings,
                currentVideoIsDolbyVision = false
            )
        )
    }
}
