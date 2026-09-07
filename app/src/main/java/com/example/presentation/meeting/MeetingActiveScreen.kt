package com.example.presentation.meeting

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Grid3x3
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PanTool
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PresentToAll
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.StopScreenShare
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.core.AudioRoute
import com.example.core.NetworkQuality
import com.example.core.ParticipantRole
import com.example.domain.model.MeetingMessage
import com.example.domain.model.Participant
import com.example.presentation.auth.outlinedFieldColors
import com.example.ui.theme.BrandBackground
import com.example.ui.theme.BrandBorder
import com.example.ui.theme.BrandControlBar
import com.example.ui.theme.BrandDanger
import com.example.ui.theme.BrandPrimary
import com.example.ui.theme.BrandSuccess
import com.example.ui.theme.BrandSurface
import com.example.ui.theme.BrandSurfaceElevated
import com.example.ui.theme.BrandSurfaceVariant
import com.example.ui.theme.BrandTextPrimary
import com.example.ui.theme.BrandTextSecondary
import com.example.ui.theme.BrandTextTertiary
import com.example.ui.theme.BrandWarning

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeetingActiveScreen(
    meetingId: String,
    viewModel: MeetingActiveViewModel,
    onLeaveMeeting: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showMoreMenu by remember { mutableStateOf(false) }
    var showReactionsBar by remember { mutableStateOf(false) }

    LaunchedEffect(meetingId) {
        viewModel.initializeMeeting(meetingId)
    }

    Scaffold(
        containerColor = BrandBackground
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .clickable { viewModel.toggleControlsVisibility() }
                .testTag("meeting_active_screen")
        ) {
            // Main Content: Participant Video Grid
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = if (uiState.isControlsVisible) 90.dp else 16.dp, top = 70.dp)
            ) {
                if (uiState.isWaitingRoom) {
                    WaitingRoomView(
                        meetingTitle = uiState.meeting?.title ?: "Meeting",
                        onLeave = {
                            viewModel.leaveMeeting()
                            onLeaveMeeting()
                        }
                    )
                } else {
                    ParticipantVideoGrid(
                        participants = uiState.participants,
                        isScreenSharing = uiState.mediaState.isScreenSharing
                    )
                }
            }

            // Floating Animated Reaction Popups
            uiState.activeReaction?.let { reaction ->
                FloatingReactionBurst(sender = reaction.first, emoji = reaction.second)
            }

            // Top Control Bar Overlay
            AnimatedVisibility(
                visible = uiState.isControlsVisible,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically(),
                modifier = Modifier.align(Alignment.TopCenter)
            ) {
                MeetingTopBar(
                    meetingTitle = uiState.meeting?.title ?: "Meeting $meetingId",
                    durationSeconds = uiState.durationSeconds,
                    quality = uiState.connectionQuality,
                    isRecording = uiState.isRecording,
                    onLeaveClick = {
                        viewModel.leaveMeeting()
                        onLeaveMeeting()
                    }
                )
            }

            // Bottom In-Meeting Controls Bar Overlay
            AnimatedVisibility(
                visible = uiState.isControlsVisible && !uiState.isWaitingRoom,
                enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { it }),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Quick Emoji Picker Bar
                    if (showReactionsBar) {
                        QuickReactionsRow(
                            onSelectEmoji = { emoji ->
                                viewModel.sendReaction(emoji)
                                showReactionsBar = false
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    MeetingBottomControlBar(
                        mediaState = uiState.mediaState,
                        isHandRaised = uiState.isHandRaised,
                        onToggleMic = viewModel::toggleMic,
                        onToggleCamera = viewModel::toggleCamera,
                        onSwitchCamera = viewModel::switchCamera,
                        onToggleScreenShare = {
                            viewModel.setScreenSharing(!uiState.mediaState.isScreenSharing)
                        },
                        onToggleHandRaise = viewModel::toggleHandRaise,
                        onOpenChat = { viewModel.openSheet(ActiveSheet.CHAT) },
                        onOpenParticipants = { viewModel.openSheet(ActiveSheet.PARTICIPANTS) },
                        onToggleReactions = { showReactionsBar = !showReactionsBar },
                        onMoreClick = { showMoreMenu = true },
                        onEndCall = {
                            viewModel.leaveMeeting()
                            onLeaveMeeting()
                        }
                    )
                }
            }

            // More Options Dropdown
            DropdownMenu(
                expanded = showMoreMenu,
                onDismissRequest = { showMoreMenu = false },
                modifier = Modifier
                    .background(BrandSurface)
                    .border(1.dp, BrandBorder, RoundedCornerShape(8.dp))
            ) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.host_controls), color = BrandTextPrimary) },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = BrandPrimary) },
                    onClick = {
                        showMoreMenu = false
                        viewModel.openSheet(ActiveSheet.HOST_CONTROLS)
                    }
                )
                DropdownMenuItem(
                    text = { Text("Network Diagnostics", color = BrandTextPrimary) },
                    leadingIcon = { Icon(Icons.Default.Speed, contentDescription = null, tint = BrandSuccess) },
                    onClick = {
                        showMoreMenu = false
                        viewModel.openSheet(ActiveSheet.DIAGNOSTICS)
                    }
                )
            }
        }
    }

    // Modal Bottom Sheets for In-call Chat, Participants, Host Controls
    when (uiState.activeBottomSheet) {
        ActiveSheet.CHAT -> {
            InMeetingChatSheet(
                messages = uiState.messages,
                onDismiss = viewModel::closeSheet,
                onSendMessage = viewModel::sendMessage
            )
        }
        ActiveSheet.PARTICIPANTS -> {
            InMeetingParticipantsSheet(
                participants = uiState.participants,
                joinRequests = uiState.joinRequests,
                onDismiss = viewModel::closeSheet,
                onMuteAll = viewModel::hostMuteAll,
                onRemoveParticipant = viewModel::hostRemoveParticipant,
                onAdmitRequest = viewModel::admitJoinRequest,
                onDenyRequest = viewModel::denyJoinRequest
            )
        }
        ActiveSheet.HOST_CONTROLS -> {
            HostControlsDialog(
                isLocked = uiState.meeting?.hostControls?.isLocked ?: false,
                onLockToggled = viewModel::hostLockMeeting,
                onDismiss = viewModel::closeSheet
            )
        }
        ActiveSheet.DIAGNOSTICS -> {
            DiagnosticsDialog(
                stats = uiState.stats,
                onDismiss = viewModel::closeSheet
            )
        }
        ActiveSheet.MORE_OPTIONS -> {}
        null -> {}
    }
}

@Composable
private fun MeetingTopBar(
    meetingTitle: String,
    durationSeconds: Long,
    quality: NetworkQuality,
    isRecording: Boolean,
    onLeaveClick: () -> Unit
) {
    val minutes = durationSeconds / 60
    val secs = durationSeconds % 60
    val durationText = "%02d:%02d".format(minutes, secs)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = meetingTitle,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = BrandTextPrimary
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = durationText,
                    style = MaterialTheme.typography.bodySmall,
                    color = BrandTextSecondary
                )
                Spacer(modifier = Modifier.width(8.dp))
                // Quality Indicator
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(
                            when (quality) {
                                NetworkQuality.EXCELLENT -> BrandSuccess
                                NetworkQuality.GOOD -> BrandPrimary
                                NetworkQuality.POOR -> BrandWarning
                                NetworkQuality.RECONNECTING -> BrandDanger
                            }
                        )
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = when (quality) {
                        NetworkQuality.EXCELLENT -> "HD • Excellent"
                        NetworkQuality.GOOD -> "Good"
                        NetworkQuality.POOR -> "Poor"
                        NetworkQuality.RECONNECTING -> "Reconnecting..."
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = BrandTextTertiary
                )
                if (isRecording) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.FiberManualRecord,
                        contentDescription = "REC",
                        tint = BrandDanger,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(text = "REC", color = BrandDanger, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Leave Button
        Button(
            onClick = onLeaveClick,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BrandDanger)
        ) {
            Icon(Icons.Default.CallEnd, contentDescription = "Leave", modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(stringResource(R.string.leave_meeting), fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
    }
}

@Composable
private fun ParticipantVideoGrid(
    participants: List<Participant>,
    isScreenSharing: Boolean
) {
    if (participants.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Connecting to secure room...", color = BrandTextSecondary)
        }
        return
    }

    if (isScreenSharing) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {
            // Screen share main panel
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.7f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(BrandSurface)
                    .border(1.dp, BrandPrimary, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.PresentToAll,
                        contentDescription = null,
                        tint = BrandPrimary,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "You are sharing your screen",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = BrandTextPrimary
                    )
                    Text(
                        text = "Everything on your display is visible to participants",
                        style = MaterialTheme.typography.bodySmall,
                        color = BrandTextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Horizontal Strip for attendees
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.3f),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(participants) { participant ->
                    Box(modifier = Modifier.width(160.dp)) {
                        ParticipantTile(participant = participant)
                    }
                }
            }
        }
    } else {
        // Standard Grid based on participant count
        when (participants.size) {
            1 -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                ) {
                    ParticipantTile(participant = participants[0])
                }
            }
            2 -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        ParticipantTile(participant = participants[0])
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        ParticipantTile(participant = participants[1])
                    }
                }
            }
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val half = (participants.size + 1) / 2
                    val firstRow = participants.take(half)
                    val secondRow = participants.drop(half)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        firstRow.forEach { p ->
                            Box(modifier = Modifier.weight(1f)) {
                                ParticipantTile(participant = p)
                            }
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        secondRow.forEach { p ->
                            Box(modifier = Modifier.weight(1f)) {
                                ParticipantTile(participant = p)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ParticipantTile(participant: Participant) {
    val borderColor = if (participant.isSpeaking) BrandSuccess else BrandBorder
    val borderWidth = if (participant.isSpeaking) 2.dp else 1.dp

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(16.dp))
            .background(BrandSurfaceVariant)
            .border(borderWidth, borderColor, RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center
    ) {
        if (participant.isVideoEnabled) {
            // Simulated Active Camera Feed
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF1B2433), Color(0xFF0F1722))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Subtle video silhouette
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(BrandSurfaceElevated),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = participant.displayName.take(2).uppercase(),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = BrandTextPrimary
                    )
                }
            }
        } else {
            // Avatar Placeholder
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(BrandSurfaceElevated),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = participant.displayName.take(2).uppercase(),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = BrandTextPrimary
                    )
                }
            }
        }

        // Overlay Badges: Name Tag & Role
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(8.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xCC0B0F14))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = participant.displayName,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                color = Color.White
            )
        }

        // Mic Status & Hand Raised in Top End
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (participant.isHandRaised) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(BrandWarning),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PanTool,
                        contentDescription = "Hand Raised",
                        tint = Color.Black,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            if (!participant.isAudioEnabled) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(BrandDanger),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MicOff,
                        contentDescription = "Muted",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun MeetingBottomControlBar(
    mediaState: com.example.webrtc.MediaTrackState,
    isHandRaised: Boolean,
    onToggleMic: () -> Unit,
    onToggleCamera: () -> Unit,
    onSwitchCamera: () -> Unit,
    onToggleScreenShare: () -> Unit,
    onToggleHandRaise: () -> Unit,
    onOpenChat: () -> Unit,
    onOpenParticipants: () -> Unit,
    onToggleReactions: () -> Unit,
    onMoreClick: () -> Unit,
    onEndCall: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 10.dp),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = BrandControlBar),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(BrandBorder, BrandBorder)))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Mic
            ControlCircleButton(
                icon = if (!mediaState.isAudioMuted) Icons.Default.Mic else Icons.Default.MicOff,
                isActive = !mediaState.isAudioMuted,
                activeColor = BrandSurfaceElevated,
                inactiveColor = BrandDanger,
                onClick = onToggleMic
            )

            // Video
            ControlCircleButton(
                icon = if (!mediaState.isVideoMuted) Icons.Default.Videocam else Icons.Default.VideocamOff,
                isActive = !mediaState.isVideoMuted,
                activeColor = BrandSurfaceElevated,
                inactiveColor = BrandDanger,
                onClick = onToggleCamera
            )

            // Switch Camera
            if (!mediaState.isVideoMuted) {
                ControlCircleButton(
                    icon = Icons.Default.Cameraswitch,
                    isActive = true,
                    activeColor = BrandSurfaceElevated,
                    inactiveColor = BrandSurfaceElevated,
                    onClick = onSwitchCamera
                )
            }

            // Hand Raise
            ControlCircleButton(
                icon = Icons.Default.PanTool,
                isActive = isHandRaised,
                activeColor = BrandWarning,
                inactiveColor = BrandSurfaceElevated,
                onClick = onToggleHandRaise
            )

            // Screen Share
            ControlCircleButton(
                icon = if (mediaState.isScreenSharing) Icons.Default.StopScreenShare else Icons.Default.PresentToAll,
                isActive = mediaState.isScreenSharing,
                activeColor = BrandPrimary,
                inactiveColor = BrandSurfaceElevated,
                onClick = onToggleScreenShare
            )

            // Chat
            ControlCircleButton(
                icon = Icons.Default.Chat,
                isActive = false,
                activeColor = BrandSurfaceElevated,
                inactiveColor = BrandSurfaceElevated,
                onClick = onOpenChat
            )

            // Participants
            ControlCircleButton(
                icon = Icons.Default.Group,
                isActive = false,
                activeColor = BrandSurfaceElevated,
                inactiveColor = BrandSurfaceElevated,
                onClick = onOpenParticipants
            )

            // More / Popover
            ControlCircleButton(
                icon = Icons.Default.MoreVert,
                isActive = false,
                activeColor = BrandSurfaceElevated,
                inactiveColor = BrandSurfaceElevated,
                onClick = onMoreClick
            )

            // End Call Button
            FloatingActionButton(
                onClick = onEndCall,
                modifier = Modifier.size(46.dp),
                shape = CircleShape,
                containerColor = BrandDanger
            ) {
                Icon(Icons.Default.CallEnd, contentDescription = "End Call", tint = Color.White, modifier = Modifier.size(22.dp))
            }
        }
    }
}

@Composable
private fun ControlCircleButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isActive: Boolean,
    activeColor: Color,
    inactiveColor: Color,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(if (isActive) activeColor else inactiveColor)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun QuickReactionsRow(
    onSelectEmoji: (String) -> Unit
) {
    val emojis = listOf("👍", "❤️", "😂", "👏", "🎉", "😮", "😢")
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = BrandSurfaceElevated),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(BrandBorder, BrandBorder)))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            emojis.forEach { emoji ->
                Text(
                    text = emoji,
                    fontSize = 24.sp,
                    modifier = Modifier
                        .clickable { onSelectEmoji(emoji) }
                        .padding(4.dp)
                )
            }
        }
    }
}

@Composable
private fun FloatingReactionBurst(sender: String, emoji: String) {
    val yOffset = remember { Animatable(0f) }

    LaunchedEffect(sender, emoji) {
        yOffset.snapTo(0f)
        yOffset.animateTo(-120f, animationSpec = tween(2200))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 120.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Card(
            modifier = Modifier.offset { IntOffset(0, yOffset.value.toInt()) },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = BrandSurfaceElevated)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = emoji, fontSize = 28.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = sender,
                    color = BrandTextPrimary,
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

@Composable
private fun WaitingRoomView(
    meetingTitle: String,
    onLeave: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = BrandSurface),
            border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(BrandBorder, BrandBorder)))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = stringResource(R.string.waiting_room_title),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = BrandTextPrimary
                )
                Text(
                    text = stringResource(R.string.waiting_room_desc),
                    style = MaterialTheme.typography.bodyMedium,
                    color = BrandTextSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onLeave,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandDanger)
                ) {
                    Text(stringResource(R.string.leave_meeting))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InMeetingChatSheet(
    messages: List<MeetingMessage>,
    onDismiss: () -> Unit,
    onSendMessage: (String, String?) -> Unit
) {
    var textInput by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = BrandSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.7f)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = stringResource(R.string.chat_title),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = BrandTextPrimary,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(messages) { msg ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(BrandSurfaceVariant)
                            .padding(10.dp)
                    ) {
                        Text(
                            text = msg.senderName,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = BrandPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = msg.text,
                            style = MaterialTheme.typography.bodyMedium,
                            color = BrandTextPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = textInput,
                    onValueChange = { textInput = it },
                    placeholder = { Text(stringResource(R.string.type_message)) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = outlinedFieldColors()
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        if (textInput.isNotBlank()) {
                            onSendMessage(textInput, null)
                            textInput = ""
                        }
                    },
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(BrandPrimary)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = Color.White
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InMeetingParticipantsSheet(
    participants: List<Participant>,
    joinRequests: List<com.example.domain.model.JoinRequest>,
    onDismiss: () -> Unit,
    onMuteAll: () -> Unit,
    onRemoveParticipant: (String) -> Unit,
    onAdmitRequest: (String) -> Unit,
    onDenyRequest: (String) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = BrandSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.7f)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${stringResource(R.string.participants)} (${participants.size})",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = BrandTextPrimary
                )

                Button(
                    onClick = onMuteAll,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandSurfaceVariant)
                ) {
                    Text(stringResource(R.string.mute_all), color = BrandDanger, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Waiting Room Join Requests
            if (joinRequests.isNotEmpty()) {
                Text(
                    text = "Waiting Room Requests",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = BrandWarning
                )
                Spacer(modifier = Modifier.height(6.dp))
                joinRequests.forEach { req ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(BrandSurfaceVariant)
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = req.displayName, color = BrandTextPrimary)
                        Row {
                            IconButton(onClick = { onAdmitRequest(req.id) }) {
                                Icon(Icons.Default.Check, contentDescription = "Admit", tint = BrandSuccess)
                            }
                            IconButton(onClick = { onDenyRequest(req.id) }) {
                                Icon(Icons.Default.Close, contentDescription = "Deny", tint = BrandDanger)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(participants) { p ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(BrandSurfaceVariant)
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = p.displayName,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = BrandTextPrimary
                            )
                            Text(
                                text = p.role.name,
                                style = MaterialTheme.typography.labelSmall,
                                color = BrandTextSecondary
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (p.role != ParticipantRole.HOST) {
                                IconButton(onClick = { onRemoveParticipant(p.id) }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Remove",
                                        tint = BrandDanger,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun HostControlsDialog(
    isLocked: Boolean,
    onLockToggled: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.host_controls), color = BrandTextPrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Control meeting access and security settings.",
                    color = BrandTextSecondary,
                    style = MaterialTheme.typography.bodySmall
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(R.string.lock_meeting), color = BrandTextPrimary)
                    Button(
                        onClick = { onLockToggled(!isLocked) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isLocked) BrandDanger else BrandSurfaceVariant
                        )
                    ) {
                        Text(if (isLocked) "Locked" else "Unlocked")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = BrandPrimary)
            }
        },
        containerColor = BrandSurface
    )
}

@Composable
private fun DiagnosticsDialog(
    stats: com.example.webrtc.RtcStatsReport,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Network Diagnostics", color = BrandTextPrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                DiagnosticItem(label = "RTT (Round Trip)", value = "${stats.rttMs} ms")
                DiagnosticItem(label = "Jitter", value = "${stats.jitterMs} ms")
                DiagnosticItem(label = "Packet Loss", value = "${stats.packetLossPercent}%")
                DiagnosticItem(label = "Target Bitrate", value = "${stats.bitrateKbps} kbps")
                DiagnosticItem(label = "Connection Quality", value = stats.quality.name)
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done", color = BrandPrimary)
            }
        },
        containerColor = BrandSurface
    )
}

@Composable
private fun DiagnosticItem(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = BrandTextSecondary, style = MaterialTheme.typography.bodySmall)
        Text(text = value, color = BrandTextPrimary, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
    }
}
