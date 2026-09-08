package com.example.presentation.contacts

import android.content.Intent
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.core.Constants
import com.example.domain.model.CallMedium
import com.example.domain.model.ContactUser
import com.example.presentation.auth.outlinedFieldColors
import com.example.presentation.calls.CallsViewModel
import com.example.security.MeetingSecurityValidator
import com.example.ui.theme.BrandBackground
import com.example.ui.theme.BrandBorder
import com.example.ui.theme.BrandDanger
import com.example.ui.theme.BrandPrimary
import com.example.ui.theme.BrandSuccess
import com.example.ui.theme.BrandSurface
import com.example.ui.theme.BrandSurfaceElevated
import com.example.ui.theme.BrandTextPrimary
import com.example.ui.theme.BrandTextSecondary
import com.example.ui.theme.BrandTextTertiary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactsScreen(
    viewModel: CallsViewModel,
    onStartCall: (meetingId: String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var showAddDialog by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }
    var newEmail by remember { mutableStateOf("") }
    var newPhoneOrNote by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.contacts_title),
                        fontWeight = FontWeight.Bold,
                        color = BrandTextPrimary
                    )
                },
                actions = {
                    IconButton(
                        onClick = { showAddDialog = true },
                        modifier = Modifier.testTag("btn_add_contact_top")
                    ) {
                        Icon(
                            Icons.Default.PersonAdd,
                            contentDescription = stringResource(R.string.add_contact),
                            tint = BrandPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BrandBackground)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = BrandPrimary,
                shape = CircleShape,
                modifier = Modifier.testTag("fab_add_contact")
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = stringResource(R.string.add_contact),
                    tint = Color.White
                )
            }
        },
        containerColor = BrandBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = viewModel::onSearchQueryChanged,
                placeholder = { Text(stringResource(R.string.search_contacts_hint)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("contacts_search_input"),
                shape = RoundedCornerShape(14.dp),
                colors = outlinedFieldColors()
            )

            if (uiState.contacts.isEmpty()) {
                // Friendly empty state with clear invitation guidance
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 60.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(BrandSurfaceElevated),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.PersonAdd,
                                contentDescription = null,
                                tint = BrandPrimary,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        Text(
                            text = stringResource(R.string.no_contacts_title),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = BrandTextPrimary,
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = stringResource(R.string.no_contacts_desc),
                            style = MaterialTheme.typography.bodyMedium,
                            color = BrandTextSecondary,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Button(
                            onClick = { showAddDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(stringResource(R.string.add_contact))
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(uiState.contacts, key = { it.id }) { contact ->
                        ContactRow(
                            contact = contact,
                            onAudioCall = {
                                viewModel.recordNewCall(contact.name, CallMedium.AUDIO)
                                onStartCall("call-${contact.id}")
                            },
                            onVideoCall = {
                                viewModel.recordNewCall(contact.name, CallMedium.VIDEO)
                                onStartCall("call-${contact.id}")
                            },
                            onInvite = {
                                val newMeetingId = MeetingSecurityValidator.generateSecureMeetingId()
                                val meetingUrl = Constants.getPublicMeetingUrl(newMeetingId)
                                val inviteMsg = "مرحباً ${contact.name}، أدعوك للانضمام إلى مكالمة فيديو في تطبيق CallfriendsZ Meet:\n$meetingUrl\nرمز الاجتماع: $newMeetingId"
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, inviteMsg)
                                    type = "text/plain"
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "مشاركة رابط الاجتماع مع ${contact.name}"))
                            },
                            onDelete = {
                                viewModel.deleteContact(contact.id)
                            }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = {
                Text(
                    text = stringResource(R.string.add_contact),
                    fontWeight = FontWeight.Bold,
                    color = BrandTextPrimary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text(stringResource(R.string.contact_name)) },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_new_contact_name"),
                        singleLine = true,
                        colors = outlinedFieldColors()
                    )

                    OutlinedTextField(
                        value = newEmail,
                        onValueChange = { newEmail = it },
                        label = { Text(stringResource(R.string.contact_email)) },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_new_contact_email"),
                        singleLine = true,
                        colors = outlinedFieldColors()
                    )

                    OutlinedTextField(
                        value = newPhoneOrNote,
                        onValueChange = { newPhoneOrNote = it },
                        label = { Text(stringResource(R.string.contact_phone)) },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = outlinedFieldColors()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newName.isNotBlank()) {
                            viewModel.addContact(newName, newEmail, newPhoneOrNote)
                            newName = ""
                            newEmail = ""
                            newPhoneOrNote = ""
                            showAddDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                    enabled = newName.isNotBlank()
                ) {
                    Text(stringResource(R.string.save_contact))
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text(stringResource(R.string.cancel), color = BrandTextSecondary)
                }
            },
            containerColor = BrandSurface
        )
    }
}

@Composable
private fun ContactRow(
    contact: ContactUser,
    onAudioCall: () -> Unit,
    onVideoCall: () -> Unit,
    onInvite: () -> Unit,
    onDelete: () -> Unit
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
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
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

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = contact.name,
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = BrandTextPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    if (contact.email.isNotBlank()) {
                        Text(
                            text = contact.email,
                            style = MaterialTheme.typography.bodySmall,
                            color = BrandTextSecondary
                        )
                    }
                    if (contact.statusText.isNotBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(if (contact.isOnline) BrandSuccess else BrandTextTertiary)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = contact.statusText,
                                style = MaterialTheme.typography.labelSmall,
                                color = BrandTextSecondary
                            )
                        }
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                IconButton(onClick = onInvite) {
                    Icon(Icons.Default.Share, contentDescription = "Share Link", tint = BrandTextSecondary, modifier = Modifier.size(20.dp))
                }
                IconButton(onClick = onVideoCall) {
                    Icon(Icons.Default.Videocam, contentDescription = "Video Call", tint = BrandPrimary)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = BrandDanger.copy(alpha = 0.7f), modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}
