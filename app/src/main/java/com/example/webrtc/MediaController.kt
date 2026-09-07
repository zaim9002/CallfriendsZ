package com.example.webrtc

import com.example.core.AudioRoute
import kotlinx.coroutines.flow.StateFlow

interface MediaController {
    fun setAudioEnabled(enabled: Boolean)
    fun setVideoEnabled(enabled: Boolean)
    fun switchCamera()
    fun setAudioRoute(route: AudioRoute)
    fun setScreenSharing(enabled: Boolean)
    fun observeMediaState(): StateFlow<MediaTrackState>
    fun getLocalAudioLevel(): StateFlow<Float> // 0.0f to 1.0f for mic preview
    fun release()
}
