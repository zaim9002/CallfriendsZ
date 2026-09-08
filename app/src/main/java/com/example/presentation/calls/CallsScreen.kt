package com.example.presentation.calls

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CallMade
import androidx.compose.material.icons.automirrored.filled.CallMissed
import androidx.compose.material.icons.automirrored.filled.CallReceived
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.domain.model.CallDirection
import com.example.domain.model.CallMedium
import com.example.domain.model.CallRecord
import com.example.domain.model.ContactUser
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
fun CallsScreen(
    viewModel: CallsViewModel,
    onStartCall: (meetingId: String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.calls_title),
                        fontWeight = FontWeight.Bold,
                        color = BrandTextPrimary
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BrandBackground)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.toggleNewCallSheet(true) },
                containerColor = BrandPrimary,
                shape = CircleShape,
                modifier = Modifier.testTag("calls_fab")
            ) {
                Icon(
                    imageVector = Icons.Default.Call,
                    contentDescription = "New Call",
                    tint = Color.White
                )
            }
        },
        containerColor = BrandBackground
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            if (uiState.callHistory.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(BrandSurfaceElevated),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Call, contentDescription = null, tint = BrandPrimary, modifier = Modifier.size(32.dp))
                        }
                        Text("لا توجد مكالمات سابقة بعد", color = BrandTextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("ابدأ مكالمتك الأولى أو أنشئ اجتماعاً لمشاركته مع أصدقائك!", color = BrandTextSecondary, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Button(
                            onClick = { viewModel.toggleNewCallSheet(true) },
                            colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(stringResource(R.string.new_call))
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(uiState.callHistory) { call ->
                        CallRecordCard(
                            call = call,
                            onCallback = {
                                viewModel.recordNewCall(call.peerName, call.medium)
                                onStartCall("call-${call.id}")
                            }
                        )
                    }
                }
            }
        }
    }

    if (uiState.showNewCallSheet) {
        NewCallBottomSheet(
            contacts = uiState.contacts,
            searchQuery = uiState.searchQuery,
            onSearchChanged = viewModel::onSearchQueryChanged,
            onDismiss = { viewModel.toggleNewCallSheet(false) },
            onInitiateCall = { contact, medium ->
                viewModel.toggleNewCallSheet(false)
                viewModel.recordNewCall(contact.name, medium)
                onStartCall("call-${contact.id}")
            }
        )
    }
}

@Composable
private fun CallRecordCard(
    call: CallRecord,
    onCallback: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = BrandSurface),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(BrandBorder, BrandBorder)))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(BrandSurfaceElevated),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = call.peerName.take(2).uppercase(),
                        fontWeight = FontWeight.Bold,
                        color = BrandTextPrimary
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = call.peerName,
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = BrandTextPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val icon = when (call.direction) {
                            CallDirection.INCOMING -> Icons.AutoMirrored.Filled.CallReceived
                            CallDirection.OUTGOING -> Icons.AutoMirrored.Filled.CallMade
                            CallDirection.MISSED -> Icons.AutoMirrored.Filled.CallMissed
                        }
                        val tint = when (call.direction) {
                            CallDirection.INCOMING -> BrandSuccess
                            CallDirection.OUTGOING -> BrandPrimary
                            CallDirection.MISSED -> BrandDanger
                        }
                        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${call.direction.name.lowercase().replaceFirstChar { it.uppercase() }} • ${call.medium.name.lowercase()}",
                            style = MaterialTheme.typography.labelSmall,
                            color = BrandTextSecondary
                        )
                    }
                }
            }

            IconButton(onClick = onCallback) {
                Icon(
                    imageVector = if (call.medium == CallMedium.VIDEO) Icons.Default.Videocam else Icons.Default.Call,
                    contentDescription = "Callback",
                    tint = BrandPrimary
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NewCallBottomSheet(
    contacts: List<ContactUser>,
    searchQuery: String,
    onSearchChanged: (String) -> Unit,
    onDismiss: () -> Unit,
    onInitiateCall: (ContactUser, CallMedium) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = BrandSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = stringResource(R.string.new_call),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = BrandTextPrimary
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChanged,
                placeholder = { Text(stringResource(R.string.search_contacts_hint)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = outlinedFieldColors()
            )

            Spacer(modifier = Modifier.height(14.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(contacts) { contact ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(BrandSurfaceVariant)
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(BrandSurfaceElevated),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = contact.name.take(2).uppercase(),
                                    fontWeight = FontWeight.Bold,
                                    color = BrandTextPrimary
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = contact.name,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = BrandTextPrimary
                                )
                                Text(
                                    text = contact.statusText,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = BrandTextSecondary
                                )
                            }
                        }

                        Row {
                            IconButton(onClick = { onInitiateCall(contact, CallMedium.AUDIO) }) {
                                Icon(Icons.Default.Call, contentDescription = "Audio Call", tint = BrandPrimary)
                            }
                            IconButton(onClick = { onInitiateCall(contact, CallMedium.VIDEO) }) {
                                Icon(Icons.Default.Videocam, contentDescription = "Video Call", tint = BrandPrimary)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
