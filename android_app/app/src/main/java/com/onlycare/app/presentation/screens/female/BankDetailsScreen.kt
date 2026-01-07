package com.onlycare.app.presentation.screens.female

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.onlycare.app.presentation.theme.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import com.onlycare.app.presentation.components.OnlyCareSoftShadowContainer
import com.onlycare.app.presentation.components.OnlyCareTextField
import com.onlycare.app.presentation.components.OnlyCareTopAppBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BankDetailsScreen(
    navController: NavController,
    viewModel: BankDetailsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    
    // Handle success - navigate back
    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) {
            viewModel.resetSuccess()
            navController.navigateUp()
        }
    }
    
    Scaffold(
        containerColor = Background,
        topBar = {
            OnlyCareTopAppBar(
                title = "Add UPI ID",
                onBackClick = { navController.navigateUp() }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Background)
                .padding(paddingValues),
            contentPadding = PaddingValues(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }
            
            // Main Card
            item {
                OnlyCareSoftShadowContainer(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    shadowOffsetY = 4.dp,
                    shadowColor = Border.copy(alpha = 0.28f)
                ) {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = ThemeSurface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, PrimaryLight, RoundedCornerShape(24.dp))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    ) {
                        // UPI Icon and Title
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(PrimaryLight.copy(alpha = 0.25f))
                                    .border(2.dp, PrimaryLight, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Payment,
                                    contentDescription = null,
                                    tint = Primary,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                            
                            Spacer(modifier = Modifier.width(16.dp))
                            
                            Column {
                                Text(
                                    text = "UPI ID",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = TextPrimary,
                                    fontSize = 22.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "For instant money credit",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary,
                                    fontSize = 14.sp
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(32.dp))
                        
                        // Input Label
                        Text(
                            text = "Enter UPI ID",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = TextSecondary,
                            fontSize = 14.sp
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        OnlyCareTextField(
                            value = state.upiId,
                            onValueChange = { viewModel.onUpiIdChange(it.lowercase().trim()) },
                            label = "UPI ID",
                            placeholder = "yourname@bank",
                            leadingIcon = Icons.Default.Payment,
                            keyboardType = KeyboardType.Email,
                            enabled = !state.isLoading,
                            error = state.error
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))

                        // Example Text
                        Text(
                            text = "Example: yourname@bank",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                        
                        Spacer(modifier = Modifier.height(28.dp))
                        
                        // Verify Button
                        Button(
                            onClick = {
                                viewModel.saveUpiId()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Primary,
                                contentColor = Color.White,
                                disabledContainerColor = PrimaryLight.copy(alpha = 0.6f),
                                disabledContentColor = Color.White
                            ),
                            shape = RoundedCornerShape(16.dp),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 0.dp,
                                pressedElevation = 0.dp,
                                disabledElevation = 0.dp
                            ),
                            enabled = state.upiId.contains("@") && state.upiId.length > 5 && !state.isLoading
                        ) {
                            if (state.isLoading) {
                                androidx.compose.material3.CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = Color.White
                                )
                            } else {
                                Text(
                                    text = "Verify & Save",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    letterSpacing = 0.2.sp,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
            }
            
            // Info Message
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = PrimaryLight.copy(alpha = 0.12f)
                    ),
                    modifier = Modifier.border(
                        1.dp,
                        PrimaryLight.copy(alpha = 0.35f),
                        RoundedCornerShape(18.dp)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        Text(
                            text = "Your UPI ID will be used for instant withdrawals. Make sure it's active and verified.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = PrimaryDark,
                            fontSize = 14.sp,
                            lineHeight = 20.sp
                        )
                    }
                }
            }
        }
    }
}


/**
 * Deprecated legacy UI (dark/gradient). Kept only for binary compatibility in old builds.
 * Do NOT use. Use [BankDetailsScreen] instead.
 */
@Deprecated("Use BankDetailsScreen")
@Composable
fun BankDetailsScreenOld(navController: NavController) {
    BankDetailsScreen(navController = navController)
}

