package com.example

import android.app.PictureInPictureParams
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Rational
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.core.AppContainer
import com.example.presentation.navigation.NavGraph
import com.example.ui.theme.CallfriendsZTheme

class MainActivity : ComponentActivity() {

    private lateinit var appContainer: AppContainer
    private var initialMeetingId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        appContainer = (application as? CallfriendsZApplication)?.appContainer
            ?: AppContainer.getInstance(applicationContext)

        // Parse Deep Link if opened via URL: https://callfriendsz.app/meeting/{meetingId}
        handleDeepLink(intent)

        setContent {
            CallfriendsZTheme(darkTheme = true) {
                NavGraph(
                    appContainer = appContainer,
                    initialMeetingId = initialMeetingId
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleDeepLink(intent)
    }

    private fun handleDeepLink(intent: Intent?) {
        val data = intent?.data ?: return
        if (data.host == "callfriendsz.app" && data.path?.startsWith("/meeting/") == true) {
            initialMeetingId = data.lastPathSegment
        }
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        // Support Picture-in-Picture on Android O+ (API 26+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                val aspectRatio = Rational(16, 9)
                val params = PictureInPictureParams.Builder()
                    .setAspectRatio(aspectRatio)
                    .build()
                enterPictureInPictureMode(params)
            } catch (_: Exception) {
                // PiP fallback
            }
        }
    }
}
