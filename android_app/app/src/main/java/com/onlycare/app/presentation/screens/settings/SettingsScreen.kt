package com.onlycare.app.presentation.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.onlycare.app.presentation.components.OnlyCareTopAppBar
import com.onlycare.app.presentation.navigation.Screen
import com.onlycare.app.presentation.screens.main.ProfileMenuItem
import com.onlycare.app.presentation.theme.DividerGray

@Composable
fun SettingsScreen(navController: NavController) {
    Scaffold(
        topBar = {
            OnlyCareTopAppBar(
                title = "Settings",
                onBackClick = { navController.navigateUp() }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            ProfileMenuItem(
                icon = Icons.Default.Security,
                title = "Account & Privacy",
                onClick = { navController.navigate(Screen.AccountPrivacy.route) }
            )
            
            Divider(color = DividerGray)
            
            ProfileMenuItem(
                icon = Icons.Default.Notifications,
                title = "Notifications",
                onClick = { /* Navigate to notifications settings */ }
            )
            
            ProfileMenuItem(
                icon = Icons.Default.Language,
                title = "Language",
                onClick = { /* Navigate to language settings */ }
            )
            
            ProfileMenuItem(
                icon = Icons.Default.Info,
                title = "About",
                onClick = { /* Navigate to about */ }
            )
        }
    }
}

