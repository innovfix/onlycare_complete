package com.onlycare.app.presentation.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.onlycare.app.presentation.components.OnlyCareTopAppBar
import com.onlycare.app.presentation.components.HtmlContentView
import com.onlycare.app.presentation.theme.*

@Composable
fun RefundPolicyScreen(
    navController: NavController,
    viewModel: RefundPolicyViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    
    Scaffold(
        containerColor = Background,
        topBar = {
            OnlyCareTopAppBar(
                title = "Refund Policy",
                onBackClick = { navController.navigateUp() }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Background)
                .padding(paddingValues)
        ) {
            when {
                state.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = Primary
                    )
                }
                
                state.error != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = "Error",
                            tint = ErrorRed,
                            modifier = Modifier.size(64.dp)
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = state.error ?: "Unknown error",
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextPrimary
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Button(
                            onClick = { viewModel.retry() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Primary,
                                contentColor = Color.White
                            )
                        ) {
                            Text("Retry")
                        }
                    }
                }
                
                else -> {
                    when {
                        state.sections.isNotEmpty() -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(24.dp)
                                    .verticalScroll(rememberScrollState())
                            ) {
                                Text(
                                    text = state.title,
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )

                                if (state.lastUpdated.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text(
                                        text = "Last Updated: ${state.lastUpdated}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                }

                                Spacer(modifier = Modifier.height(24.dp))

                                state.sections.forEach { section ->
                                    Text(
                                        text = section.heading,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text(
                                        text = section.text,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextSecondary
                                    )

                                    if (!section.points.isNullOrEmpty()) {
                                        Spacer(modifier = Modifier.height(8.dp))

                                        section.points.forEach { point ->
                                            Text(
                                                text = "• $point",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = TextSecondary,
                                                modifier = Modifier.padding(start = 16.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))
                                }
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
                                    text = "No refund policy content available.",
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}



