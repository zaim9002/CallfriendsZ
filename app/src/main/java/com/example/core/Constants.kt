package com.example.core

object Constants {
    const val BASE_URL = "https://callfriendsz.app"
    const val JITSI_BASE_URL = "https://meet.jit.si"
    const val MEETING_ROOM_PREFIX = "CallfriendsZ_"
    const val DEEP_LINK_SCHEME = "https"
    const val DEEP_LINK_HOST = "meet.jit.si"
    const val DEEP_LINK_MEETING_PREFIX = "/meeting/"

    // Standard STUN fallbacks for WebRTC ICE negotiation
    val DEFAULT_STUN_SERVERS = listOf(
        "stun:stun.l.google.com:19302",
        "stun:stun1.l.google.com:19302",
        "stun:stun2.l.google.com:19302"
    )

    const val DEFAULT_MAX_PARTICIPANTS = 100
    const val DEFAULT_BITRATE_KBPS = 1500
    const val DATA_SAVER_BITRATE_KBPS = 350
    const val RECONNECT_MAX_RETRIES = 5

    /**
     * Generates a universally accessible, public WebRTC meeting URL that works on ANY browser
     * (Chrome, Safari, Soul Browser, Telegram web, WhatsApp, etc.) without DNS failure.
     */
    fun getPublicMeetingUrl(meetingId: String): String {
        val cleanId = meetingId.replace(Regex("[^a-zA-Z0-9]"), "")
        return "$JITSI_BASE_URL/${MEETING_ROOM_PREFIX}$cleanId"
    }

    /**
     * Extracts meeting ID from any raw code or web URL
     */
    fun extractMeetingId(input: String): String {
        val trimmed = input.trim()
        var extracted = trimmed
        if (extracted.contains("meet.jit.si/")) {
            extracted = extracted.substringAfterLast("meet.jit.si/")
        } else if (extracted.contains("/meeting/")) {
            extracted = extracted.substringAfterLast("/meeting/")
        } else if (extracted.startsWith("http://") || extracted.startsWith("https://")) {
            extracted = extracted.substringAfterLast("/")
        }
        extracted = extracted.substringBefore("?").substringBefore("#")
        return extracted.removePrefix(MEETING_ROOM_PREFIX).ifBlank { trimmed }
    }
}
