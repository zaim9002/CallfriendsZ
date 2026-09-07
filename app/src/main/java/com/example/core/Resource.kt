package com.example.core

sealed class Resource<out T> {
    data class Success<out T>(val data: T) : Resource<T>()
    data class Error(val message: String, val cause: Throwable? = null) : Resource<Nothing>()
    object Loading : Resource<Nothing>()

    val isSuccess: Boolean get() = this is Success
    val isError: Boolean get() = this is Error
    val isLoading: Boolean get() = this is Loading

    fun getOrNull(): T? = (this as? Success)?.data
}

enum class AudioRoute {
    SPEAKER,
    EARPIECE,
    BLUETOOTH,
    WIRED
}

enum class NetworkQuality {
    EXCELLENT,
    GOOD,
    POOR,
    RECONNECTING
}

enum class MeetingAccessType {
    ANYONE_WITH_LINK,
    ASK_TO_JOIN, // Waiting Room
    INVITED_ONLY
}

enum class ParticipantRole {
    HOST,
    CO_HOST,
    PARTICIPANT,
    GUEST
}

enum class UserStatus {
    AVAILABLE,
    BUSY,
    AWAY,
    DO_NOT_DISTURB
}

enum class VideoQuality(val label: String, val width: Int, val height: Int, val targetFps: Int) {
    AUTO("Auto", 1280, 720, 30),
    P360("360p", 640, 360, 24),
    P480("480p", 854, 480, 24),
    P720("720p HD", 1280, 720, 30),
    P1080("1080p FHD", 1920, 1080, 30)
}
