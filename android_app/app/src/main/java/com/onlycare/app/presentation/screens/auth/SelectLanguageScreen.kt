package com.onlycare.app.presentation.screens.auth

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.onlycare.app.domain.model.Gender
import com.onlycare.app.domain.model.Language
import com.onlycare.app.presentation.components.OnlyCarePrimaryButton
import com.onlycare.app.presentation.navigation.Screen
import com.onlycare.app.presentation.theme.TextGray
import com.onlycare.app.presentation.theme.White

@Composable
fun SelectLanguageScreen(
    navController: NavController,
    viewModel: SelectLanguageViewModel = hiltViewModel()
) {
    var selectedLanguage by remember { mutableStateOf<Language?>(null) }
    val languages = Language.entries.toList()
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(com.onlycare.app.presentation.theme.Background)  // White background
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Back button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.Start
        ) {
            IconButton(onClick = { navController.navigateUp() }) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = com.onlycare.app.presentation.theme.TextPrimary  // Dark arrow on white
                )
            }
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        
        Text(
            text = "Select Your Language",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = com.onlycare.app.presentation.theme.TextPrimary  // Dark text on white
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Choose your preferred language",
            style = MaterialTheme.typography.bodyMedium,
            color = com.onlycare.app.presentation.theme.TextSecondary,  // Medium gray
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(40.dp))
        
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(languages) { language ->
                LanguageChip(
                    text = language.displayName,
                    selected = selectedLanguage == language,
                    onClick = { selectedLanguage = language },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        OnlyCarePrimaryButton(
            text = if (state.isRegistering) "Registering..." else "Continue",
            onClick = {
                selectedLanguage?.let { language ->
                    // Save language to session
                    viewModel.saveLanguage(language)
                    
                    val userGender = viewModel.getGender()
                    
                    if (userGender == Gender.MALE) {
                        // Male users: Register here and go to GrantPermissions
                        viewModel.registerMaleUser(
                            onSuccess = {
                                // Navigate to GrantPermissions and clear stack
                                navController.navigate(Screen.GrantPermissions.route) {
                                    popUpTo(Screen.Login.route) { inclusive = true }
                                }
                            },
                            onFailure = { error ->
                                Toast.makeText(
                                    context,
                                    "Registration failed: $error",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        )
                    } else {
                        // Female users: Go to SetupProfile screen
                        navController.navigate(Screen.SetupProfile.route)
                    }
                }
            },
            enabled = selectedLanguage != null && !state.isRegistering
        )
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}



@Composable
private fun LanguageChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Selected: white background, blue border, blue text (no filled blue)
    val borderColor = if (selected) com.onlycare.app.presentation.theme.Primary else com.onlycare.app.presentation.theme.Border
    val textColor = if (selected) com.onlycare.app.presentation.theme.Primary else com.onlycare.app.presentation.theme.TextPrimary

    Surface(
        onClick = onClick,
        modifier = modifier.height(56.dp),
        color = White,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = borderColor
        ),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = textColor,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}
