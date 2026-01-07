package com.onlycare.app.presentation.screens.main

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.onlycare.app.domain.model.Gender
import com.onlycare.app.presentation.navigation.Screen
import com.onlycare.app.presentation.theme.Primary
import com.onlycare.app.presentation.theme.PrimaryLight
import com.onlycare.app.presentation.theme.Background
import com.onlycare.app.presentation.theme.TextPrimary
import com.onlycare.app.presentation.theme.TextTertiary
import com.onlycare.app.presentation.theme.White
import com.onlycare.app.presentation.theme.Surface
import androidx.compose.material3.Icon
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.layout.RowScope
// Removed ambiguous import of Modifier to avoid conflicts
import androidx.compose.foundation.layout.size

// If you have these screens in other packages, keep their imports as-is.
// MaleHomeScreen is the one we just finished.
// FemaleHomeScreen / RecentCallsScreen / MaleProfileScreen / FemaleProfileScreen / FriendsScreen
// should already exist in your project.

@Composable
fun MainScreen(
    navController: NavController,
    viewModel: MainScreenViewModel = hiltViewModel()
) {
    val bottomNavController = rememberNavController()
    val currentRoute = bottomNavController
        .currentBackStackEntryAsState().value?.destination?.route

    val isMale = viewModel.getGender() == Gender.MALE
    
    // Auth screen routes that should not be accessible via back button
    val authRoutes = setOf(
        Screen.Splash.route,
        Screen.Login.route,
        "verify_otp", // Base route for VerifyOTP
        Screen.SelectGender.route,
        Screen.SelectLanguage.route,
        Screen.SetupProfile.route,
        Screen.VoiceIdentification.route,
        Screen.GrantPermissions.route
    )
    
    // Get current back stack entry and context
    val backStackEntry = navController.currentBackStackEntryAsState().value
    val context = LocalContext.current
    
    // Handle system back button - prevent going back to account creation screens
    BackHandler(enabled = true) {
        // Check if there's a previous entry in the back stack
        val previousEntry = navController.previousBackStackEntry
        val previousRoute = previousEntry?.destination?.route
        
        // Check if the previous route is an auth screen
        val isPreviousAuthScreen = previousRoute?.let { route ->
            authRoutes.any { authRoute -> 
                route == authRoute || route.startsWith(authRoute) || route.contains("/$authRoute")
            }
        } ?: false
        
        if (isPreviousAuthScreen || previousEntry == null) {
            // Prevent navigation back to auth screens or if no previous entry
            // Exit app instead of going back to account creation
            (context as? Activity)?.finish()
        } else {
            // Allow normal back navigation if not going to auth screen
            navController.navigateUp()
        }
    }

    Scaffold(
        containerColor = Background,
        bottomBar = {
            if (isMale) {
                MaleBottomNavigation(
                    currentRoute = currentRoute ?: Screen.Home.route
                ) { route ->
                    bottomNavController.navigate(route) {
                        bottomNavController.graph.startDestinationRoute?.let { start ->
                            popUpTo(start) { saveState = true }
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            } else {
                FemaleBottomNavigation(
                    currentRoute = currentRoute ?: Screen.Home.route
                ) { route ->
                    bottomNavController.navigate(route) {
                        bottomNavController.graph.startDestinationRoute?.let { start ->
                            popUpTo(start) { saveState = true }
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = bottomNavController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Screen.Home.route) {
                if (isMale) {
                    // Adapter overload: passes through to callbacks
                    MaleHomeScreen(
                        onOpenMessages = { navController.navigate(Screen.ChatList.route) },
                        onOpenRecent = { navController.navigate(Screen.Recent.route) },
                        onOpenProfileTab = { navController.navigate(Screen.Profile.route) },
                        onOpenProfile = { userId, _ -> navController.navigate(Screen.UserProfile.createRoute(userId)) },
                        onNavigateToWallet = { navController.navigate(Screen.Wallet.route) },
                        onAudioCall = { userId -> navController.navigate(Screen.CallConnecting.createRoute(userId, "audio")) },
                        onVideoCall = { userId -> navController.navigate(Screen.CallConnecting.createRoute(userId, "video")) },
                        onStartRandomCall = { callType, candidateUserIds ->
                            // Pass list via savedStateHandle (avoid long routes)
                            navController.currentBackStackEntry?.savedStateHandle?.set(
                                "random_candidates",
                                ArrayList(candidateUserIds)
                            )
                            navController.navigate(Screen.RandomCall.createRoute(callType))
                        }
                    )
                } else {
                    FemaleHomeScreen(navController = navController)
                }
            }

            composable(Screen.Recent.route) {
                RecentCallsScreen(navController = navController)
            }

            composable(Screen.Profile.route) {
                if (isMale) {
                    MaleProfileScreen(navController = navController)
                } else {
                    FemaleProfileScreen(navController = navController)
                }
            }

            composable(Screen.Friends.route) {
                FriendsScreen(navController = navController)
            }
        }
    }
}

/* ---------------- Bottom Navs ---------------- */

@Composable
fun MaleBottomNavigation(
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    NavigationBar(
        containerColor = Surface,
        contentColor = TextPrimary,
        tonalElevation = 0.dp
    ) {
        BottomNavItem(
            icon = Icons.Default.Home,
            label = "Home",
            selected = currentRoute == Screen.Home.route,
            onClick = { onNavigate(Screen.Home.route) }
        )
        BottomNavItem(
            icon = Icons.Default.History,
            label = "Recent",
            selected = currentRoute == Screen.Recent.route,
            onClick = { onNavigate(Screen.Recent.route) }
        )
        BottomNavItem(
            icon = Icons.Default.Person,
            label = "Profile",
            selected = currentRoute == Screen.Profile.route,
            onClick = { onNavigate(Screen.Profile.route) }
        )
    }
}

@Composable
fun FemaleBottomNavigation(
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    NavigationBar(
        containerColor = Surface,
        contentColor = TextPrimary,
        tonalElevation = 0.dp
    ) {
        BottomNavItem(
            icon = Icons.Default.Home,
            label = "Home",
            selected = currentRoute == Screen.Home.route,
            onClick = { onNavigate(Screen.Home.route) }
        )
        BottomNavItem(
            icon = Icons.Default.History,
            label = "Recent",
            selected = currentRoute == Screen.Recent.route,
            onClick = { onNavigate(Screen.Recent.route) }
        )
        BottomNavItem(
            icon = Icons.Default.Person,
            label = "Profile",
            selected = currentRoute == Screen.Profile.route,
            onClick = { onNavigate(Screen.Profile.route) }
        )
    }
}

@Composable
fun RowScope.BottomNavItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    NavigationBarItem(
        icon = {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(24.dp)
            )
        },
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall
            )
        },
        selected = selected,
        onClick = onClick,
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = White,
            unselectedIconColor = TextTertiary,
            selectedTextColor = Primary,
            unselectedTextColor = TextTertiary,
            // Use strong brand color so white selected icon is clearly visible
            indicatorColor = Primary
        )
    )
}
