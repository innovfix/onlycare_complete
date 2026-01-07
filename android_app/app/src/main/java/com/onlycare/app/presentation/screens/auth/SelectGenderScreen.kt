package com.onlycare.app.presentation.screens.auth

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Female
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Male
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.BackHandler
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.onlycare.app.domain.model.Gender
import com.onlycare.app.presentation.navigation.Screen
import com.onlycare.app.presentation.theme.*
import kotlinx.coroutines.launch

@Composable
fun SelectGenderScreen(
    navController: NavController,
    viewModel: SelectGenderViewModel = hiltViewModel()
) {
    var selectedGender by remember { mutableStateOf<Gender?>(null) }
    var selectedAvatarIndex by remember { mutableStateOf<Int?>(null) }
    val state by viewModel.state.collectAsState()
    
    // Get avatars based on selected gender
    val avatars = when (selectedGender) {
        Gender.MALE -> state.maleAvatars
        Gender.FEMALE -> state.femaleAvatars
        null -> emptyList() // Don't show anything if no gender selected
    }
    
    // Reset avatar selection when gender changes
    LaunchedEffect(selectedGender) {
        selectedAvatarIndex = null
    }
    
    // Handle back button - clear session and go back to Login
    BackHandler(enabled = true) {
        viewModel.clearSessionAndCancelRegistration()
        // Navigate back to Login screen and clear the entire back stack
        navController.navigate(Screen.Login.route) {
            // Clear entire back stack up to and including the start destination
            popUpTo(navController.graph.startDestinationRoute ?: Screen.Splash.route) {
                inclusive = true
            }
            // Prevent multiple instances
            launchSingleTop = true
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)  // Pure white background
    ) {
        // Premium Header Box (flat royal violet)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Primary)  // Flat royal violet header
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Back Button Row
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(start = 16.dp, end = 16.dp, top = 8.dp)
                ) {
                    IconButton(
                        onClick = {
                            // Clear all session data and temporary user info
                            viewModel.clearSessionAndCancelRegistration()
                            // Navigate back to Login screen and clear the entire back stack
                            navController.navigate(Screen.Login.route) {
                                // Clear entire back stack up to and including the start destination
                                popUpTo(navController.graph.startDestinationRoute ?: Screen.Splash.route) {
                                    inclusive = true
                                }
                                // Prevent multiple instances
                                launchSingleTop = true
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
                
                // Title Section
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(top = 8.dp, bottom = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Set up your profile",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = White,
                        fontSize = 26.sp,
                        textAlign = TextAlign.Center,
                        letterSpacing = 0.5.sp
                    )
                    
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    Text(
                        text = "Choose your gender and avatar to get started",
                        style = MaterialTheme.typography.bodyMedium,
                        color = White.copy(alpha = 0.85f),
                        textAlign = TextAlign.Center,
                        letterSpacing = 0.3.sp,
                        lineHeight = 20.sp
                    )
                }
            }
        }
        
        // Scrollable Content Section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(top = 32.dp)
        ) {
            // "I am a" Label
            Text(
                text = "I am a",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,  // Rich black text
                fontSize = 20.sp,
                letterSpacing = 0.5.sp
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Gender Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                PremiumGenderCard(
                    icon = Icons.Default.Male,
                    label = "Male",
                    selected = selectedGender == Gender.MALE,
                    onClick = { 
                        selectedGender = Gender.MALE
                        selectedAvatarIndex = null // Reset avatar selection
                    },
                    modifier = Modifier.weight(1f)
                )
                
                PremiumGenderCard(
                    icon = Icons.Default.Female,
                    label = "Female",
                    selected = selectedGender == Gender.FEMALE,
                    onClick = { 
                        selectedGender = Gender.FEMALE
                        selectedAvatarIndex = null // Reset avatar selection
                    },
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Avatar Picker
            Text(
                text = "Pick your avatar",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,  // Rich black text
                fontSize = 20.sp,
                letterSpacing = 0.5.sp
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Show avatars only if gender is selected
            if (selectedGender != null) {
                if (state.isLoadingAvatars) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Primary)  // Royal violet spinner
                    }
                } else if (avatars.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No avatars available",
                            color = TextSecondary,
                            fontSize = 14.sp
                        )
                    }
                } else {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        itemsIndexed(avatars) { index, avatar ->
                            PremiumAvatarItem(
                                imageUrl = avatar.imageUrl,
                                selected = selectedAvatarIndex == index,
                                onClick = { selectedAvatarIndex = index }
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Warning Box
            PremiumWarningBox()
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Premium Continue Button
            // Enable only when both gender and avatar are selected
            PremiumContinueButton(
                text = "Continue",
                enabled = selectedGender != null && selectedAvatarIndex != null && avatars.isNotEmpty(),
                onClick = {
                    selectedGender?.let { gender ->
                        selectedAvatarIndex?.let { index ->
                            if (index in avatars.indices) {
                                val avatarId = avatars[index].id.toString()
                                viewModel.saveGenderAndContinue(gender, avatarId)
                                
                                // Navigate to Language Selection
                                navController.navigate(Screen.SelectLanguage.route)
                            }
                        }
                    }
                }
            )
            
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

// ============================================
// PREMIUM COMPONENTS
// ============================================

@Composable
fun PremiumGenderCard(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        // Subtle shadow (flat design)
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(y = 2.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(Border.copy(alpha = 0.3f))  // Subtle shadow
        )
        
        // Main Card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.85f)
                .clip(RoundedCornerShape(18.dp))
                .background(White)  // White card background
                .border(
                    width = if (selected) 2.5.dp else 1.dp,
                    color = if (selected) Primary else Border,  // Royal violet when selected
                    shape = RoundedCornerShape(18.dp)
                )
                .clickable(onClick = onClick)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Icon with circular background
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .clip(CircleShape)
                    .background(if (selected) Primary else Surface)  // Violet when selected, subtle white for unselected
                    .border(1.dp, if (selected) Primary else Border, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    modifier = Modifier.size(40.dp),
                    tint = if (selected) White else TextSecondary  // White icon on violet, gray on white
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = label,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,  // Rich black text
                fontSize = 20.sp,
                letterSpacing = 0.5.sp
            )
        }
    }
}

@Composable
fun PremiumAvatarItem(
    imageUrl: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier.size(100.dp)
    ) {
        // Avatar Circle (flat design)
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(Surface)  // Flat surface background for avatar
                .border(
                    width = if (selected) 3.dp else 1.dp,
                    color = if (selected) Primary else Border,  // Violet border when selected
                    shape = CircleShape
                )
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = imageUrl,
                contentDescription = "Avatar",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
        
        // Tick mark above avatar when selected
        if (selected) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .align(Alignment.TopCenter)
                    .offset(y = (-10).dp)
                    .clip(CircleShape)
                    .background(
                        color = Color(0xFF4CAF50),
                        shape = CircleShape
                    )
                    .border(2.dp, White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun PremiumWarningBox() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(AccentLavender)  // Very light violet background
            .border(1.5.dp, Primary, RoundedCornerShape(14.dp))  // Royal violet border
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = "Info",
            tint = Primary,  // Royal violet icon
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Text(
            text = "Gender can't be changed later",
            style = MaterialTheme.typography.bodyMedium,
            color = TextPrimary,  // Dark text
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.3.sp
        )
    }
}

@Composable
fun PremiumContinueButton(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val scale = remember { Animatable(1f) }
    
    LaunchedEffect(enabled) {
        if (enabled) {
            scale.animateTo(
                targetValue = 1.02f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2000, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                )
            )
        }
    }
    
    // Continue button - flat design without shadow
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .scale(scale.value),
        colors = ButtonDefaults.buttonColors(
            containerColor = Primary,  // Royal violet button
            contentColor = White,      // White text
            disabledContainerColor = Border,  // Light gray when disabled
            disabledContentColor = TextTertiary  // Gray text when disabled
        ),
        shape = RoundedCornerShape(14.dp),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 0.dp,  // Flat design - no shadow
            pressedElevation = 2.dp
        )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.sp,
            fontSize = 17.sp,
            color = if (enabled) White else TextTertiary
        )
    }
}

