package com.example.presentation.auth

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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.VideoCall
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.ui.theme.BrandBackground
import com.example.ui.theme.BrandBorder
import com.example.ui.theme.BrandDanger
import com.example.ui.theme.BrandPrimary
import com.example.ui.theme.BrandSurface
import com.example.ui.theme.BrandSurfaceElevated
import com.example.ui.theme.BrandTextPrimary
import com.example.ui.theme.BrandTextSecondary

@Composable
fun AuthScreen(
    viewModel: AuthViewModel,
    pendingMeetingId: String? = null,
    onAuthSuccess: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onAuthSuccess()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BrandBackground)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .widthIn(max = 480.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = BrandSurface),
            border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(BrandBorder, BrandBorder)))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(BrandPrimary, Color(0xFF1D4ED8)))),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.VideoCall,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = if (uiState.isSignUpMode) stringResource(R.string.auth_signup) else stringResource(R.string.auth_login),
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = BrandTextPrimary
                )

                Text(
                    text = stringResource(R.string.tagline),
                    style = MaterialTheme.typography.bodySmall,
                    color = BrandTextSecondary
                )

                // If user entered via a shared meeting link
                if (!pendingMeetingId.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = BrandPrimary.copy(alpha = 0.12f)),
                        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(BrandPrimary, BrandPrimary)))
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "دعوة انضمام لاجتماع",
                                fontWeight = FontWeight.Bold,
                                color = BrandPrimary,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "سجل بحساب Google أو بريدك الإلكتروني للدخول فوراً للاجتماع:\n$pendingMeetingId",
                                style = MaterialTheme.typography.bodySmall,
                                color = BrandTextPrimary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Sign In with Google Button
                Button(
                    onClick = viewModel::loginWithGoogle,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("auth_google_btn"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color(0xFF1F1F1F)
                    ),
                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(Color(0xFFDADCE0), Color(0xFFDADCE0)))),
                    enabled = !uiState.isLoading
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF4285F4)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("G", color = Color.White, fontWeight = FontWeight.Black, fontSize = 14.sp)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = stringResource(R.string.auth_google_login),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp,
                            color = Color(0xFF1F1F1F)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HorizontalDivider(modifier = Modifier.weight(1f), color = BrandBorder)
                    Text(
                        text = "  أو عبر البريد  ",
                        style = MaterialTheme.typography.labelSmall,
                        color = BrandTextSecondary
                    )
                    HorizontalDivider(modifier = Modifier.weight(1f), color = BrandBorder)
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (uiState.isSignUpMode) {
                    OutlinedTextField(
                        value = uiState.displayName,
                        onValueChange = viewModel::onDisplayNameChanged,
                        label = { Text(stringResource(R.string.your_name)) },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_name_input"),
                        singleLine = true,
                        colors = outlinedFieldColors()
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                }

                OutlinedTextField(
                    value = uiState.email,
                    onValueChange = viewModel::onEmailChanged,
                    label = { Text(stringResource(R.string.auth_email_hint)) },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("auth_email_input"),
                    singleLine = true,
                    colors = outlinedFieldColors()
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = uiState.password,
                    onValueChange = viewModel::onPasswordChanged,
                    label = { Text(stringResource(R.string.auth_password_hint)) },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("auth_password_input"),
                    singleLine = true,
                    colors = outlinedFieldColors()
                )

                if (uiState.errorMessage != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = uiState.errorMessage!!,
                        color = BrandDanger,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = viewModel::submitAuth,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("auth_submit_btn"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                    enabled = !uiState.isLoading
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                    } else {
                        Text(
                            text = if (uiState.isSignUpMode) stringResource(R.string.auth_signup) else stringResource(R.string.auth_login),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = viewModel::loginAsGuest,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("auth_guest_btn"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = BrandTextSecondary),
                    border = ButtonDefaults.outlinedButtonBorder.copy(brush = Brush.linearGradient(listOf(BrandBorder, BrandBorder)))
                ) {
                    Text(stringResource(R.string.auth_continue_guest))
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (uiState.isSignUpMode) "Already have an account?" else "Don't have an account?",
                        color = BrandTextSecondary,
                        style = MaterialTheme.typography.bodySmall
                    )
                    TextButton(onClick = viewModel::toggleAuthMode) {
                        Text(
                            text = if (uiState.isSignUpMode) stringResource(R.string.auth_login) else stringResource(R.string.auth_signup),
                            color = BrandPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun outlinedFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = BrandPrimary,
    unfocusedBorderColor = BrandBorder,
    focusedLabelColor = BrandPrimary,
    unfocusedLabelColor = BrandTextSecondary,
    focusedTextColor = BrandTextPrimary,
    unfocusedTextColor = BrandTextPrimary,
    focusedLeadingIconColor = BrandPrimary,
    unfocusedLeadingIconColor = BrandTextSecondary
)
