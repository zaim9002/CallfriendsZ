package com.example.network

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class FeatureFlags(
    val maintenanceMode: Boolean = false,
    val registrationEnabled: Boolean = true,
    val videoCallsEnabled: Boolean = true,
    val screenShareEnabled: Boolean = true,
    val recordingEnabled: Boolean = true,
    val chatEnabled: Boolean = true,
    val maxParticipants: Int = 100,
    val minimumAppVersion: String = "1.0.0",
    val forceUpdate: Boolean = false,
    val announcement: String? = null
)

object RemoteConfigManager {
    private val _flags = MutableStateFlow(FeatureFlags())
    val flags: StateFlow<FeatureFlags> = _flags.asStateFlow()

    fun updateFlag(update: (FeatureFlags) -> FeatureFlags) {
        _flags.value = update(_flags.value)
    }
}
