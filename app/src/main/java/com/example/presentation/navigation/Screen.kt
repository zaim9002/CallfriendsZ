package com.example.presentation.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Auth : Screen("auth")
    object Main : Screen("main") // Container with BottomNav
    object CreateMeeting : Screen("create_meeting")
    object JoinMeeting : Screen("join_meeting")
    object MeetingActive : Screen("meeting_active/{meetingId}") {
        fun createRoute(meetingId: String) = "meeting_active/$meetingId"
    }
    object ScheduleMeeting : Screen("schedule_meeting")
    object Settings : Screen("settings")
}

sealed class BottomNavTab(val route: String, val stringResId: Int, val iconRes: String) {
    object Home : BottomNavTab("tab_home", com.example.R.string.nav_home, "home")
    object Calls : BottomNavTab("tab_calls", com.example.R.string.nav_calls, "calls")
    object Meetings : BottomNavTab("tab_meetings", com.example.R.string.nav_meetings, "meetings")
    object Contacts : BottomNavTab("tab_contacts", com.example.R.string.nav_contacts, "contacts")
    object Profile : BottomNavTab("tab_profile", com.example.R.string.nav_profile, "profile")
}
