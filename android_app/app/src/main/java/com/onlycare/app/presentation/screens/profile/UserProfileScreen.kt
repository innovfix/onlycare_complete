package com.onlycare.app.presentation.screens.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.onlycare.app.presentation.components.*
import com.onlycare.app.presentation.theme.*
import com.onlycare.app.utils.getDisplayName
import androidx.compose.ui.graphics.vector.ImageVector
import android.widget.Toast

@Composable
fun UserProfileScreen(
    navController: NavController,
    viewModel: UserProfileViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    // Friend request error toast
    LaunchedEffect(state.friendRequestError) {
        state.friendRequestError?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            viewModel.clearFriendRequestError()
        }
    }

    // Friend request success toast
    LaunchedEffect(state.friendRequestSent) {
        if (state.friendRequestSent) {
            Toast.makeText(context, "Friend request sent!", Toast.LENGTH_SHORT).show()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        // Back button at top - using IconButton for reliable touch handling
        IconButton(
            onClick = { 
                navController.navigateUp()
            },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
                .statusBarsPadding()
                .size(48.dp)
                .zIndex(10f) // Ensure button is above all other content
                .background(
                    color = PrimaryLight.copy(alpha = 0.55f),
                    shape = RoundedCornerShape(24.dp)
                )
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = Primary,
                modifier = Modifier.size(24.dp)
            )
        }

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
                        tint = Primary,
                        modifier = Modifier.size(64.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = state.error ?: "Unknown error",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        modifier = Modifier.padding(horizontal = 16.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    
                    if (state.error?.contains("not found", ignoreCase = true) == true) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "This user may not exist or has been deleted",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                            modifier = Modifier.padding(horizontal = 16.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    OnlyCarePrimaryButton(
                        text = "Retry",
                        onClick = { viewModel.retry() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 48.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Button(
                        onClick = { navController.navigateUp() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            contentColor = Primary
                        )
                    ) {
                        Text("Go Back")
                    }
                }
            }

            state.user != null -> {
                val user = state.user!!

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Background)
                ) {
                    // Profile Header
                    item {
                        Spacer(modifier = Modifier.height(56.dp)) // Space for back button
                    }

                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            ProfileImage(
                                imageUrl = user.profileImage,
                                size = 120.dp,
                                showOnlineStatus = true,
                                isOnline = user.isOnline
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = user.getDisplayName(),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )

                            Text(
                                text = "${user.age} years",
                                style = MaterialTheme.typography.bodyLarge,
                                color = TextSecondary
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            if (user.rating > 0f) {
                                RatingBar(rating = user.rating, size = 20.dp)
                            }
                        }
                    }

                    // About
                    item {
                        Divider(color = PrimaryLight.copy(alpha = 0.7f))

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp)
                        ) {
                            Text(
                                text = "About",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = user.bio.ifEmpty { "No bio available" },
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }

                        Divider(color = PrimaryLight.copy(alpha = 0.7f))
                    }

                    // Interests
                    if (user.interests.isNotEmpty()) {
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp)
                            ) {
                                Text(
                                    text = "Interests",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(user.interests) { interest ->
                                        InterestChip(
                                            text = interest.uppercase(),
                                            icon = getInterestIcon(interest),
                                            selected = false
                                        )
                                    }
                                }
                            }
                        }
                        item {
                            Divider(color = PrimaryLight.copy(alpha = 0.7f))
                        }
                    }

                    // Language
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp)
                        ) {
                            Text(
                                text = "Language",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Premium Language Chip - Symmetric and Professional
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                color = ThemeSurface,
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(
                                    width = 1.dp,
                                    color = PrimaryLight
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 16.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Flat icon container (no shadow/blur)
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(PrimaryLight.copy(alpha = 0.25f))
                                            .border(1.dp, PrimaryLight, RoundedCornerShape(10.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Language,
                                            contentDescription = "Language",
                                            tint = Primary,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(14.dp))

                                    // Language name - Centered
                                    Text(
                                        text = user.language.displayName,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextPrimary,
                                        letterSpacing = 0.3.sp
                                    )
                                }
                            }
                        }
                    }

                    // Send Friend Request button (themed)
                    item {
                        Spacer(modifier = Modifier.height(12.dp))

                        OnlyCarePrimaryButton(
                            text = if (state.friendRequestSent) "Request Sent" else "Send Friend Request",
                            onClick = { viewModel.sendFriendRequest() },
                            enabled = !state.friendRequestSent,
                            loading = state.isSendingFriendRequest,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp)
                        )

                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}

/**
 * Get premium icon for interest based on interest name
 */
private fun getInterestIcon(interest: String): ImageVector {
    return when (interest.uppercase()) {
        "MUSIC" -> Icons.Default.MusicNote
        "MOVIES" -> Icons.Default.Movie
        "SPORTS" -> Icons.Default.SportsSoccer
        "GAMING" -> Icons.Default.SportsEsports
        "TRAVEL" -> Icons.Default.Flight
        "FOOD" -> Icons.Default.Restaurant
        "PHOTOGRAPHY" -> Icons.Default.CameraAlt
        "ART" -> Icons.Default.Palette
        "BOOKS" -> Icons.Default.MenuBook
        "FITNESS" -> Icons.Default.FitnessCenter
        "TECHNOLOGY" -> Icons.Default.Computer
        "FASHION" -> Icons.Default.Checkroom
        "DANCE" -> Icons.Default.MusicNote
        "READING" -> Icons.Default.MenuBook
        "COOKING" -> Icons.Default.Restaurant
        "WRITING" -> Icons.Default.Create
        "SINGING" -> Icons.Default.Mic
        "YOGA" -> Icons.Default.FitnessCenter
        "MEDITATION" -> Icons.Default.Favorite
        "SHOPPING" -> Icons.Default.ShoppingCart
        else -> Icons.Default.Circle // Default premium icon
    }
}

