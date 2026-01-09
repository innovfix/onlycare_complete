package com.onlycare.app.presentation.screens.main

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.onlycare.app.domain.model.CallType
import com.onlycare.app.presentation.components.*
import com.onlycare.app.presentation.navigation.Screen
import com.onlycare.app.presentation.theme.*
import android.util.Log
import java.text.SimpleDateFormat
import java.util.*

private const val TAG_RECENT_CALLS = "RecentCallsScreen"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecentCallsScreen(
    navController: NavController,
    viewModel: RecentCallsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var showFilterDialog by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val isMale = remember { viewModel.getGender() == com.onlycare.app.domain.model.Gender.MALE }
    val isFemale = remember { !isMale }
    
    // Detect when user scrolls to bottom for pagination
    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()
            lastVisibleItem != null && 
            lastVisibleItem.index >= state.calls.size - 3 &&
            !state.isLoadingMore &&
            state.hasMorePages
        }
    }
    
    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) {
            viewModel.loadMoreCalls()
        }
    }
    
    Scaffold(
        containerColor = Background,
        topBar = {
            // Custom Premium Header with Subtitle and Filter
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Background)
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Recent Calls",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = when (state.selectedFilter) {
                                "talktime" -> "Sorted by call duration"
                                "az" -> "Sorted alphabetically"
                                else -> "Most recent calls"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Messages button (right, near filter)
                        Surface(
                            onClick = { navController.navigate(Screen.ChatList.route) },
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White,
                            border = androidx.compose.foundation.BorderStroke(1.dp, Border),
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Chat,
                                    contentDescription = "Messages",
                                    tint = SuccessGreen,
                                    modifier = Modifier.size(20.dp)
                        )
                            }
                    }
                    
                        // Filter/Sort Button (right)
                    Surface(
                        onClick = { showFilterDialog = true },
                        shape = RoundedCornerShape(12.dp),
                            color = Color.White,
                            border = androidx.compose.foundation.BorderStroke(1.dp, Border),
                            modifier = Modifier.size(44.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.FilterList,
                                contentDescription = "Filter",
                                    tint = Primary,
                                modifier = Modifier.size(20.dp)
                            )
                            }
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        when {
            state.error != null && state.calls.isEmpty() -> {
                val errorMessage = state.error
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    EmptyState(
                        icon = Icons.Default.Error,
                        title = "Error Loading Calls",
                        message = errorMessage ?: "Unknown error"
                    )
                }
            }
            state.calls.isEmpty() && !state.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    EmptyState(
                        icon = Icons.Default.History,
                        title = "No Recent Calls",
                        message = "Your call history will appear here"
                    )
                }
            }
            else -> {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(state.calls) { call ->
                        val availability = state.callAvailabilityByUserId[call.otherUserId]
                        val displayName = state.displayNameByUserId[call.otherUserId]
                            ?.takeIf { it.isNotBlank() }
                            ?: call.otherUserName
                        PremiumRecentCallCard(
                            call = call,
                            displayName = displayName,
                            showVideoCall = isMale,
                            isAudioEnabled = availability?.audioEnabled ?: true,
                            isVideoEnabled = availability?.videoEnabled ?: true,
                            onAudioCall = {
                                Log.i(TAG_RECENT_CALLS, "========================================")
                                Log.i(TAG_RECENT_CALLS, "📞 RECENT PAGE - CALL INITIATED")
                                Log.i(TAG_RECENT_CALLS, "========================================")
                                Log.i(TAG_RECENT_CALLS, "User Type: ${if (isFemale) "FEMALE" else "MALE"}")
                                Log.i(TAG_RECENT_CALLS, "Call Type: AUDIO")
                                Log.i(TAG_RECENT_CALLS, "Receiver ID: ${call.otherUserId}")
                                Log.i(TAG_RECENT_CALLS, "Receiver Name: $displayName")
                                Log.i(TAG_RECENT_CALLS, "From: Recent Calls Page")
                                if (isFemale) {
                                    Log.i(TAG_RECENT_CALLS, "✅ FEMALE USER - Coin balance check will be SKIPPED")
                                } else {
                                    Log.i(TAG_RECENT_CALLS, "💰 MALE USER - Coin balance check will be performed")
                                }
                                Log.i(TAG_RECENT_CALLS, "========================================")
                                navController.navigate(
                                    Screen.CallConnecting.createRoute(call.otherUserId, "audio")
                                )
                            },
                            onVideoCall = {
                                Log.i(TAG_RECENT_CALLS, "========================================")
                                Log.i(TAG_RECENT_CALLS, "📞 RECENT PAGE - CALL INITIATED")
                                Log.i(TAG_RECENT_CALLS, "========================================")
                                Log.i(TAG_RECENT_CALLS, "User Type: ${if (isFemale) "FEMALE" else "MALE"}")
                                Log.i(TAG_RECENT_CALLS, "Call Type: VIDEO")
                                Log.i(TAG_RECENT_CALLS, "Receiver ID: ${call.otherUserId}")
                                Log.i(TAG_RECENT_CALLS, "Receiver Name: $displayName")
                                Log.i(TAG_RECENT_CALLS, "From: Recent Calls Page")
                                if (isFemale) {
                                    Log.i(TAG_RECENT_CALLS, "✅ FEMALE USER - Coin balance check will be SKIPPED")
                                } else {
                                    Log.i(TAG_RECENT_CALLS, "💰 MALE USER - Coin balance check will be performed")
                                }
                                Log.i(TAG_RECENT_CALLS, "========================================")
                                navController.navigate(
                                    Screen.CallConnecting.createRoute(call.otherUserId, "video")
                                )
                            }
                        )
                    }
                    
                    // Loading more indicator
                    if (state.isLoadingMore) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
        
        if (state.isLoading && state.calls.isEmpty()) {
            LoadingIndicator()
        }
        
        // Filter Dialog
        if (showFilterDialog) {
            var tempSelectedFilter by remember { mutableStateOf(state.selectedFilter) }
            
            AlertDialog(
                onDismissRequest = { showFilterDialog = false },
                // User request: pure white dialog background
                containerColor = Background,
                title = {
                    Text(
                        text = "Sort Calls",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Choose how to sort your calls:",
                            color = TextSecondary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            FilterChip(
                                text = "Recent",
                                subtitle = "Most recent first",
                                selected = tempSelectedFilter == "recent",
                                onClick = { tempSelectedFilter = "recent" },
                                modifier = Modifier.fillMaxWidth()
                            )
                            FilterChip(
                                text = "Talktime",
                                subtitle = "Longest calls first",
                                selected = tempSelectedFilter == "talktime",
                                onClick = { tempSelectedFilter = "talktime" },
                                modifier = Modifier.fillMaxWidth()
                            )
                            FilterChip(
                                text = "A-Z",
                                subtitle = "Alphabetical order",
                                selected = tempSelectedFilter == "az",
                                onClick = { tempSelectedFilter = "az" },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (tempSelectedFilter != state.selectedFilter) {
                                viewModel.changeFilter(tempSelectedFilter)
                            }
                            showFilterDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Primary,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 8.dp)
                    ) {
                        Text("Apply", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showFilterDialog = false }
                    ) {
                        Text("Cancel", color = TextSecondary)
                    }
                }
            )
        }
    }
}

@Composable
private fun FilterChip(
    text: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        // Use white background (no bluish Surface tint)
        color = if (selected) PrimaryLight.copy(alpha = 0.35f) else Background,
        border = if (selected) androidx.compose.foundation.BorderStroke(
            1.5.dp,
            Primary
        ) else androidx.compose.foundation.BorderStroke(
            1.dp,
            PrimaryLight
        ),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 14.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = text,
                color = if (selected) Primary else TextPrimary,
                fontSize = 15.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                color = TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PremiumRecentCallCard(
    call: com.onlycare.app.domain.model.Call,
    displayName: String,
    showVideoCall: Boolean = true,
    isAudioEnabled: Boolean,
    isVideoEnabled: Boolean,
    onAudioCall: () -> Unit,
    onVideoCall: () -> Unit
) {
    // Availability per creator call settings
    val audioEnabled = isAudioEnabled
    val videoEnabled = isVideoEnabled
    OnlyCareSoftShadowContainer(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        shadowOffsetY = 4.dp,
        shadowColor = Border.copy(alpha = 0.28f)
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = Background
            ),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth(),
        onClick = { /* View call details */ }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Premium Profile Image with Animated Border
            PremiumProfileImageWithBorder(
                imageUrl = call.otherUserImage,
                isOnline = true
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Call Info
            Column(modifier = Modifier.weight(1f)) {
                // User Name
                Text(
                    text = displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Prominent Call Duration with Clock Icon
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Primary
                    )
                    Text(
                        text = formatDurationProminent(call.duration),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = TextPrimary
                    )
                }
                
                Spacer(modifier = Modifier.height(2.dp))
                
                // Date and Time
                Text(
                    text = formatTimestamp(call.timestamp),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }
            
            // Call Action Buttons with Availability States
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Audio Call Button
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (audioEnabled) Primary else Border,
                    modifier = Modifier
                        .size(48.dp),
                    onClick = if (audioEnabled) onAudioCall else ({})
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = "Audio Call",
                            tint = if (audioEnabled) Color.White else TextTertiary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
                
                // Video Call Button (only show if showVideoCall is true)
                if (showVideoCall) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (videoEnabled) Color(0xFF10B981) else Border,
                        modifier = Modifier.size(48.dp),
                        onClick = if (videoEnabled) onVideoCall else ({})
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Videocam,
                                contentDescription = "Video Call",
                                tint = if (videoEnabled) Color.White else TextTertiary,
                                modifier = Modifier.size(22.dp)
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
private fun PremiumProfileImageWithBorder(
    imageUrl: String?,
    isOnline: Boolean
) {
    // User request: show a clean round avatar ring (no gradients)
    Box(
        modifier = Modifier.size(64.dp),
        contentAlignment = Alignment.Center
    ) {
        // Outer ring
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(Background)
                .border(
                    width = 2.dp,
                    color = PrimaryLight,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            // Inner avatar
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Background)
        ) {
            ProfileImage(
                imageUrl = imageUrl,
                    size = 56.dp
            )
            }
        }
    }
}

private fun formatDurationProminent(seconds: Int): String {
    return when {
        seconds < 60 -> "$seconds sec"
        seconds < 3600 -> "${seconds / 60} min"
        else -> "${seconds / 3600} hr ${(seconds % 3600) / 60} min"
    }
}

private fun formatDuration(seconds: Int): String {
    val minutes = seconds / 60
    val secs = seconds % 60
    return String.format("%d:%02d", minutes, secs)
}

private fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
