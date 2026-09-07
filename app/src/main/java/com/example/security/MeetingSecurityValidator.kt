package com.example.security

import java.security.MessageDigest
import java.util.UUID

object MeetingSecurityValidator {

    fun generateSecureMeetingId(): String {
        val part1 = UUID.randomUUID().toString().take(3)
        val part2 = UUID.randomUUID().toString().take(4)
        val part3 = UUID.randomUUID().toString().take(3)
        return "cfz-$part1-$part2"
    }

    fun hashPasscode(passcode: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(passcode.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun verifyPasscode(input: String, expectedHash: String?): Boolean {
        if (expectedHash.isNullOrBlank()) return true
        return hashPasscode(input) == expectedHash
    }

    fun sanitizeMeetingCode(input: String): String {
        val trimmed = input.trim()
        return if (trimmed.startsWith("https://") || trimmed.startsWith("http://")) {
            trimmed.substringAfterLast("/")
        } else {
            trimmed
        }
    }
}
