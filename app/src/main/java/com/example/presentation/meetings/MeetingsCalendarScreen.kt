package com.example.presentation.meetings

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
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.text.style.TextAlign
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
import com.example.domain.model.ScheduledMeeting
import com.example.presentation.auth.outlinedFieldColors
import com.example.ui.theme.BrandBackground
import com.example.ui.theme.BrandBorder
import com.example.ui.theme.BrandPrimary
import com.example.ui.theme.BrandSurface
import com.example.ui.theme.BrandSurfaceElevated
import com.example.ui.theme.BrandSurfaceVariant
import com.example.ui.theme.BrandTextPrimary
import com.example.ui.theme.BrandTextSecondary
import com.example.ui.theme.BrandTextTertiary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeetingsCalendarScreen(
    viewModel: MeetingsCalendarViewModel,
    onJoinMeeting: (meetingId: String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.meetings_title),
                        fontWeight = FontWeight.Bold,
                        color = BrandTextPrimary
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BrandBackground)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.toggleScheduleDialog(true) },
                containerColor = BrandPrimary,
                shape = CircleShape,
                modifier = Modifier.testTag("meetings_schedule_fab")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Schedule",
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
                .padding(horizontal = 16.dp)
        ) {
            // Calendar Tabs
            TabRow(
                selectedTabIndex = uiState.selectedTab.ordinal,
                containerColor = BrandBackground,
                contentColor = BrandPrimary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[uiState.selectedTab.ordinal]),
                        color = BrandPrimary
                    )
                }
            ) {
                Tab(
                    selected = uiState.selectedTab == CalendarTab.TODAY,
                    onClick = { viewModel.selectTab(CalendarTab.TODAY) },
                    text = { Text(stringResource(R.string.tab_today), fontWeight = FontWeight.SemiBold) }
                )
                Tab(
                    selected = uiState.selectedTab == CalendarTab.TOMORROW,
                    onClick = { viewModel.selectTab(CalendarTab.TOMORROW) },
                    text = { Text(stringResource(R.string.tab_tomorrow), fontWeight = FontWeight.SemiBold) }
                )
                Tab(
                    selected = uiState.selectedTab == CalendarTab.UPCOMING,
                    onClick = { viewModel.selectTab(CalendarTab.UPCOMING) },
                    text = { Text(stringResource(R.string.tab_upcoming), fontWeight = FontWeight.SemiBold) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            val filteredMeetings = when (uiState.selectedTab) {
                CalendarTab.TODAY -> uiState.scheduledMeetings.filter { it.dateString.equals("Today", ignoreCase = true) }
                CalendarTab.TOMORROW -> uiState.scheduledMeetings.filter { it.dateString.equals("Tomorrow", ignoreCase = true) }
                CalendarTab.UPCOMING -> uiState.scheduledMeetings
            }

            if (filteredMeetings.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Text(
                            text = "لا توجد اجتماعات مجدولة في هذه الفترة",
                            color = BrandTextPrimary,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "اضغط على زر (+) لجدولة اجتماع جديد ومشاركته برابط مباشر",
                            color = BrandTextSecondary,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredMeetings) { meeting ->
                        ScheduledCard(
                            meeting = meeting,
                            onJoin = { onJoinMeeting("cfz-sch-${meeting.id.takeLast(4)}") }
                        )
                    }
                }
            }
        }
    }

    if (uiState.showScheduleDialog) {
        ScheduleMeetingDialog(
            onDismiss = { viewModel.toggleScheduleDialog(false) },
            onSave = viewModel::saveScheduledMeeting
        )
    }
}

@Composable
private fun ScheduledCard(
    meeting: ScheduledMeeting,
    onJoin: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = BrandSurface),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(BrandBorder, BrandBorder)))
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = meeting.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = BrandTextPrimary
                )
                Button(
                    onClick = onJoin,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary)
                ) {
                    Text("Join", fontWeight = FontWeight.Bold)
                }
            }

            if (meeting.description.isNotBlank()) {
                Text(
                    text = meeting.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = BrandTextSecondary
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    tint = BrandPrimary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${meeting.dateString} • ${meeting.startTime} - ${meeting.endTime}",
                    style = MaterialTheme.typography.bodySmall,
                    color = BrandTextSecondary
                )
            }
        }
    }
}

@Composable
private fun ScheduleMeetingDialog(
    onDismiss: () -> Unit,
    onSave: (title: String, desc: String, date: String, start: String, end: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("Today") }
    var start by remember { mutableStateOf("03:00 PM") }
    var end by remember { mutableStateOf("04:00 PM") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.schedule_meeting), color = BrandTextPrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = outlinedFieldColors()
                )
                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = outlinedFieldColors()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = start,
                        onValueChange = { start = it },
                        label = { Text("Start") },
                        modifier = Modifier.weight(1f),
                        colors = outlinedFieldColors()
                    )
                    OutlinedTextField(
                        value = end,
                        onValueChange = { end = it },
                        label = { Text("End") },
                        modifier = Modifier.weight(1f),
                        colors = outlinedFieldColors()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(title, desc, date, start, end) },
                colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary)
            ) {
                Text("Schedule")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = BrandTextSecondary)
            }
        },
        containerColor = BrandSurface
    )
}
