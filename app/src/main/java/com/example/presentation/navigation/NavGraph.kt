package com.example.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.example.core.AppContainer
import com.example.core.Constants
import com.example.core.UserSession
import com.example.presentation.auth.AuthScreen
import com.example.presentation.auth.AuthViewModel
import com.example.presentation.main.MainScreen
import com.example.presentation.meeting.CreateMeetingScreen
import com.example.presentation.meeting.CreateMeetingViewModel
import com.example.presentation.meeting.JoinMeetingScreen
import com.example.presentation.meeting.JoinMeetingViewModel
import com.example.presentation.meeting.MeetingActiveScreen
import com.example.presentation.meeting.MeetingActiveViewModel
import com.example.presentation.settings.SettingsScreen
import com.example.presentation.settings.SettingsViewModel
import com.example.presentation.splash.SplashScreen

@Composable
fun NavGraph(
    appContainer: AppContainer,
    initialMeetingId: String? = null
) {
    val navController = rememberNavController()
    val context = LocalContext.current

    val startDestination = if (initialMeetingId != null) {
        Screen.MeetingActive.createRoute(initialMeetingId)
    } else {
        Screen.Main.route
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Splash Screen
        composable(Screen.Splash.route) {
            SplashScreen(
                onSplashFinished = {
                    val isLoggedIn = UserSession.currentUser.value != null
                    val target = if (isLoggedIn) Screen.Main.route else Screen.Auth.route
                    navController.navigate(target) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        // Authentication Screen
        composable(Screen.Auth.route) {
            val authViewModel = remember { AuthViewModel() }
            AuthScreen(
                viewModel = authViewModel,
                onAuthSuccess = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Auth.route) { inclusive = true }
                    }
                }
            )
        }

        // Main Dashboard Screen (With 5 Bottom Tabs)
        composable(Screen.Main.route) {
            MainScreen(
                appContainer = appContainer,
                onNavigateToCreateMeeting = { navController.navigate(Screen.CreateMeeting.route) },
                onNavigateToJoinMeeting = { navController.navigate(Screen.JoinMeeting.route) },
                onNavigateToSchedule = { /* Handled in tab */ },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onJoinMeetingId = { id -> navController.navigate(Screen.MeetingActive.createRoute(id)) },
                onLogout = {
                    navController.navigate(Screen.Auth.route) {
                        popUpTo(Screen.Main.route) { inclusive = true }
                    }
                }
            )
        }

        // Create Meeting Screen
        composable(Screen.CreateMeeting.route) {
            val createMeetingViewModel = remember {
                CreateMeetingViewModel(appContainer.meetingRepository)
            }
            CreateMeetingScreen(
                viewModel = createMeetingViewModel,
                onNavigateBack = { navController.popBackStack() },
                onStartMeeting = { meetingId ->
                    navController.navigate(Screen.MeetingActive.createRoute(meetingId)) {
                        popUpTo(Screen.CreateMeeting.route) { inclusive = true }
                    }
                }
            )
        }

        // Join Meeting Screen
        composable(Screen.JoinMeeting.route) {
            val joinMeetingViewModel = remember {
                JoinMeetingViewModel(appContainer.meetingRepository, appContainer.meetingClient)
            }
            JoinMeetingScreen(
                viewModel = joinMeetingViewModel,
                onNavigateBack = { navController.popBackStack() },
                onJoinSuccess = { meetingId ->
                    navController.navigate(Screen.MeetingActive.createRoute(meetingId)) {
                        popUpTo(Screen.JoinMeeting.route) { inclusive = true }
                    }
                }
            )
        }

        // Active Meeting Screen
        composable(
            route = Screen.MeetingActive.route,
            arguments = listOf(
                navArgument("meetingId") { type = NavType.StringType }
            ),
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = "${Constants.DEEP_LINK_SCHEME}://${Constants.DEEP_LINK_HOST}${Constants.DEEP_LINK_MEETING_PREFIX}{meetingId}"
                }
            )
        ) { backStackEntry ->
            val meetingId = backStackEntry.arguments?.getString("meetingId") ?: "default-meeting"
            val meetingActiveViewModel = remember(meetingId) {
                MeetingActiveViewModel(appContainer.meetingRepository, appContainer.meetingClient)
            }
            MeetingActiveScreen(
                meetingId = meetingId,
                viewModel = meetingActiveViewModel,
                onLeaveMeeting = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.MeetingActive.route) { inclusive = true }
                    }
                }
            )
        }

        // Settings Screen
        composable(Screen.Settings.route) {
            val settingsViewModel = remember { SettingsViewModel() }
            SettingsScreen(
                viewModel = settingsViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
