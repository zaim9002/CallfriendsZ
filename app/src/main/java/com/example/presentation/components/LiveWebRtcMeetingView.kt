package com.example.presentation.components

import android.annotation.SuppressLint
import android.net.Uri
import android.view.ViewGroup
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ui.theme.BrandBackground
import com.example.ui.theme.BrandPrimary

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun LiveWebRtcMeetingView(
    roomUrl: String,
    displayName: String,
    isAudioMuted: Boolean,
    isVideoMuted: Boolean,
    modifier: Modifier = Modifier
) {
    var isLoading by remember { mutableStateOf(true) }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }

    val fullUrl = remember(roomUrl, displayName, isAudioMuted, isVideoMuted) {
        buildString {
            append(roomUrl)
            append("#userInfo.displayName=\"${Uri.encode(displayName)}\"")
            append("&config.startWithAudioMuted=$isAudioMuted")
            append("&config.startWithVideoMuted=$isVideoMuted")
            append("&config.prejoinConfig.enabled=false")
            append("&config.disableDeepLinking=true")
            append("&config.enableWelcomePage=false")
            append("&config.p2p.enabled=true")
            append("&interfaceConfig.SHOW_JITSI_WATERMARK=false")
            append("&interfaceConfig.SHOW_WATERMARK_FOR_GUESTS=false")
            append("&interfaceConfig.TOOLBAR_BUTTONS=[\"microphone\",\"camera\",\"tileview\",\"chat\",\"raisehand\",\"hangup\",\"desktop\"]")
        }
    }

    Box(modifier = modifier.fillMaxSize().background(BrandBackground)) {
        AndroidView(
            factory = { ctx ->
                WebView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        databaseEnabled = true
                        mediaPlaybackRequiresUserGesture = false
                        allowFileAccess = true
                        allowContentAccess = true
                        useWideViewPort = true
                        loadWithOverviewMode = true
                        cacheMode = WebSettings.LOAD_DEFAULT
                    }
                    webChromeClient = object : WebChromeClient() {
                        override fun onPermissionRequest(request: PermissionRequest?) {
                            request?.grant(request.resources)
                        }
                    }
                    webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            isLoading = false
                        }
                    }
                    webViewRef = this
                    loadUrl(fullUrl)
                }
            },
            update = { view ->
                // Keep view active
            },
            modifier = Modifier.fillMaxSize()
        )

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().background(BrandBackground),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = BrandPrimary)
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            try {
                webViewRef?.apply {
                    loadUrl("about:blank")
                    stopLoading()
                    destroy()
                }
            } catch (e: Exception) {
                // safely ignored
            }
        }
    }
}
