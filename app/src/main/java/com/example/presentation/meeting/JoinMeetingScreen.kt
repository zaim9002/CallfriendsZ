package com.example.presentation.meeting

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.core.AudioRoute
import com.example.presentation.auth.outlinedFieldColors
import com.example.ui.theme.BrandBackground
import com.example.ui.theme.BrandBorder
import com.example.ui.theme.BrandDanger
import com.example.ui.theme.BrandPrimary
import com.example.ui.theme.BrandSuccess
import com.example.ui.theme.BrandSurface
import com.example.ui.theme.BrandSurfaceElevated
import com.example.ui.theme.BrandSurfaceVariant
import com.example.ui.theme.BrandTextPrimary
import com.example.ui.theme.BrandTextSecondary
import com.example.ui.theme.BrandTextTertiary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JoinMeetingScreen(
    viewModel: JoinMeetingViewModel,
    onNavigateBack: () -> Unit,
    onJoinSuccess: (meetingId: String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var audioDropdownExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.join_meeting_title),
                        fontWeight = FontWeight.Bold,
                        color = BrandTextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = BrandTextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BrandBackground)
            )
        },
        containerColor = BrandBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Meeting Code & Passcode Inputs
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = BrandSurface),
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(BrandBorder, BrandBorder)))
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = uiState.meetingCodeInput,
                        onValueChange = viewModel::onCodeChanged,
                        label = { Text(stringResource(R.string.enter_code_hint)) },
                        leadingIcon = { Icon(Icons.Default.Keyboard, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("join_code_input"),
                        singleLine = true,
                        colors = outlinedFieldColors()
                    )

                    OutlinedTextField(
                        value = uiState.passcodeInput,
                        onValueChange = viewModel::onPasscodeChanged,
                        label = { Text(stringResource(R.string.enter_password_hint)) },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("join_passcode_input"),
                        singleLine = true,
                        colors = outlinedFieldColors()
                    )

                    OutlinedTextField(
                        value = uiState.displayNameInput,
                        onValueChange = viewModel::onDisplayNameChanged,
                        label = { Text(stringResource(R.string.your_name)) },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = outlinedFieldColors()
                    )
                }
            }

            // Audio & Video Preview Box
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = BrandSurface),
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(BrandBorder, BrandBorder)))
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        text = stringResource(R.string.preview_title),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = BrandTextPrimary
                    )

                    // Video Camera Preview Frame
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(BrandBackground)
                            .border(1.dp, BrandBorder, RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (uiState.isCameraEnabled) {
                            // Simulated Camera Feed Surface
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(Color(0xFF1E293B), Color(0xFF0F172A))
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(
                                        modifier = Modifier
                                            .size(72.dp)
                                            .clip(CircleShape)
                                            .background(BrandSurfaceElevated),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = uiState.displayNameInput.take(2).uppercase().ifBlank { "ME" },
                                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                            color = BrandTextPrimary
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = if (uiState.isFrontCamera) "Front Camera Active" else "Rear Camera Active",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = BrandTextSecondary
                                    )
                                }
                            }
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.VideocamOff,
                                    contentDescription = null,
                                    tint = BrandTextTertiary,
                                    modifier = Modifier.size(40.dp)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Camera is turned off",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = BrandTextTertiary
                                )
                            }
                        }

                        // Floating In-Preview Controls
                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            FloatingActionButton(
                                onClick = viewModel::toggleMic,
                                modifier = Modifier.size(44.dp),
                                shape = CircleShape,
                                containerColor = if (uiState.isMicEnabled) BrandSurfaceElevated else BrandDanger
                            ) {
                                Icon(
                                    imageVector = if (uiState.isMicEnabled) Icons.Default.Mic else Icons.Default.MicOff,
                                    contentDescription = "Toggle Mic",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            FloatingActionButton(
                                onClick = viewModel::toggleCamera,
                                modifier = Modifier.size(44.dp),
                                shape = CircleShape,
                                containerColor = if (uiState.isCameraEnabled) BrandSurfaceElevated else BrandDanger
                            ) {
                                Icon(
                                    imageVector = if (uiState.isCameraEnabled) Icons.Default.Videocam else Icons.Default.VideocamOff,
                                    contentDescription = "Toggle Camera",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            if (uiState.isCameraEnabled) {
                                FloatingActionButton(
                                    onClick = viewModel::switchCamera,
                                    modifier = Modifier.size(44.dp),
                                    shape = CircleShape,
                                    containerColor = BrandSurfaceElevated
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Cameraswitch,
                                        contentDescription = "Switch Camera",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Microphone Level Test
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = stringResource(R.string.test_audio),
                                style = MaterialTheme.typography.bodySmall,
                                color = BrandTextSecondary
                            )
                            Text(
                                text = if (uiState.isMicEnabled) "Speaking Level" else "Muted",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (uiState.isMicEnabled) BrandSuccess else BrandDanger
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        val animatedLevel by animateFloatAsState(targetValue = uiState.localAudioLevel, label = "audio_level")
                        LinearProgressIndicator(
                            progress = { animatedLevel },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = BrandSuccess,
                            trackColor = BrandSurfaceVariant
                        )
                    }

                    // Audio Output Selection Dropdown
                    ExposedDropdownMenuBox(
                        expanded = audioDropdownExpanded,
                        onExpandedChange = { audioDropdownExpanded = it },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = when (uiState.audioRoute) {
                                AudioRoute.SPEAKER -> stringResource(R.string.route_speaker)
                                AudioRoute.EARPIECE -> stringResource(R.string.route_earpiece)
                                AudioRoute.BLUETOOTH -> stringResource(R.string.route_bluetooth)
                                AudioRoute.WIRED -> stringResource(R.string.route_wired)
                            },
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(stringResource(R.string.audio_route)) },
                            leadingIcon = {
                                Icon(
                                    imageVector = when (uiState.audioRoute) {
                                        AudioRoute.SPEAKER -> Icons.AutoMirrored.Filled.VolumeUp
                                        AudioRoute.EARPIECE -> Icons.Default.PhoneAndroid
                                        AudioRoute.BLUETOOTH, AudioRoute.WIRED -> Icons.Default.Headphones
                                    },
                                    contentDescription = null,
                                    tint = BrandPrimary
                                )
                            },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = audioDropdownExpanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
                            colors = outlinedFieldColors()
                        )

                        ExposedDropdownMenu(
                            expanded = audioDropdownExpanded,
                            onDismissRequest = { audioDropdownExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.route_speaker)) },
                                onClick = {
                                    viewModel.setAudioRoute(AudioRoute.SPEAKER)
                                    audioDropdownExpanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.route_earpiece)) },
                                onClick = {
                                    viewModel.setAudioRoute(AudioRoute.EARPIECE)
                                    audioDropdownExpanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.route_bluetooth)) },
                                onClick = {
                                    viewModel.setAudioRoute(AudioRoute.BLUETOOTH)
                                    audioDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            if (uiState.errorMessage != null) {
                Text(
                    text = uiState.errorMessage!!,
                    color = BrandDanger,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }

            // Join Meeting Action Button
            Button(
                onClick = {
                    viewModel.verifyAndJoin { meetingId ->
                        onJoinSuccess(meetingId)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("join_submit_btn"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                enabled = !uiState.isVerifying
            ) {
                if (uiState.isVerifying) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp))
                } else {
                    Text(
                        text = stringResource(R.string.join_now),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
