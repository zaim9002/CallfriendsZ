package com.example.core

object Constants {
    const val BASE_URL = "https://callfriendsz.app"
    const val DEEP_LINK_SCHEME = "https"
    const val DEEP_LINK_HOST = "callfriendsz.app"
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
}
