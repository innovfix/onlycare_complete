package com.onlycare.app.presentation.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.onlycare.app.presentation.theme.*
import com.onlycare.app.presentation.components.HtmlContentView

@Composable
fun PrivacyPolicyScreen(
    navController: NavController,
    viewModel: PrivacyPolicyViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Premium Header with Back Button
            PremiumPrivacyHeader(
                title = state.title,
                onBackClick = { navController.navigateUp() }
            )
            
            // Content based on state
            when {
                state.isLoading -> {
                    // Loading State
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = Primary)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Loading privacy policy...",
                                color = TextSecondary,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
                
                state.error != null -> {
                    // Error State
                    val errorMessage = state.error ?: "Unknown error"
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(32.dp)
                        ) {
                            Text(
                                text = "Failed to load privacy policy",
                                color = TextPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = errorMessage,
                                color = TextSecondary,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(
                                onClick = { viewModel.retry() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Primary,
                                    contentColor = Color.White
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Retry",
                                    tint = Color.White
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Retry", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
                
                else -> {
                    // Success State - Prefer structured sections; fallback to HTML if provided.
                    when {
                        state.sections.isNotEmpty() -> {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(24.dp)
                            ) {
                                // Last Updated
                                item {
                                    Spacer(modifier = Modifier.height(8.dp))

                                    if (state.lastUpdated.isNotEmpty()) {
                                        Text(
                                            text = "Last Updated: ${state.lastUpdated}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextSecondary,
                                            fontSize = 13.sp
                                        )
                                        Spacer(modifier = Modifier.height(24.dp))
                                    }
                                }

                                // Content Sections from API
                                items(state.sections) { section ->
                                    PrivacySection(
                                        heading = section.heading,
                                        text = section.text,
                                        points = section.points
                                    )
                                    Spacer(modifier = Modifier.height(20.dp))
                                }

                                // Bottom spacing
                                item { Spacer(modifier = Modifier.height(12.dp)) }
                            }
                        }

                        !state.htmlContent.isNullOrBlank() -> {
                            HtmlContentView(
                                html = state.htmlContent!!,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        else -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No privacy policy content available.",
                                    color = TextSecondary,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// Premium Header Component
@Composable
private fun PremiumPrivacyHeader(
    title: String,
    onBackClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Primary)
            .padding(horizontal = 16.dp, vertical = 16.dp)
            .statusBarsPadding(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBackClick) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = Color.White
            )
        }
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            fontSize = 20.sp
        )
    }
}

// Privacy Section Component - Updated to handle API data structure
@Composable
private fun PrivacySection(
    heading: String,
    text: String,
    points: List<String>? = null
) {
    Column {
        // Heading
        Text(
            text = heading,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            fontSize = 18.sp
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Main text
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            fontSize = 15.sp,
            lineHeight = 24.sp
        )
        
        // Points (if available)
        if (!points.isNullOrEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            
            points.forEach { point ->
                Row(
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Text(
                        text = "• ",
                        color = TextSecondary,
                        fontSize = 15.sp,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = point,
                        color = TextSecondary,
                        fontSize = 15.sp,
                        lineHeight = 22.sp
                    )
                }
            }
        }
    }
}

