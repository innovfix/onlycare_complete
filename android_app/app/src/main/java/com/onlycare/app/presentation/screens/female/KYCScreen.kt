package com.onlycare.app.presentation.screens.female

import androidx.compose.foundation.BorderStroke
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
fun KYCScreen(
    navController: NavController,
    viewModel: KYCViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    
    // Handle success - navigate back
    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) {
            viewModel.resetSuccess()
            navController.navigateUp()
        }
    }
    
    val isPanValid = state.panNumber.length == 10 && state.panNumber.matches(Regex("[A-Z]{5}[0-9]{4}[A-Z]"))
    val isFormValid = state.fullName.isNotBlank() && isPanValid && !state.isLoading
    
    Scaffold(
        containerColor = Background,
        topBar = {
            OnlyCareTopAppBar(
                title = "Complete KYC",
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
            
            // PAN Card Details Card
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
                        // Header
                        Text(
                            text = "PAN Card Details",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary,
                            fontSize = 20.sp
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "Please provide your PAN card information for verification",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                            fontSize = 14.sp,
                            lineHeight = 20.sp
                        )
                        
                        Spacer(modifier = Modifier.height(28.dp))
                        
                        // Full Name Field
                        Text(
                            text = "Enter your full name as on PAN card",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary,
                            fontSize = 15.sp
                        )
                        
                        Spacer(modifier = Modifier.height(6.dp))
                        
                        Text(
                            text = "Enter name exactly as it appears on your PAN card",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))

                        OnlyCareTextField(
                            value = state.fullName,
                            onValueChange = { viewModel.onFullNameChange(it) },
                            label = "Full name",
                            placeholder = "Enter your full name",
                            leadingIcon = Icons.Default.Person,
                            enabled = !state.isLoading,
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // PAN Number Field
                        Text(
                            text = "Please enter 10 digit PAN number",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary,
                            fontSize = 15.sp
                        )
                        
                        Spacer(modifier = Modifier.height(6.dp))
                        
                        Text(
                            text = "10 digit alphanumeric PAN number",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        OnlyCareTextField(
                            value = state.panNumber,
                            onValueChange = {
                                val up = it.uppercase().trim()
                                if (up.length <= 10) viewModel.onPanNumberChange(up)
                            },
                            label = "PAN number",
                            placeholder = "ABCDE1234F",
                            leadingIcon = Icons.Default.Badge,
                            enabled = !state.isLoading,
                            error = state.error,
                            maxLines = 1
                        )
                    }
                }
                }
            }
            
            // Info Message
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = PrimaryLight.copy(alpha = 0.22f)
                    ),
                    modifier = Modifier.border(
                        1.dp,
                        PrimaryLight,
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
                            text = "Your information is secure and will be used for verification purposes only",
                            style = MaterialTheme.typography.bodyMedium,
                            color = PrimaryDark,
                            fontSize = 14.sp,
                            lineHeight = 20.sp
                        )
                    }
                }
            }

            // Submit Button
            item {
                Button(
                    onClick = {
                        viewModel.verifyPanCard()
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
                    enabled = isFormValid
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White
                        )
                    } else {
                        Text(
                            text = "Verify & Submit",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            letterSpacing = 0.5.sp,
                            color = Color.White
                        )
                    }
                }
            }
            
            item { Spacer(modifier = Modifier.height(20.dp)) }
        }
    }
}
