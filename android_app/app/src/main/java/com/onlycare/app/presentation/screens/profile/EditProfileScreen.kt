package com.onlycare.app.presentation.screens.profile

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.onlycare.app.domain.model.Gender
import com.onlycare.app.domain.model.Interest
import com.onlycare.app.domain.model.Language
import com.onlycare.app.presentation.components.*
import com.onlycare.app.presentation.theme.*
import kotlin.math.absoluteValue

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EditProfileScreen(
    navController: NavController,
    viewModel: EditProfileViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    
    // Load avatars when gender is available
    LaunchedEffect(state.selectedGender) {
        if (state.avatars.isEmpty() && !state.isLoadingAvatars) {
            viewModel.loadAvatars(state.selectedGender)
        }
    }
    
    // Find initial page based on selected avatar
    val initialPage = remember(state.avatars, state.selectedAvatarId) {
        state.avatars.indexOfFirst { it.imageUrl == state.selectedAvatarId }.takeIf { it >= 0 } ?: 0
    }
    
    // Recreate pager state when avatars change
    val pagerState = rememberPagerState(
        initialPage = initialPage,
        pageCount = { state.avatars.size }
    )
    
    // Reset pager when avatars change
    LaunchedEffect(state.avatars.size) {
        if (state.avatars.isNotEmpty() && pagerState.currentPage >= state.avatars.size) {
            pagerState.animateScrollToPage(0)
        }
    }
    
    val interests = Interest.entries.toList()
    
    // Handle success - navigate back
    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) {
            viewModel.resetSuccess()
            navController.navigateUp()
        }
    }
    
    // Show elegant error dialog if there's an error
    if (state.error != null) {
        ErrorDialog(
            error = state.error ?: "",
            onDismiss = { viewModel.clearError() }
        )
    }
    
    Scaffold(
        containerColor = Background,
        topBar = {
            OnlyCareTopAppBar(
                title = "Edit Profile",
                onBackClick = { navController.navigateUp() }
            )
        }
    ) { paddingValues ->
        // Show loading indicator while fetching user data
        if (state.isLoadingUserData) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Background)
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Primary)
            }
            return@Scaffold
        }
        
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Background)
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                // Enhanced Avatar Selection Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 0.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = ThemeSurface
                    ),
                    shape = RoundedCornerShape(28.dp),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 0.dp
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Enhanced Title Section
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Choose Your Avatar",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                fontSize = 22.sp
                            )
                            
                            Spacer(modifier = Modifier.height(6.dp))
                            
                            Box(
                                modifier = Modifier
                                    .width(60.dp)
                                    .height(3.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(Primary)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(28.dp))
                        
                        // Enhanced Avatar Carousel - Clean & Symmetrical
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            if (state.isLoadingAvatars) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(320.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(color = Primary)
                                }
                            } else if (state.avatars.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(320.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "No avatars available",
                                        color = TextSecondary,
                                        fontSize = 16.sp
                                    )
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(320.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    HorizontalPager(
                                        state = pagerState,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(320.dp),
                                        contentPadding = PaddingValues(horizontal = 50.dp),
                                        pageSpacing = 32.dp
                                    ) { page ->
                                        val avatar = state.avatars[page]
                                        val pageOffset = (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
                                        val scale = 1f - (pageOffset.absoluteValue * 0.3f).coerceIn(0f, 0.3f)
                                        val alpha = 1f - (pageOffset.absoluteValue * 0.7f).coerceIn(0f, 0.7f)
                                        val isSelected = pageOffset.absoluteValue < 0.1f
                                        
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier.scale(scale)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(300.dp)
                                                    .aspectRatio(1f),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                // Enhanced glowing effects for selected avatar
                                                if (isSelected) {
                                                    // Outer glow - larger halo
                                                    Box(
                                                        modifier = Modifier
                                                            .size(310.dp)
                                                            .aspectRatio(1f)
                                                            .clip(CircleShape)
                                                            .background(White.copy(alpha = 0.12f))
                                                    )
                                                    
                                                    // Middle glow
                                                    Box(
                                                        modifier = Modifier
                                                            .size(300.dp)
                                                            .aspectRatio(1f)
                                                            .clip(CircleShape)
                                                            .background(White.copy(alpha = 0.18f))
                                                    )
                                                    
                                                    // Premium white border ring
                                                    Box(
                                                        modifier = Modifier
                                                            .size(286.dp)
                                                            .aspectRatio(1f)
                                                            .clip(CircleShape)
                                                            .background(White)
                                                            .padding(5.dp),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Box(
                                                            modifier = Modifier
                                                                .fillMaxSize()
                                                                .aspectRatio(1f)
                                                                .clip(CircleShape)
                                                                .background(ElevatedSurface)
                                                        )
                                                    }
                                                }
                                                
                                                // Avatar Circle with gradient background
                                                Box(
                                                    modifier = Modifier
                                                        .size(276.dp)
                                                        .aspectRatio(1f)
                                                        .clip(CircleShape)
                                                        .background(
                                                            if (isSelected) {
                                                                Surface
                                                            } else {
                                                                CardBackground
                                                            }
                                                        )
                                                        .border(
                                                            width = if (isSelected) 0.dp else 1.dp,
                                                            color = if (isSelected) Color.Transparent else BorderPrimary,
                                                            shape = CircleShape
                                                        ),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    AsyncImage(
                                                        model = avatar.imageUrl,
                                                        contentDescription = "Avatar",
                                                        contentScale = ContentScale.Crop,
                                                        modifier = Modifier
                                                            .fillMaxSize()
                                                            .graphicsLayer {
                                                                this.alpha = alpha
                                                            }
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(20.dp))
                                
                                // Page indicators (dots)
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    repeat(state.avatars.size) { index ->
                                        val isSelected = pagerState.currentPage == index
                                        Box(
                                            modifier = Modifier
                                                .size(
                                                    width = if (isSelected) 24.dp else 6.dp,
                                                    height = 6.dp
                                                )
                                                .clip(RoundedCornerShape(3.dp))
                                                .background(
                                                    if (isSelected) White else TextTertiary.copy(alpha = 0.4f)
                                                )
                                        )
                                    }
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(18.dp))
                        
                        // Enhanced instruction with icon
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowLeft,
                                contentDescription = null,
                                tint = TextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Swipe to choose your avatar",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowRight,
                                contentDescription = null,
                                tint = TextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(28.dp))
            }
            
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    OnlyCareTextField(
                        value = state.username,
                        onValueChange = { viewModel.onUsernameChange(it) },
                        label = "Display Name",
                        leadingIcon = Icons.Default.Person,
                        error = state.usernameError,
                        enabled = !state.isLoading
                    )
                    
                    // Display name requirements text
                    if (state.usernameError == null) {
                        Text(
                            text = "This is the name others will see",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
            }
            
            // Gender Display (Read-only)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = ThemeSurface),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (state.selectedGender == Gender.MALE) Icons.Default.Man else Icons.Default.Woman,
                            contentDescription = null,
                            tint = Primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Gender",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = state.selectedGender.name.lowercase().replaceFirstChar { it.uppercase() },
                                style = MaterialTheme.typography.bodyLarge,
                                color = TextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Preferred Language Display (Read-only)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = ThemeSurface),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = null,
                            tint = Primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Preferred Language",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = state.selectedLanguage.displayName,
                                style = MaterialTheme.typography.bodyLarge,
                                color = TextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
            }
            
            item {
                Text(
                    text = "Interests",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(12.dp))
            }
            
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    interests.chunked(2).forEach { rowInterests ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            rowInterests.forEach { interest ->
                                InterestChip(
                                    text = interest.displayName,
                                    icon = interest.icon,
                                    selected = state.selectedInterests.contains(interest),
                                    onClick = {
                                        if (!state.isLoading) {
                                            viewModel.onInterestToggle(interest)
                                        }
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            // Fill remaining space if odd number of interests
                            repeat(2 - rowInterests.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
            }
            
            item {
                OnlyCarePrimaryButton(
                    text = if (state.isLoading) "Saving..." else "Save Changes",
                    onClick = {
                        // Update avatar selection before saving
                        if (state.avatars.isNotEmpty() && pagerState.currentPage < state.avatars.size) {
                            val selectedAvatar = state.avatars[pagerState.currentPage]
                            viewModel.onAvatarSelected(selectedAvatar.id.toString())  // Pass avatar ID instead of URL
                        }
                        viewModel.saveProfile()
                    },
                    enabled = !state.isLoading && state.usernameError == null && state.avatars.isNotEmpty()
                )
                
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun ErrorDialog(
    error: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ThemeSurface,
        shape = RoundedCornerShape(24.dp),
        title = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.ErrorOutline,
                    contentDescription = null,
                    tint = Color(0xFFFF6B6B),
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Oops!",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                // Parse and display error message in user-friendly format
                val errorMessage = parseErrorMessage(error)
                
                Text(
                    text = errorMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    fontSize = 15.sp,
                    lineHeight = 22.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                OnlyCarePrimaryButton(
                    text = "Got it",
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    )
}

/**
 * Parses raw error JSON/text into user-friendly message
 */
fun parseErrorMessage(error: String): String {
    return try {
        when {
            // Handle validation error format
            error.contains("username") && error.contains("string") -> {
                "Please enter a valid username (4-10 characters with letters and numbers)"
            }
            error.contains("name") && error.contains("string") -> {
                "Your profile needs a name. Please contact support if this persists."
            }
            error.contains("name") && error.contains("2 characters") -> {
                "Your profile needs a valid name. Please contact support if this persists."
            }
            error.contains("age") && error.contains("integer") -> {
                "Age must be a valid number (18+)"
            }
            error.contains("username") && error.contains("unique") -> {
                "This username is already taken. Please choose another one."
            }
            error.contains("VALIDATION_ERROR") || error.contains("validation") -> {
                "Please check your information and try again"
            }
            error.contains("network") || error.contains("Network") -> {
                "Connection issue. Please check your internet and try again."
            }
            error.contains("timeout") || error.contains("Timeout") -> {
                "Request timed out. Please try again."
            }
            error.contains("500") || error.contains("Internal Server Error") -> {
                "Something went wrong on our end. Please try again in a moment."
            }
            error.contains("401") || error.contains("Unauthorized") -> {
                "Session expired. Please log in again."
            }
            error.length > 200 -> {
                "Unable to save changes. Please try again."
            }
            else -> {
                // Clean up the message if it's somewhat readable
                error.replace("\"", "")
                    .replace("{", "")
                    .replace("}", "")
                    .replace("[", "")
                    .replace("]", "")
                    .take(150)
                    .trim()
                    .ifEmpty { "Unable to save changes. Please try again." }
            }
        }
    } catch (e: Exception) {
        "Unable to save changes. Please try again."
    }
}

