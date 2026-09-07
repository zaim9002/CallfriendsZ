package com.example.presentation.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.example.R
import com.example.core.AppContainer
import com.example.presentation.calls.CallsScreen
import com.example.presentation.calls.CallsViewModel
import com.example.presentation.contacts.ContactsScreen
import com.example.presentation.home.HomeScreen
import com.example.presentation.home.HomeViewModel
import com.example.presentation.meetings.MeetingsCalendarScreen
import com.example.presentation.meetings.MeetingsCalendarViewModel
import com.example.presentation.profile.ProfileScreen
import com.example.ui.theme.BrandBackground
import com.example.ui.theme.BrandBorder
import com.example.ui.theme.BrandPrimary
import com.example.ui.theme.BrandSurface
import com.example.ui.theme.BrandTextPrimary
import com.example.ui.theme.BrandTextSecondary

enum class MainTab {
    HOME,
    CALLS,
    MEETINGS,
    CONTACTS,
    PROFILE
}

@Composable
fun MainScreen(
    appContainer: AppContainer,
    onNavigateToCreateMeeting: () -> Unit,
    onNavigateToJoinMeeting: () -> Unit,
    onNavigateToSchedule: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onJoinMeetingId: (String) -> Unit,
    onLogout: () -> Unit
) {
    var currentTab by rememberSaveable { mutableStateOf(MainTab.HOME) }

    val homeViewModel = remember(appContainer) {
        HomeViewModel(appContainer.meetingRepository, appContainer.scheduledMeetingRepository)
    }
    val callsViewModel = remember(appContainer) {
        CallsViewModel(appContainer.callsRepository)
    }
    val meetingsViewModel = remember(appContainer) {
        MeetingsCalendarViewModel(appContainer.scheduledMeetingRepository)
    }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = BrandSurface,
                contentColor = BrandTextPrimary,
                modifier = Modifier.testTag("main_bottom_bar")
            ) {
                NavigationBarItem(
                    selected = currentTab == MainTab.HOME,
                    onClick = { currentTab = MainTab.HOME },
                    alwaysShowLabel = false,
                    icon = { Icon(Icons.Default.Home, contentDescription = stringResource(R.string.nav_home)) },
                    label = { Text(stringResource(R.string.nav_home), maxLines = 1, softWrap = false, overflow = TextOverflow.Ellipsis) },
                    colors = navBarColors()
                )
                NavigationBarItem(
                    selected = currentTab == MainTab.CALLS,
                    onClick = { currentTab = MainTab.CALLS },
                    alwaysShowLabel = false,
                    icon = { Icon(Icons.Default.Call, contentDescription = stringResource(R.string.nav_calls)) },
                    label = { Text(stringResource(R.string.nav_calls), maxLines = 1, softWrap = false, overflow = TextOverflow.Ellipsis) },
                    colors = navBarColors()
                )
                NavigationBarItem(
                    selected = currentTab == MainTab.MEETINGS,
                    onClick = { currentTab = MainTab.MEETINGS },
                    alwaysShowLabel = false,
                    icon = { Icon(Icons.Default.CalendarMonth, contentDescription = stringResource(R.string.nav_meetings)) },
                    label = { Text(stringResource(R.string.nav_meetings), maxLines = 1, softWrap = false, overflow = TextOverflow.Ellipsis) },
                    colors = navBarColors()
                )
                NavigationBarItem(
                    selected = currentTab == MainTab.CONTACTS,
                    onClick = { currentTab = MainTab.CONTACTS },
                    alwaysShowLabel = false,
                    icon = { Icon(Icons.Default.People, contentDescription = stringResource(R.string.nav_contacts)) },
                    label = { Text(stringResource(R.string.nav_contacts), maxLines = 1, softWrap = false, overflow = TextOverflow.Ellipsis) },
                    colors = navBarColors()
                )
                NavigationBarItem(
                    selected = currentTab == MainTab.PROFILE,
                    onClick = { currentTab = MainTab.PROFILE },
                    alwaysShowLabel = false,
                    icon = { Icon(Icons.Default.Person, contentDescription = stringResource(R.string.nav_profile)) },
                    label = { Text(stringResource(R.string.nav_profile), maxLines = 1, softWrap = false, overflow = TextOverflow.Ellipsis) },
                    colors = navBarColors()
                )
            }
        },
        containerColor = BrandBackground
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                MainTab.HOME -> {
                    HomeScreen(
                        viewModel = homeViewModel,
                        onNavigateToCreateMeeting = onNavigateToCreateMeeting,
                        onNavigateToJoinMeeting = onNavigateToJoinMeeting,
                        onNavigateToSchedule = { currentTab = MainTab.MEETINGS },
                        onNavigateToSettings = onNavigateToSettings,
                        onJoinMeetingId = onJoinMeetingId
                    )
                }
                MainTab.CALLS -> {
                    CallsScreen(
                        viewModel = callsViewModel,
                        onStartCall = onJoinMeetingId
                    )
                }
                MainTab.MEETINGS -> {
                    MeetingsCalendarScreen(
                        viewModel = meetingsViewModel,
                        onJoinMeeting = onJoinMeetingId
                    )
                }
                MainTab.CONTACTS -> {
                    ContactsScreen(
                        viewModel = callsViewModel,
                        onStartCall = onJoinMeetingId
                    )
                }
                MainTab.PROFILE -> {
                    ProfileScreen(
                        onNavigateToSettings = onNavigateToSettings,
                        onLogout = onLogout
                    )
                }
            }
        }
    }
}

@Composable
private fun navBarColors() = NavigationBarItemDefaults.colors(
    selectedIconColor = BrandPrimary,
    selectedTextColor = BrandPrimary,
    unselectedIconColor = BrandTextSecondary,
    unselectedTextColor = BrandTextSecondary,
    indicatorColor = BrandPrimary.copy(alpha = 0.18f)
)
