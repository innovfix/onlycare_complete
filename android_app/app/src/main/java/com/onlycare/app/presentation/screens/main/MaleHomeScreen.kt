package com.onlycare.app.presentation.screens.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.Circle
import kotlinx.coroutines.launch
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.collectAsState
import com.onlycare.app.domain.model.User
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.onlycare.app.presentation.components.ProfileImage
import com.onlycare.app.presentation.components.OnlyCareSoftShadowContainer
import com.onlycare.app.presentation.components.OnlyCareListItemSkeleton
import com.onlycare.app.presentation.theme.AccentLavender
import com.onlycare.app.presentation.theme.Border as ThemeBorder
import com.onlycare.app.presentation.theme.CardBackground
import com.onlycare.app.presentation.theme.CallGreen
import com.onlycare.app.presentation.theme.OnlineGreen
import com.onlycare.app.presentation.theme.Primary
import com.onlycare.app.presentation.theme.PrimaryDark
import com.onlycare.app.presentation.theme.PrimaryLight
import com.onlycare.app.presentation.theme.Surface as ThemeSurface
import com.onlycare.app.presentation.theme.TextPrimary
import com.onlycare.app.presentation.theme.TextSecondary
import com.onlycare.app.presentation.theme.TextTertiary
import com.onlycare.app.presentation.theme.White
import com.onlycare.app.presentation.theme.Background
import com.onlycare.app.utils.getDisplayName


/* ------------------------------------
 * Small shared types
 * ------------------------------------ */
enum class CallType { Audio, Video }

enum class AvailabilityStatus {
    AVAILABLE, OFFLINE
}

data class CreatorUi(
    val id: String,
    val name: String,
    val age: Int,
    val rating: Float,
    val online: Boolean,
    val profileImage: String? = null,
    val bio: String = "",
    val language: String = "English",
    val interests: List<String> = emptyList(),
    val audioRate: Int = 10, // coins per minute
    val videoRate: Int = 60, // coins per minute
    val originalAudioRate: Int? = null, // for showing discount
    val originalVideoRate: Int? = null,
    val availabilityStatus: AvailabilityStatus = AvailabilityStatus.AVAILABLE,
    val lastActiveMinutes: Int? = null, // minutes ago for offline users
    val responseTimeMinutes: Int = 5,
    val reviewCount: Int = 0,
    val lastReview: String = "",
    val badge: String? = null, // "NEW", "POPULAR", "TOP RATED"
    val isVerified: Boolean = true,
    val isFirstCallDiscount: Boolean = false,
    val completedCalls: Int = 0,
    val responseRate: Int = 95, // percentage
    val isFavorite: Boolean = false,
    val isAudioEnabled: Boolean = true,
    val isVideoEnabled: Boolean = true
)

/* ------------------------------------
 * Helper function to convert User to CreatorUi
 * ------------------------------------ */
private fun User.toCreatorUi(fallbackProfileImageUrl: String? = null): CreatorUi {
    // Product rule for Male list:
    // - If creator enables at least one call type (audio/video), treat them as "Online/Available".
    // - If creator disables both, they must be hidden from the list (handled by caller-side filter),
    //   and should not show as "Offline" when their buttons are enabled.
    val isAvailableByToggles = this.audioCallEnabled || this.videoCallEnabled

    // Use the utility function to get consistent display name across all screens
    val displayName = this.getDisplayName()
    
    return CreatorUi(
        id = this.id,
        name = displayName,
        age = this.age,
        rating = this.rating,
        online = isAvailableByToggles,
        profileImage = this.profileImage.ifBlank { fallbackProfileImageUrl.orEmpty() },
        bio = this.bio,
        language = this.language.displayName,
        interests = this.interests,
        audioRate = 10, // Default rate, can be fetched from settings
        videoRate = 60, // Default rate, can be fetched from settings
        originalAudioRate = null,
        originalVideoRate = null,
        availabilityStatus = if (isAvailableByToggles) AvailabilityStatus.AVAILABLE else AvailabilityStatus.OFFLINE,
        lastActiveMinutes = null,
        responseTimeMinutes = 5,
        reviewCount = 0,
        lastReview = "",
        badge = if (this.isVerified) "VERIFIED" else null,
        isVerified = this.isVerified,
        isFirstCallDiscount = false,
        completedCalls = 0,
        responseRate = 95,
        isFavorite = false,
        isAudioEnabled = this.audioCallEnabled,
        isVideoEnabled = this.videoCallEnabled
    )
}

/* ------------------------------------
 * Demo data - NO LONGER USED
 * All data now comes from API via state.femaleUsers
 * ------------------------------------ */
/*
private val demoCreators_REMOVED = listOf(
    CreatorUi(
        id = "1",
        name = "Alina Kapoor",
        age = 24,
        rating = 4.7f,
        online = true,
        bio = "I love cooking and good conversations",
        language = "Hindi",
        interests = listOf("Travel", "Cooking", "Music", "Food"),
        audioRate = 10,
        videoRate = 60,
        originalAudioRate = 15,
        originalVideoRate = 80,
        availabilityStatus = AvailabilityStatus.AVAILABLE,
        responseTimeMinutes = 2,
        reviewCount = 1234,
        lastReview = "Amazing conversation! Very friendly and engaging.",
        badge = "POPULAR",
        isVerified = true,
        isFirstCallDiscount = true,
        completedCalls = 856,
        responseRate = 98,
        isFavorite = false
    ),
    CreatorUi(
        id = "2",
        name = "Rhea Thomas",
        age = 22,
        rating = 4.2f,
        online = false,
        bio = "Movie buff and foodie",
        language = "Tamil",
        interests = listOf("Movies", "Art", "Photography"),
        audioRate = 8,
        videoRate = 50,
        availabilityStatus = AvailabilityStatus.OFFLINE,
        lastActiveMinutes = 45,
        responseTimeMinutes = 5,
        reviewCount = 892,
        lastReview = "Great movie recommendations!",
        badge = null,
        isVerified = true,
        isFirstCallDiscount = false,
        completedCalls = 654,
        responseRate = 95,
        isFavorite = true
    ),
    CreatorUi(
        id = "3",
        name = "Mira Khan",
        age = 26,
        rating = 5.0f,
        online = true,
        bio = "Fashion enthusiast and travel lover",
        language = "English",
        interests = listOf("Fashion", "Travel", "Shopping", "Style"),
        audioRate = 15,
        videoRate = 80,
        originalAudioRate = 20,
        originalVideoRate = 100,
        availabilityStatus = AvailabilityStatus.AVAILABLE,
        responseTimeMinutes = 3,
        reviewCount = 2145,
        lastReview = "Best fashion advice ever! So professional.",
        badge = "TOP RATED",
        isVerified = true,
        isFirstCallDiscount = true,
        completedCalls = 1243,
        responseRate = 99,
        isFavorite = false
    ),
    CreatorUi(
        id = "4",
        name = "Lina Trivedi",
        age = 23,
        rating = 4.6f,
        online = true,
        bio = "Yoga and wellness coach",
        language = "Hindi",
        interests = listOf("Yoga", "Fitness", "Meditation"),
        audioRate = 12,
        videoRate = 70,
        availabilityStatus = AvailabilityStatus.OFFLINE,
        responseTimeMinutes = 10,
        reviewCount = 567,
        lastReview = "Very calming presence, helped me a lot.",
        badge = null,
        isVerified = true,
        isFirstCallDiscount = false,
        completedCalls = 423,
        responseRate = 92,
        isFavorite = false
    ),
    CreatorUi(
        id = "5",
        name = "Kavya Singh",
        age = 21,
        rating = 4.5f,
        online = true,
        bio = "Dance and music passionate",
        language = "Punjabi",
        interests = listOf("Dance", "Music", "Singing"),
        audioRate = 10,
        videoRate = 60,
        availabilityStatus = AvailabilityStatus.AVAILABLE,
        responseTimeMinutes = 5,
        reviewCount = 345,
        lastReview = "So energetic and fun to talk to!",
        badge = "NEW",
        isVerified = true,
        isFirstCallDiscount = true,
        completedCalls = 234,
        responseRate = 96,
        isFavorite = true
    ),
    CreatorUi(
        id = "6",
        name = "Diya Verma",
        age = 20,
        rating = 4.4f,
        online = false,
        bio = "Books and coffee lover",
        language = "English",
        interests = listOf("Reading", "Coffee", "Writing"),
        audioRate = 8,
        videoRate = 45,
        availabilityStatus = AvailabilityStatus.OFFLINE,
        lastActiveMinutes = 120,
        responseTimeMinutes = 8,
        reviewCount = 678,
        lastReview = "Lovely book discussions!",
        badge = null,
        isVerified = true,
        isFirstCallDiscount = false,
        completedCalls = 512,
        responseRate = 94,
        isFavorite = false
    ),
)
*/

/* ------------------------------------
 * Local theme - Dostt Purple Light Theme
 * ------------------------------------ */
@Composable
private fun OnlyCareLocalTheme(content: @Composable () -> Unit) {
    val light = lightColorScheme(
        primary = Primary,                     // Royal Violet #8B5CF6
        onPrimary = White,
        primaryContainer = PrimaryLight,       // Light Violet #C4B5FD
        onPrimaryContainer = PrimaryDark,      // Deep Violet #7C3AED
        secondary = TextSecondary,             // Medium gray
        onSecondary = White,
        tertiary = PrimaryDark,                // Deep Violet
        surface = ThemeSurface,                // Subtle warm white #FAFAF9
        surfaceVariant = White,
        background = White,
        onSurface = TextPrimary,               // Rich black #18181B
        onSurfaceVariant = TextSecondary,      // Medium gray #71717A
        outline = ThemeBorder                  // Light gray #E4E4E7
    )
    MaterialTheme(
        colorScheme = light,
        content = content
    )
}

/* ------------------------------------
 * PUBLIC API — keep signature the same
 * ------------------------------------ */
@Composable
fun MaleHomeScreen(
    onOpenMessages: () -> Unit,
    onOpenRecent: () -> Unit,        // kept for wiring (not used here)
    onOpenProfileTab: () -> Unit,    // kept for wiring (not used here)
    onOpenProfile: (String, String) -> Unit,
    onNavigateToWallet: () -> Unit = {},
    onAudioCall: (String) -> Unit = {},  // userId -> navigate to call
    onVideoCall: (String) -> Unit = {},  // userId -> navigate to call
    onStartRandomCall: (callType: String, candidateUserIds: List<String>) -> Unit = { _, _ -> },
    viewModel: MaleHomeViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var showRandomDialog by remember { mutableStateOf(false) }
    var showRandomMenu by remember { mutableStateOf(false) }
    val currentBalance = state.coinBalance
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    
    // Convert API users to CreatorUi for rendering
    val creators = remember(state.femaleUsers, state.femaleAvatarUrls) {
        val fallbacks = state.femaleAvatarUrls
        // Product rule:
        // 1. Hide creators who have turned OFF both audio + video availability.
        // 2. Only show creators who have at least one call type enabled (Audio or Video).
        // 3. New registers must remain hidden until they flip a switch (handled by default OFF logic).
        state.femaleUsers
            .filter { user -> 
                // Ensure at least one call type is enabled
                user.audioCallEnabled || user.videoCallEnabled
            }
            .map { user ->
            val fallback = if (fallbacks.isNotEmpty() && user.profileImage.isBlank()) {
                val idx = kotlin.math.abs(user.id.hashCode()) % fallbacks.size
                fallbacks[idx]
            } else null
            user.toCreatorUi(fallbackProfileImageUrl = fallback)
            }
    }
    
    // Call API when screen is first shown
    LaunchedEffect(Unit) {
        viewModel.updateOnlineDatetime()
    }
    
    // Call API when screen resumes (comes back from background or other screens)
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.updateOnlineDatetime()
                // Ensure creators load immediately when returning from Random Call / Call screens.
                // Otherwise users may appear with delay (auto-refresh is 30s).
                viewModel.refresh()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    
    OnlyCareLocalTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                // User request: pure white background behind all content
                .background(Background)
        ) {
            // Subtle particle effects
            PremiumParticles()
            
            val swipeRefreshState = rememberSwipeRefreshState(isRefreshing = state.isRefreshing)
            
            SwipeRefresh(
                state = swipeRefreshState,
                onRefresh = { viewModel.refresh() },
                modifier = Modifier.fillMaxSize()
            ) {
                Column(
                    Modifier.fillMaxSize()
                ) {

            // Header - white card with soft shadow (like cards), strong separation from background
            val headerShape = RoundedCornerShape(18.dp)
            OnlyCareSoftShadowContainer(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 0.dp, vertical = 4.dp),
                shape = headerShape,
                shadowOffsetY = 6.dp,
                leftShadowOffsetX = (-3).dp,
                rightShadowOffsetX = 3.dp,
                topShadowOffsetY = (-3).dp,
                shadowColor = ThemeBorder.copy(alpha = 0.34f)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(headerShape)
                        .background(White)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    // Feelin's-like: app name (left) + coin chip (right). No emoji/avatar.
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Only Care",
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontSize = 30.sp,
                                letterSpacing = (-0.2).sp
                            ),
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.SansSerif,
                            color = Primary
                        )

                        PremiumWalletChip(
                            coins = currentBalance,
                            onAdd = onNavigateToWallet,
                            onClick = onNavigateToWallet
                        )
                    }
                }
            }

            // Handle loading, error, and success states
            when {
                state.isLoading -> {
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(6) {
                            OnlyCareSoftShadowContainer(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                shadowOffsetY = 4.dp,
                                shadowColor = ThemeBorder.copy(alpha = 0.28f),
                                showSideShadows = true
                            ) {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = ThemeSurface),
                                    shape = RoundedCornerShape(20.dp),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.dp, PrimaryLight, RoundedCornerShape(20.dp))
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        // Roughly matches the card visual weight while loading.
                                        OnlyCareListItemSkeleton()
                                        Spacer(modifier = Modifier.height(8.dp))
                                        OnlyCareListItemSkeleton()
                                    }
                                }
                            }
                        }
                    }
                }
                
                state.error != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Text(
                                text = state.error ?: "Failed to load creators",
                                style = MaterialTheme.typography.bodyLarge,
                                color = TextSecondary,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                            Button(
                                onClick = { viewModel.loadFemaleUsers() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Primary
                                )
                            ) {
                                Text("Retry", color = Color.White)
                            }
                        }
                    }
                }
                
                creators.isNotEmpty() -> {
                    // Vertical list - one card per row for premium look
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(creators.size, key = { creators[it].id }) { index ->
                            val c = creators[index]
                            CreatorCard(
                                creator = c,
                                animationDelay = index * 120L, // Staggered entrance
                                onAudio = { onAudioCall(c.id) },
                                onVideo = { onVideoCall(c.id) },
                                onOpenProfile = { onOpenProfile(c.id, c.name) }
                            )
                        }
                    }
                }
                
                else -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No creators available",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
        }
            
            // Premium Random FAB
            PremiumRandomFAB(
                expanded = showRandomMenu,
                onToggle = { showRandomMenu = !showRandomMenu },
                onClose = { showRandomMenu = false },
                onAudio = {
                    showRandomMenu = false
                    val minRequiredCoins = 10
                    if (currentBalance >= minRequiredCoins) {
                        val candidateIds = state.femaleUsers
                            .filter { it.isOnline && it.audioCallEnabled }
                            .map { it.id }
                            .filter { it.isNotBlank() }
                            .distinct()
                            .shuffled()
                        if (candidateIds.isNotEmpty()) {
                            onStartRandomCall("audio", candidateIds)
                        } else {
                            android.widget.Toast.makeText(
                                context.applicationContext,
                                "No online creators available right now",
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        onNavigateToWallet()
                    }
                },
                onVideo = {
                    showRandomMenu = false
                    val minRequiredCoins = 60
                    if (currentBalance >= minRequiredCoins) {
                        val candidateIds = state.femaleUsers
                            .filter { it.isOnline && it.videoCallEnabled }
                            .map { it.id }
                            .filter { it.isNotBlank() }
                            .distinct()
                            .shuffled()
                        if (candidateIds.isNotEmpty()) {
                            onStartRandomCall("video", candidateIds)
                        } else {
                            android.widget.Toast.makeText(
                                context.applicationContext,
                                "No online creators available right now",
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        onNavigateToWallet()
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    // Reduce left/right/bottom padding so it doesn't feel too far inset
                    .padding(end = 14.dp, bottom = 14.dp)
            )
            
            // Random Call Dialog
            if (showRandomDialog) {
                RandomCallDialog(
                    onDismiss = { showRandomDialog = false },
                    onAudioCall = {
                        showRandomDialog = false
                        // Check balance for audio call (assume 10 coins/min minimum)
                        val minRequiredCoins = 10
                        if (currentBalance >= minRequiredCoins) {
                            // Random call queue: online creators, one-by-one, 10s each
                            val candidateIds = state.femaleUsers
                                .filter { it.isOnline && it.audioCallEnabled }
                                .map { it.id }
                                .filter { it.isNotBlank() }
                                .distinct()
                                .shuffled()

                            if (candidateIds.isNotEmpty()) {
                                onStartRandomCall("audio", candidateIds)
                            }
                        } else {
                            // Navigate immediately with message
                            onNavigateToWallet()
                        }
                    },
                    onVideoCall = {
                        showRandomDialog = false
                        // Check balance for video call (assume 60 coins/min minimum)
                        val minRequiredCoins = 60
                        if (currentBalance >= minRequiredCoins) {
                            // Random call queue: online creators, one-by-one, 10s each
                            val candidateIds = state.femaleUsers
                                .filter { it.isOnline && it.videoCallEnabled }
                                .map { it.id }
                                .filter { it.isNotBlank() }
                                .distinct()
                                .shuffled()

                            if (candidateIds.isNotEmpty()) {
                                onStartRandomCall("video", candidateIds)
                            }
                        } else {
                            // Navigate immediately with message
                            onNavigateToWallet()
                        }
                    }
                )
            }
        }
    }
}

/* ------------------------------------
 * Header bits
 * ------------------------------------ */
@Composable
private fun PremiumWalletChip(
    coins: Int,
    onAdd: () -> Unit,
    onClick: () -> Unit = {}
) {
    // Breathing animation for plus button
    val plusScale = remember { Animatable(1f) }
    val starAlpha = remember { Animatable(1f) }
    
    LaunchedEffect(Unit) {
        launch {
            while (true) {
                plusScale.animateTo(
                    targetValue = 1.08f,
                    animationSpec = tween(1500, easing = FastOutSlowInEasing)
                )
                plusScale.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(1500, easing = FastOutSlowInEasing)
                )
            }
        }
        launch {
            while (true) {
                starAlpha.animateTo(
                    targetValue = 0.6f,
                    animationSpec = tween(1000, easing = FastOutSlowInEasing)
                )
                starAlpha.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(1000, easing = FastOutSlowInEasing)
                )
            }
        }
    }
    
    // Wallet chip - flat (no gradients, no border)
    Row(
        modifier = Modifier
            .height(40.dp)
            .clip(RoundedCornerShape(100))
            .background(AccentLavender)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
            // Coin icon with glow effect
            Box {
                // Glow layer
        Icon(
            imageVector = Icons.Default.Circle,
            contentDescription = null,
                    tint = Color(0xFFFFD700).copy(alpha = 0.3f * starAlpha.value),
                    modifier = Modifier.size(22.dp)
                )
                // Main coin
                Icon(
                    imageVector = Icons.Default.Circle,
                    contentDescription = null,
                    tint = Color(0xFFFFD700).copy(alpha = starAlpha.value),
                    modifier = Modifier.size(20.dp)
                )
            }
            
        Spacer(Modifier.width(8.dp))
            
        Text(
            text = coins.toString(),
            style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
        )
        Spacer(Modifier.width(10.dp))
            
            // Add button with breathing animation
            Box(modifier = Modifier.scale(plusScale.value)) {
                // Main button
        Surface(
            shape = RoundedCornerShape(100),
                    color = PrimaryDark,
            modifier = Modifier.size(30.dp),
            onClick = onAdd
        ) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Filled.Add, 
                            contentDescription = "Add",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MessagesButton(
    badgeCount: Int,
    onClick: () -> Unit
) {
    BadgedBox(
        badge = {
            if (badgeCount > 0) {
                Badge(
                    containerColor = PrimaryLight,
                    contentColor = PrimaryDark,
                    modifier = Modifier.offset(y = 3.dp)
                ) {
                    Text(if (badgeCount > 9) "9+" else badgeCount.toString())
                }
            }
        }
    ) {
        Surface(
            shape = RoundedCornerShape(100),
            color = ThemeSurface,
            modifier = Modifier
                .size(44.dp),
            onClick = onClick
        ) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Filled.Chat, 
                    contentDescription = "Messages",
                    tint = Primary
                )
            }
        }
    }
}

/* ------------------------------------
 * SIMPLE CREATOR CARD - HORIZONTAL LAYOUT WITH PREMIUM ANIMATED BORDER
 * ------------------------------------ */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatorCard(
    creator: CreatorUi,
    animationDelay: Long = 0L,
    onAudio: () -> Unit,
    onVideo: () -> Unit,
    onOpenProfile: () -> Unit
) {
    val isAudioActive = creator.isAudioEnabled
    val isVideoActive = creator.isVideoEnabled

    // Feelin’s-style “hero” card (list item)
    OnlyCareSoftShadowContainer(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        shadowOffsetY = 5.dp,
        shadowColor = ThemeBorder.copy(alpha = 0.28f),
        showSideShadows = true
    ) {
        Card(
            onClick = onOpenProfile,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = White),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column {
                // HERO IMAGE AREA
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(0.78f)
                        .background(PrimaryLight.copy(alpha = 0.12f))
                ) {
                    if (!creator.profileImage.isNullOrBlank()) {
                        AsyncImage(
                            model = creator.profileImage,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Filled.Person,
                                contentDescription = null,
                                tint = Primary,
                                modifier = Modifier.size(64.dp)
                            )
                        }
                    }

                    // Online pill (top-left)
                    FeelinStatusPill(
                        online = creator.online,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(14.dp)
                    )

                    // Bottom overlay: fade + texts + buttons (no white tail section)
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.Black.copy(alpha = 0.78f)
                                    )
                                )
                            )
                            .padding(horizontal = 18.dp, vertical = 14.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = creator.name,
                                color = White,
                                fontSize = 30.sp,
                                fontWeight = FontWeight.ExtraBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            if (creator.bio.isNotBlank()) {
                                Text(
                                    text = creator.bio,
                                    color = White.copy(alpha = 0.92f),
                                    fontSize = 14.sp,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    lineHeight = 19.sp
                                )
                            }

                            val interestsText = creator.interests
                                .filter { it.isNotBlank() }
                                .take(3)
                                .joinToString("  |  ")
                            if (interestsText.isNotBlank()) {
                                Text(
                                    text = interestsText,
                                    color = White.copy(alpha = 0.90f),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                FeelinCallButton(
                                    enabled = isAudioActive,
                                    titleIcon = Icons.Filled.Call,
                                    ratePerMin = creator.audioRate,
                                    minutes = 0,
                                    containerColor = Primary,
                                    // Less faded when disabled (still non-clickable)
                                    disabledColor = ThemeBorder.copy(alpha = 0.85f),
                                    onClick = onAudio,
                                    modifier = Modifier.weight(1f)
                                )

                                FeelinCallButton(
                                    enabled = isVideoActive,
                                    titleIcon = Icons.Filled.Videocam,
                                    ratePerMin = creator.videoRate,
                                    minutes = 0,
                                    containerColor = Primary,
                                    // Less faded when disabled (still non-clickable)
                                    disabledColor = ThemeBorder.copy(alpha = 0.85f),
                                    onClick = onVideo,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FeelinStatusPill(
    online: Boolean,
    modifier: Modifier = Modifier
) {
    val bg = White.copy(alpha = 0.92f)
    val dot = if (online) OnlineGreen else ThemeBorder
    val label = if (online) "Online" else "Offline"

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(999.dp),
        color = bg,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(dot)
            )
            Text(
                text = label,
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun FeelinCallButton(
    enabled: Boolean,
    titleIcon: ImageVector,
    ratePerMin: Int,
    minutes: Int,
    containerColor: Color,
    disabledColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Slightly less rounded than a full pill, per UI request
    val shape = RoundedCornerShape(24.dp)
    val disabledTint = TextSecondary.copy(alpha = 0.92f)
    Box(
        modifier = modifier
            .height(62.dp)
            .clip(shape)
            .background(if (enabled) containerColor else disabledColor, shape)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = titleIcon,
                contentDescription = null,
                tint = if (enabled) White else disabledTint,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                GoldCoinMini(size = 10.dp, alpha = if (enabled) 1f else 0.85f)
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = "${ratePerMin}/min",
                    color = if (enabled) White else disabledTint,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 12.sp
                )
                if (minutes > 0) {
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "${minutes} mins",
                        color = if (enabled) White.copy(alpha = 0.90f) else disabledTint,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun CallRatePill(
    enabled: Boolean,
    containerColor: Color,
    disabledColor: Color,
    icon: ImageVector,
    ratePerMin: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(999.dp)

    Box(
        modifier = modifier
            .height(48.dp)
            .clip(shape)
            .background(color = if (enabled) containerColor else disabledColor, shape = shape)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (enabled) Color.White else TextTertiary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            GoldCoinMini(size = 12.dp, alpha = if (enabled) 1f else 0.5f)
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = "${ratePerMin}/min",
                color = if (enabled) Color.White else TextTertiary,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 14.sp,
                letterSpacing = 0.1.sp,
                maxLines = 1,
                overflow = TextOverflow.Clip
            )
        }
    }
}

@Composable
private fun GoldCoinMini(
    size: Dp,
    alpha: Float = 1f,
    modifier: Modifier = Modifier
) {
    // tiny 3-layer gold coin (no shadow)
    val darkGold = Color(0xFFB8860B).copy(alpha = alpha)
    val midGold = Color(0xFFDAA520).copy(alpha = alpha)
    val brightGold = Color(0xFFFFD700).copy(alpha = alpha)

    Box(modifier = modifier.size(size)) {
        Icon(
            imageVector = Icons.Default.Circle,
            contentDescription = null,
            tint = darkGold,
            modifier = Modifier
                .size(size)
                .offset(x = 1.dp, y = 1.dp)
        )
        Icon(
            imageVector = Icons.Default.Circle,
            contentDescription = null,
            tint = midGold,
            modifier = Modifier.size(size - 1.dp)
        )
        Icon(
            imageVector = Icons.Default.Circle,
            contentDescription = null,
            tint = brightGold,
            modifier = Modifier.size(size - 2.dp)
        )
    }
}

/* ------------------------------------
 * Premium Avatar with glow and verified badge
 * ------------------------------------ */
@Composable
private fun PremiumAvatar(isOnline: Boolean, isVerified: Boolean) {
    val glowAlpha = remember { Animatable(0.5f) }
    
    LaunchedEffect(isOnline) {
        if (isOnline) {
            while (true) {
                glowAlpha.animateTo(0.8f, tween(1500))
                glowAlpha.animateTo(0.5f, tween(1500))
            }
        }
    }
    
    Box(
        modifier = Modifier.size(90.dp),
        contentAlignment = Alignment.Center
    ) {
        // Outer glow for online
        if (isOnline) {
            Canvas(modifier = Modifier.size(96.dp)) {
                drawCircle(
                    color = Color.White.copy(alpha = 0.1f * glowAlpha.value),
                    radius = 48.dp.toPx()
                )
            }
        }
        
        // Avatar - flat
        Box(
            modifier = Modifier
                .size(82.dp)
                .clip(CircleShape)
                .background(ThemeSurface)
                .border(2.dp, PrimaryLight, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Person,
                contentDescription = null,
                tint = Primary,
                modifier = Modifier.size(42.dp)
            )
        }
        
        // Verified badge
        if (isVerified) {
            Icon(
                Icons.Filled.CheckCircle,
                "Verified",
                tint = Color.White,
                modifier = Modifier
                    .size(20.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = (-2).dp, y = 2.dp)
                    .background(Primary, CircleShape)
                    .padding(2.dp)
            )
        }
    }
}

/* ------------------------------------
 * Premium 3D Button with pricing
 * ------------------------------------ */
@Composable
private fun Premium3DButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    rate: Int,
    originalRate: Int?,
    isPrimary: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        // Flat button (no 3D shadow)
        Button(
            onClick = onClick,
            enabled = enabled,
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isPrimary) Primary else ThemeSurface,
                contentColor = if (isPrimary) Color.White else TextPrimary,
                disabledContainerColor = ThemeSurface,
                disabledContentColor = TextTertiary
            ),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(icon, label, modifier = Modifier.size(16.dp))
                    Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (originalRate != null && originalRate > rate) {
                        Text(
                            text = "₹$originalRate",
                            fontSize = 9.sp,
                            textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough,
                            color = if (isPrimary) Color.White.copy(alpha = 0.7f) else TextSecondary
                        )
                    }
                    Text(
                        text = "₹$rate/min",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isPrimary) Color.White else TextPrimary
                    )
                }
            }
        }
    }
}

/* ------------------------------------
 * Discount Badge (50% OFF)
 * ------------------------------------ */
@Composable
private fun DiscountBadge(modifier: Modifier = Modifier) {
    val pulse = remember { Animatable(1f) }
    
    LaunchedEffect(Unit) {
        while (true) {
            pulse.animateTo(1.1f, tween(800))
            pulse.animateTo(1f, tween(800))
        }
    }
    
    Text(
        text = "50% OFF",
        color = Color.White,
        fontSize = 10.sp,
        fontWeight = FontWeight.ExtraBold,
        modifier = modifier
            .scale(pulse.value)
            .background(
                Primary,
                RoundedCornerShape(6.dp)
            )
            .padding(horizontal = 8.dp, vertical = 3.dp)
    )
}

/* ------------------------------------
 * Corner Badge (NEW/POPULAR/TOP RATED)
 * ------------------------------------ */
@Composable
private fun CornerBadge(badge: String, modifier: Modifier = Modifier) {
    Text(
        text = badge,
        color = Color.White,
        fontSize = 9.sp,
        fontWeight = FontWeight.ExtraBold,
        letterSpacing = 0.5.sp,
        modifier = modifier
            .background(
                Primary,
                RoundedCornerShape(bottomStart = 10.dp, topEnd = 10.dp)
            )
            .padding(horizontal = 10.dp, vertical = 4.dp)
    )
}

/* ------------------------------------
 * World-Class Avatar (#1) - Larger 100dp with verified badge
 * ------------------------------------ */
@Composable
private fun WorldClassAvatar(
    isOnline: Boolean,
    isPremium: Boolean = true
) {
    // Subtle continuous glow animation
    val glowPulse = remember { Animatable(0.7f) }
    
    LaunchedEffect(Unit) {
        while (true) {
            glowPulse.animateTo(1f, tween(2000, easing = FastOutSlowInEasing))
            glowPulse.animateTo(0.7f, tween(2000, easing = FastOutSlowInEasing))
        }
    }
    
    Box(
        modifier = Modifier.size(100.dp), // Larger avatar (#1)
        contentAlignment = Alignment.Center
    ) {
        // Subtle outer glow
        Canvas(modifier = Modifier.size(106.dp)) {
            drawCircle(
                color = Color.White.copy(alpha = 0.08f * glowPulse.value),
                radius = 53.dp.toPx()
            )
        }
        
        // Avatar - flat
        Box(
            modifier = Modifier
                .size(90.dp)
                .clip(CircleShape)
                .background(ThemeSurface)
                .border(2.dp, PrimaryLight, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            // inner disc for depth
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(ThemeSurface)
            )
            Icon(
                imageVector = Icons.Filled.Person,
                contentDescription = null,
                tint = Primary,
                modifier = Modifier.size(40.dp)
            )
            
            // Verified badge overlay - positioned outside (#1)
            if (isPremium) {
                Box(
            modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 6.dp, y = (-6).dp) // Push outside the circle
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(Primary)
                        .border(2.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = "Verified",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

/* ------------------------------------
 * Premium Status Badge (#4) - Pill-shaped with glow
 * ------------------------------------ */
@Composable
private fun PremiumStatusBadge(isOnline: Boolean) {
    val glowAlpha = remember { Animatable(0.3f) }
    
    LaunchedEffect(isOnline) {
        if (isOnline) {
            while (true) {
                glowAlpha.animateTo(0.8f, tween(1500, easing = FastOutSlowInEasing))
                glowAlpha.animateTo(0.3f, tween(1500, easing = FastOutSlowInEasing))
            }
        }
    }
    
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(100))
            .background(if (isOnline) AccentLavender else ThemeSurface)
            .border(
                width = 1.dp,
                color = if (isOnline) PrimaryLight else ThemeBorder,
                shape = RoundedCornerShape(100)
            )
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Pulsing dot
            if (isOnline) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(CallGreen)
                        .alpha(glowAlpha.value)
                )
            }
            Text(
                text = if (isOnline) "ONLINE NOW" else "OFFLINE",
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (isOnline) Primary else TextSecondary,
                letterSpacing = 1.sp
            )
        }
    }
}

/* ------------------------------------
 * Premium Stars Row (#5) - Gradient stars
 * ------------------------------------ */
@Composable
private fun PremiumStarsRow(rating: Float) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        val fullStars = rating.toInt().coerceIn(0, 5)
        repeat(5) { i ->
            Box {
                // Glow layer for filled stars (#9)
                if (i < fullStars) {
                    Icon(
                        imageVector = Icons.Default.Circle,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.3f),
                        modifier = Modifier.size(20.dp)
                    )
                }
                // Main star (#9)
                Icon(
                    imageVector = Icons.Default.Circle,
                    contentDescription = null,
                    tint = if (i < fullStars) Primary else ThemeBorder,
                        modifier = Modifier.size(18.dp)
                    )
            }
        }
                    Spacer(Modifier.width(6.dp))
                    Text(
            text = "%.1f".format(rating),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            letterSpacing = 0.5.sp
        )
    }
}

/* ------------------------------------
 * Helper Functions
 * ------------------------------------ */
private fun formatNumber(num: Int): String {
    return when {
        num >= 1000 -> "%.1fk".format(num / 1000.0)
        else -> num.toString()
    }
}

/* ------------------------------------
 * Top Corner Badge (NEW/POPULAR/TOP RATED)
 * ------------------------------------ */
@Composable
private fun TopCornerBadge(badge: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(bottomStart = 12.dp))
            .background(Primary)
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
                Text(
            text = badge,
            fontSize = 9.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White,
            letterSpacing = 0.8.sp
        )
    }
}

/* ------------------------------------
 * First Call Discount Badge
 * ------------------------------------ */
@Composable
private fun FirstCallDiscountBadge(modifier: Modifier = Modifier) {
    val glowAlpha = remember { Animatable(0.5f) }
    
    LaunchedEffect(Unit) {
        while (true) {
            glowAlpha.animateTo(1f, tween(1000))
            glowAlpha.animateTo(0.5f, tween(1000))
        }
    }
    
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Primary)
            .border(1.dp, PrimaryLight.copy(alpha = glowAlpha.value), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = "50% OFF",
            fontSize = 9.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White,
            letterSpacing = 0.5.sp
        )
    }
}

/* ------------------------------------
 * Quick Action Button (Favorite/Share)
 * ------------------------------------ */
@Composable
private fun QuickActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    onClick: () -> Unit
) {
    Surface(
        shape = CircleShape,
        color = ThemeSurface,
        modifier = Modifier
            .size(32.dp),
        onClick = onClick
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

/* ------------------------------------
 * Availability Status Chip
 * ------------------------------------ */
@Composable
private fun AvailabilityStatusChip(
    status: AvailabilityStatus,
    lastActiveMinutes: Int?
) {
    data class StatusStyle(
        val backgroundColor: Color,
        val borderColor: Color,
        val textColor: Color,
        val statusText: String
    )
    
    val statusStyle = when (status) {
        AvailabilityStatus.AVAILABLE -> StatusStyle(
            backgroundColor = CallGreen.copy(alpha = 0.12f),
            borderColor = CallGreen,
            textColor = CallGreen,
            statusText = "Available Now"
        )
        AvailabilityStatus.OFFLINE -> StatusStyle(
            backgroundColor = ThemeSurface,
            borderColor = ThemeBorder,
            textColor = TextSecondary,
            statusText = if (lastActiveMinutes != null) {
                when {
                    lastActiveMinutes < 60 -> "Active ${lastActiveMinutes}m ago"
                    lastActiveMinutes < 1440 -> "Active ${lastActiveMinutes / 60}h ago"
                    else -> "Offline"
                }
            } else "Offline"
        )
    }
    
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(100))
            .background(statusStyle.backgroundColor)
            .border(1.dp, statusStyle.borderColor, RoundedCornerShape(100))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(statusStyle.borderColor)
            )
            Text(
                text = statusStyle.statusText,
                fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                color = statusStyle.textColor,
                letterSpacing = 0.3.sp
            )
        }
    }
}

/* ------------------------------------
 * Interest Tag Component
 * ------------------------------------ */
@Composable
private fun InterestTag(interest: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(100))
            .background(ThemeSurface)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Interest icon based on type
            Text(
                text = when (interest.lowercase()) {
                    "travel" -> "✈️"
                    "cooking" -> "👩‍🍳"
                    "music" -> "🎵"
                    "movies" -> "🎬"
                    "art" -> "🎨"
                    "fashion" -> "👗"
                    "yoga" -> "🧘"
                    "fitness" -> "💪"
                    "dance" -> "💃"
                    "reading" -> "📚"
                    "coffee" -> "☕"
                    "photography" -> "📸"
                    "shopping" -> "🛍️"
                    "style" -> "👔"
                    "meditation" -> "🧘‍♀️"
                    "singing" -> "🎤"
                    "writing" -> "✍️"
                    "food" -> "🍽️"
                    else -> "✨"
                },
                fontSize = 10.sp
            )
            Text(
                text = interest,
                fontSize = 11.sp,
                color = Color.White,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.3.sp
            )
        }
    }
}

/* ------------------------------------
 * More Interests Tag (+X more)
 * ------------------------------------ */
@Composable
private fun MoreInterestsTag(count: Int) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(100))
            .background(ThemeSurface)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = "+$count more",
            fontSize = 11.sp,
            color = Color(0xFF999999),
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.3.sp
        )
    }
}

/* ------------------------------------
 * Response Time Badge
 * ------------------------------------ */
@Composable
private fun ResponseTimeBadge(minutes: Int) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(ThemeSurface)
            .border(1.dp, Color(0xFF00FF00).copy(alpha = 0.3f), RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = "⚡ ${minutes}min",
            fontSize = 10.sp,
            color = Color(0xFF00FF00),
            fontWeight = FontWeight.Bold
        )
    }
}

/* ------------------------------------
 * Review Snippet
 * ------------------------------------ */
@Composable
private fun ReviewSnippet(review: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(ThemeSurface)
            .border(1.dp, ThemeSurface, RoundedCornerShape(8.dp))
            .padding(8.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = "💬",
            fontSize = 12.sp
        )
        Text(
            text = review,
            fontSize = 11.sp,
            color = Color(0xFFCCCCCC),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
            lineHeight = 15.sp,
            modifier = Modifier.weight(1f)
        )
    }
}

/* ------------------------------------
 * 3D Call Button WITH DISCOUNT - Premium
 * ------------------------------------ */
@Composable
private fun Premium3DCallButtonWithDiscount(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    rate: Int,
    originalRate: Int?,
    isPrimary: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val buttonScale = remember { Animatable(1f) }
    
    // Subtle breathing animation when enabled
    LaunchedEffect(enabled) {
        if (enabled) {
            while (true) {
                buttonScale.animateTo(1.02f, tween(2500, easing = FastOutSlowInEasing))
                buttonScale.animateTo(1f, tween(2500, easing = FastOutSlowInEasing))
            }
        } else {
            buttonScale.snapTo(1f)
        }
    }
    
    Box(modifier = modifier.scale(buttonScale.value)) {
        // Flat theme: no 3D shadow layer
        Button(
            onClick = onClick,
            enabled = enabled,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isPrimary) {
                    Primary
                } else {
                    ThemeSurface
                },
                contentColor = if (isPrimary) White else TextPrimary,
                disabledContainerColor = ThemeSurface,
                disabledContentColor = TextTertiary
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .border(
                    width = 1.5.dp,
                    color = if (enabled) {
                        if (isPrimary) Color.White else PrimaryLight
                    } else {
                        ThemeBorder
                    },
                    shape = RoundedCornerShape(12.dp)
                ),
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Icon with label row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                Text(
                        text = label,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        letterSpacing = 0.3.sp
                    )
                }
                
                // Rate display with discount
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Circle,
                        contentDescription = null,
                        modifier = Modifier.size(10.dp),
                        tint = if (enabled) {
                            if (isPrimary) Color.White else Primary
                        } else {
                            TextTertiary
                        }
                    )
                    Spacer(Modifier.width(2.dp))
                    
                    // Show discount if original rate exists
                    if (originalRate != null && originalRate > rate) {
                        // Strikethrough original price
                        Text(
                            text = "$originalRate",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Normal,
                            color = if (enabled) {
                                if (isPrimary) Color.White.copy(alpha = 0.6f) else TextSecondary
                            } else {
                                TextTertiary
                            },
                            style = androidx.compose.ui.text.TextStyle(
                                textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                            )
                        )
                        Spacer(Modifier.width(3.dp))
                    }
                    
                    // Current price
                    Text(
                        text = "$rate/min",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (enabled) {
                            if (originalRate != null && originalRate > rate) {
                                if (isPrimary) Color.White else CallGreen
                            } else {
                                if (isPrimary) Color.White else TextPrimary
                            }
                        } else {
                            TextTertiary
                        }
                    )
                }
            }
        }
    }
}

/* ------------------------------------
 * 3D Call Button WITH RATE - Premium
 * ------------------------------------ */
@Composable
private fun Premium3DCallButtonWithRate(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    rate: Int,
    isPrimary: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val buttonScale = remember { Animatable(1f) }
    
    // Subtle breathing animation when enabled
    LaunchedEffect(enabled) {
        if (enabled) {
            while (true) {
                buttonScale.animateTo(1.02f, tween(2500, easing = FastOutSlowInEasing))
                buttonScale.animateTo(1f, tween(2500, easing = FastOutSlowInEasing))
            }
        } else {
            buttonScale.snapTo(1f)
        }
    }
    
    Box(modifier = modifier.scale(buttonScale.value)) {
        // Flat theme: no 3D shadow layer
                    Button(
            onClick = onClick,
            enabled = enabled,
                        colors = ButtonDefaults.buttonColors(
                containerColor = if (isPrimary) {
                    Primary
                } else {
                    ThemeSurface
                },
                contentColor = if (isPrimary) White else TextPrimary,
                disabledContainerColor = ThemeSurface,
                disabledContentColor = TextTertiary
                        ),
                        modifier = Modifier
                .fillMaxWidth()
                .height(46.dp)
                .border(
                    width = 1.5.dp,
                    color = if (enabled) {
                        if (isPrimary) Color.White else PrimaryLight
                    } else {
                        ThemeBorder
                    },
                    shape = RoundedCornerShape(12.dp)
                ),
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Icon with label row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = label,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        letterSpacing = 0.3.sp
                    )
                }
                // Rate display
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Circle,
                        contentDescription = null,
                        modifier = Modifier.size(10.dp),
                        tint = if (enabled) {
                            if (isPrimary) Color.White else Primary
                        } else {
                            TextTertiary
                        }
                    )
                    Spacer(Modifier.width(2.dp))
                    Text(
                        text = "$rate/min",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (enabled) {
                            if (isPrimary) Color.White else TextPrimary
                        } else {
                            TextTertiary
                        }
                    )
                }
            }
        }
    }
}

/* ------------------------------------
 * 3D Call Button (#2) - Shadow depth like Login screen
 * ------------------------------------ */
@Composable
private fun Premium3DCallButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isPrimary: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val buttonScale = remember { Animatable(1f) }
    
    // Subtle breathing animation when enabled
    LaunchedEffect(enabled) {
        if (enabled) {
            while (true) {
                buttonScale.animateTo(1.02f, tween(2500, easing = FastOutSlowInEasing))
                buttonScale.animateTo(1f, tween(2500, easing = FastOutSlowInEasing))
            }
        } else {
            buttonScale.snapTo(1f)
        }
    }
    
    Box(modifier = modifier.scale(buttonScale.value)) {
        // Flat theme: no 3D shadow layer
                    Button(
            onClick = onClick,
            enabled = enabled,
                        colors = ButtonDefaults.buttonColors(
                containerColor = if (isPrimary) {
                    Primary
                } else {
                    ThemeSurface
                },
                contentColor = if (isPrimary) Color.White else TextPrimary,
                disabledContainerColor = ThemeSurface,
                disabledContentColor = TextTertiary
                        ),
                        modifier = Modifier
                .fillMaxWidth()
                            .height(48.dp)
                .then(
                    if (!isPrimary && enabled) {
                        Modifier.border(
                            width = 2.dp,
                            color = PrimaryLight,
                            shape = RoundedCornerShape(14.dp)
                        )
                    } else if (!isPrimary && !enabled) {
                        Modifier.border(
                            width = 1.dp,
                            color = ThemeBorder,
                            shape = RoundedCornerShape(14.dp)
                        )
                    } else {
                        Modifier
                    }
                ),
            shape = RoundedCornerShape(14.dp),
            contentPadding = PaddingValues(horizontal = 12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Icon with glow (#9)
                Box {
                    if (enabled) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            modifier = Modifier
                                .size(22.dp)
                                .alpha(0.3f)
                        )
                    }
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        modifier = Modifier.size(20.dp) // Larger icons (#9)
                    )
                }
                        Spacer(Modifier.width(8.dp))
                Text(
                    text = label,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

@Composable
private fun PremiumAvatar3D(statusDot: Color, isOnline: Boolean) {
    // Animated gradient ring rotation
    val rotation = remember { Animatable(0f) }
    
    LaunchedEffect(isOnline) {
        if (isOnline) {
            while (true) {
                rotation.animateTo(
                    targetValue = 360f,
                    animationSpec = tween(3000, easing = LinearEasing)
                )
                rotation.snapTo(0f)
            }
        }
    }
    
    Box(
        modifier = Modifier.size(80.dp),
        contentAlignment = Alignment.Center
    ) {
        // Flat theme: no animated gradient ring
        
        // Avatar - flat
    Box(
        modifier = Modifier
            .size(70.dp)
            .clip(CircleShape)
            .background(ThemeSurface)
            .border(1.5.dp, PrimaryLight, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        // inner disc for depth
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                    .background(ThemeSurface)
        )
        Icon(
            imageVector = Icons.Filled.Person,
            contentDescription = null,
            tint = Primary,
                modifier = Modifier.size(32.dp)
            )
            // status with pulse animation
            val pulseScale = remember { Animatable(1f) }
            LaunchedEffect(isOnline) {
                if (isOnline) {
                    while (true) {
                        pulseScale.animateTo(1.2f, tween(800))
                        pulseScale.animateTo(1f, tween(800))
                    }
                }
            }
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = (-3).dp, y = (-3).dp)
                    .size(14.dp)
                    .scale(pulseScale.value)
                    .clip(CircleShape)
                    .background(statusDot)
                    .border(2.dp, Color.White, CircleShape)
            )
        }
    }
}

@Composable
private fun Avatar3D(statusDot: Color) {
    Box(
        modifier = Modifier
            .size(70.dp)
            .clip(CircleShape)
            .background(ThemeSurface)
            .border(1.dp, PrimaryLight, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        // inner disc for depth
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(ThemeSurface)
        )
        Icon(
            imageVector = Icons.Filled.Person,
            contentDescription = null,
            tint = Primary,
            modifier = Modifier.size(32.dp)
        )
        // status
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = (-3).dp, y = (-3).dp)
                .size(14.dp)
                .clip(CircleShape)
                .background(statusDot)
                .border(2.dp, Color.White, CircleShape)
        )
    }
}

@Composable
private fun StarsRow(
    rating: Float,
    centered: Boolean = false
) {
    val rowModifier = if (centered) Modifier.fillMaxWidth() else Modifier
    Row(
        modifier = rowModifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = if (centered) Arrangement.Center else Arrangement.Start
    ) {
        val fullStars = rating.toInt().coerceIn(0, 5)
        repeat(5) { i ->
            val tint = if (i < fullStars) Primary else ThemeBorder
            Icon(
                imageVector = Icons.Default.Circle,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(16.dp)
            )
            if (i != 4) Spacer(Modifier.width(4.dp))
        }
        Spacer(Modifier.width(8.dp))
        Text(
            text = "%.1f".format(rating),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = Color.White
        )
    }
}

/* ------------------------------------
 * Hearts burst animation
 * ------------------------------------ */
private data class HeartParticle(
    val angle: Float,
    val speed: Float,
    val size: Float
)

@Composable
private fun HeartsBurst(
    color: Color,
    onFinished: () -> Unit,
    modifier: Modifier = Modifier
        .fillMaxWidth()
        .height(80.dp)
) {
    val particles = remember {
        List(18) {
            HeartParticle(
                angle = Random.nextFloat() * 360f,
                speed = 90f + Random.nextFloat() * 120f,
                size = 5f + Random.nextFloat() * 8f
            )
        }
    }
    val anim = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        anim.animateTo(
            targetValue = 1f,
            animationSpec = tween(700, easing = LinearOutSlowInEasing)
        )
        onFinished()
    }
    Canvas(modifier) {
        val center = androidx.compose.ui.geometry.Offset(size.width / 2f, size.height)
        particles.forEach { p ->
            val t = anim.value
            val rad = Math.toRadians(p.angle.toDouble()).toFloat()
            val d = p.speed * t
            val x = center.x + d * cos(rad)
            val y = center.y - d * sin(rad)
            drawCircle(
                color = color.copy(alpha = (1f - t).coerceAtLeast(0f)),
                radius = p.size,
                center = androidx.compose.ui.geometry.Offset(x, y)
            )
        }
    }
}

/* ------------------------------------
 * Premium particle effects
 * ------------------------------------ */
@Composable
private fun PremiumParticles() {
    data class Particle(
        val x: Float,
        val y: Float,
        val size: Float,
        val speedY: Float,
        val alpha: Float
    )
    
    val particles = remember {
        List(15) {
            Particle(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                size = Random.nextFloat() * 2f + 1f,
                speedY = Random.nextFloat() * 0.5f + 0.2f,
                alpha = Random.nextFloat() * 0.3f + 0.1f
            )
        }
    }
    
    val animProgress = remember { Animatable(0f) }
    
    LaunchedEffect(Unit) {
        while (true) {
            animProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(8000, easing = LinearEasing)
            )
            animProgress.snapTo(0f)
        }
    }
    
    Canvas(modifier = Modifier.fillMaxSize()) {
        particles.forEach { particle ->
            val progress = animProgress.value
            val xPos = particle.x * size.width
            val yPos = ((particle.y + particle.speedY * progress) % 1f) * size.height
            
            drawCircle(
                color = Color.White.copy(alpha = particle.alpha),
                radius = particle.size,
                center = androidx.compose.ui.geometry.Offset(xPos, yPos)
            )
        }
    }
}

/* ------------------------------------
 * Premium Random FAB
 * ------------------------------------ */
@Composable
private fun PremiumRandomFAB(
    expanded: Boolean,
    onToggle: () -> Unit,
    onClose: () -> Unit,
    onAudio: () -> Unit,
    onVideo: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Pulsing animation for the FAB
    val pulseScale = remember { Animatable(1f) }
    val iconRotation = remember { Animatable(0f) }
    val glowAlpha = remember { Animatable(0.6f) }
    
    LaunchedEffect(Unit) {
        launch {
            while (true) {
                pulseScale.animateTo(
                    targetValue = 1.05f,
                    animationSpec = tween(1500, easing = FastOutSlowInEasing)
                )
                pulseScale.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(1500, easing = FastOutSlowInEasing)
                )
            }
        }
        launch {
            while (true) {
                iconRotation.animateTo(
                    targetValue = 12f,
                    animationSpec = tween(1000, easing = FastOutSlowInEasing)
                )
                iconRotation.animateTo(
                    targetValue = -12f,
                    animationSpec = tween(1000, easing = FastOutSlowInEasing)
                )
            }
        }
        launch {
            while (true) {
                glowAlpha.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(1200, easing = FastOutSlowInEasing)
                )
                glowAlpha.animateTo(
                    targetValue = 0.6f,
                    animationSpec = tween(1200, easing = FastOutSlowInEasing)
                )
            }
        }
    }
    
    Box(modifier = modifier.scale(pulseScale.value)) {
        if (!expanded) {
            // Collapsed: old Random button style
            // Outer glow layer
            ExtendedFloatingActionButton(
                onClick = {},
                modifier = Modifier
                    .offset(y = 0.dp)
                    // no glow blur (flat theme)
                    .blur(0.dp, edgeTreatment = BlurredEdgeTreatment.Unbounded)
                    .alpha(glowAlpha.value * 0.8f),
                // inside color: blue -> white
                containerColor = White,
                contentColor = Color.Transparent
            ) {
                Icon(
                    imageVector = Icons.Filled.Shuffle,
                    contentDescription = null,
                    tint = PrimaryDark.copy(alpha = glowAlpha.value.coerceIn(0.0f, 1.0f)),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Random",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryDark.copy(alpha = glowAlpha.value.coerceIn(0.0f, 1.0f))
                )
            }

            // Main FAB - flat
            ExtendedFloatingActionButton(
                onClick = onToggle,
                containerColor = White,
                contentColor = Color.Transparent,
                modifier = Modifier.border(
                    width = 1.dp,
                    color = PrimaryLight.copy(alpha = glowAlpha.value * 0.9f),
                    shape = RoundedCornerShape(16.dp)
                )
            ) {
                Icon(
                    imageVector = Icons.Filled.Shuffle,
                    contentDescription = null,
                    tint = PrimaryDark,
                    modifier = Modifier
                        .size(20.dp)
                        .rotate(iconRotation.value)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Random",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.8.sp,
                    color = PrimaryDark
                )
            }
        } else {
            // Expanded: only 3 round icons (Audio, Video, X)
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                FloatingActionButton(
                    onClick = onAudio,
                    containerColor = Primary,
                    contentColor = White,
                    shape = CircleShape
                ) {
                    Icon(
                        imageVector = Icons.Filled.Call,
                        contentDescription = "Random Audio Call",
                        modifier = Modifier.size(22.dp)
                    )
                }

                FloatingActionButton(
                    onClick = onVideo,
                    containerColor = OnlineGreen,
                    contentColor = White,
                    shape = CircleShape
                ) {
                    Icon(
                        imageVector = Icons.Filled.Videocam,
                        contentDescription = "Random Video Call",
                        modifier = Modifier.size(22.dp)
                    )
                }

                FloatingActionButton(
                    onClick = onClose,
                    containerColor = White,
                    contentColor = TextPrimary,
                    shape = CircleShape
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Close",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

/* ------------------------------------
 * Random Call Dialog
 * ------------------------------------ */
@Composable
private fun RandomCallDialog(
    onDismiss: () -> Unit,
    onAudioCall: () -> Unit,
    onVideoCall: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        // Light + purple theme (no dark sheet)
        containerColor = ThemeSurface,
        titleContentColor = TextPrimary,
        textContentColor = TextSecondary,
        shape = RoundedCornerShape(22.dp),
        modifier = Modifier.border(1.dp, PrimaryLight, RoundedCornerShape(22.dp)),
        title = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Shuffle icon with glow
                    Box {
                        Icon(
                            imageVector = Icons.Filled.Shuffle,
                            contentDescription = null,
                            tint = Primary.copy(alpha = 0.3f),
                            modifier = Modifier.size(32.dp)
                        )
                        Icon(
                            imageVector = Icons.Filled.Shuffle,
                            contentDescription = null,
                            tint = Primary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Text(
                        text = "Random Call",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 22.sp,
                        letterSpacing = 0.5.sp,
                        color = TextPrimary
                    )
                }
                Text(
                    text = "Choose how would you like to connect",
                    fontSize = 14.sp,
                    color = TextSecondary,
                    letterSpacing = 0.2.sp,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(vertical = 16.dp)
            ) {
                // Audio Call Button - Purple (match Home theme)
                Button(
                    onClick = onAudioCall,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Primary,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(14.dp),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 0.dp,
                        pressedElevation = 0.dp,
                        disabledElevation = 0.dp
                    )
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Call,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Audio Call",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.6.sp,
                            color = Color.White
                        )
                    }
                }
                
                // Video Call Button - Green
                Button(
                    onClick = onVideoCall,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF10B981),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(14.dp),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 0.dp,
                        pressedElevation = 0.dp,
                        disabledElevation = 0.dp
                    )
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Videocam,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Video Call",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.6.sp,
                            color = Color.White
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, PrimaryLight),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary)
            ) {
                Text(
                    text = "Cancel",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.3.sp
                )
            }
        }
    )
}
