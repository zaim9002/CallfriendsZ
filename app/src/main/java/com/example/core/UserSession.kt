package com.example.core

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

data class CurrentUser(
    val id: String,
    val displayName: String,
    val email: String,
    val avatarUrl: String? = null,
    val status: UserStatus = UserStatus.AVAILABLE,
    val isGuest: Boolean = false
)

object UserSession {
    private val _currentUser = MutableStateFlow<CurrentUser?>(
        CurrentUser(
            id = "usr_" + UUID.randomUUID().toString().take(8),
            displayName = "Alex Morgan",
            email = "alex.morgan@callfriendsz.app",
            avatarUrl = null,
            status = UserStatus.AVAILABLE,
            isGuest = false
        )
    )
    val currentUser: StateFlow<CurrentUser?> = _currentUser.asStateFlow()

    fun updateProfile(name: String, email: String, status: UserStatus) {
        val current = _currentUser.value ?: return
        _currentUser.value = current.copy(
            displayName = name.ifBlank { current.displayName },
            email = email.ifBlank { current.email },
            status = status
        )
    }

    fun setGuest(guestName: String) {
        _currentUser.value = CurrentUser(
            id = "guest_" + UUID.randomUUID().toString().take(6),
            displayName = guestName.ifBlank { "Guest User" },
            email = "guest@callfriendsz.app",
            status = UserStatus.AVAILABLE,
            isGuest = true
        )
    }

    fun logout() {
        _currentUser.value = null
    }

    fun login(email: String, name: String) {
        _currentUser.value = CurrentUser(
            id = "usr_" + UUID.randomUUID().toString().take(8),
            displayName = name.ifBlank { email.substringBefore("@") },
            email = email,
            status = UserStatus.AVAILABLE,
            isGuest = false
        )
    }
}
