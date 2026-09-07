package com.example.presentation.settings

import androidx.lifecycle.ViewModel
import com.example.core.VideoQuality
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class SettingsUiState(
    val videoQuality: VideoQuality = VideoQuality.AUTO,
    val isDataSaverEnabled: Boolean = false,
    val isEchoCancellationEnabled: Boolean = true,
    val isNoiseSuppressionEnabled: Boolean = true,
    val isLiveCaptionsEnabled: Boolean = false,
    val appLanguage: String = "English", // "العربية" or "English"
    val themeMode: String = "Dark" // "Dark", "Light", "System"
)

class SettingsViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun setVideoQuality(quality: VideoQuality) {
        _uiState.value = _uiState.value.copy(videoQuality = quality)
    }

    fun toggleDataSaver(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(
            isDataSaverEnabled = enabled,
            videoQuality = if (enabled) VideoQuality.P360 else VideoQuality.AUTO
        )
    }

    fun toggleEchoCancellation(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(isEchoCancellationEnabled = enabled)
    }

    fun toggleNoiseSuppression(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(isNoiseSuppressionEnabled = enabled)
    }

    fun toggleLiveCaptions(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(isLiveCaptionsEnabled = enabled)
    }

    fun setLanguage(language: String) {
        _uiState.value = _uiState.value.copy(appLanguage = language)
    }

    fun setThemeMode(mode: String) {
        _uiState.value = _uiState.value.copy(themeMode = mode)
    }
}
