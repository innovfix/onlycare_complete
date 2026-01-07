package com.onlycare.app.presentation.screens.main

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import androidx.navigation.NavController
import com.onlycare.app.presentation.theme.Background
import com.onlycare.app.presentation.theme.Surface as ThemeSurface
import com.onlycare.app.presentation.theme.TextPrimary
import com.onlycare.app.presentation.theme.TextSecondary
import com.onlycare.app.presentation.theme.Primary
import com.onlycare.app.presentation.theme.PrimaryLight
import com.onlycare.app.presentation.theme.CallGreen
import androidx.hilt.navigation.compose.hiltViewModel
import com.onlycare.app.domain.model.User
import com.onlycare.app.domain.model.FriendRequest
import com.onlycare.app.presentation.components.*
import com.onlycare.app.presentation.navigation.Screen
import com.onlycare.app.presentation.theme.White
import com.onlycare.app.utils.getDisplayName

enum class FriendTab {
    CHAT, FRIENDS, MY_REQUESTS, SENT
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendsScreen(
    navController: NavController,
    viewModel: FriendsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(FriendTab.FRIENDS) }
    var showAddFriendDialog by remember { mutableStateOf(false) }
    
    // Show success message
    LaunchedEffect(state.successMessage) {
        state.successMessage?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            viewModel.clearSuccessMessage()
        }
    }
    
    // Show error message
    LaunchedEffect(state.error) {
        state.error?.let { error ->
            Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
            viewModel.clearError()
        }
    }
    
    Scaffold(
        containerColor = Background,
        floatingActionButton = {
            if (selectedTab == FriendTab.FRIENDS || selectedTab == FriendTab.SENT) {
                AnimatedFloatingActionButton(onClick = { showAddFriendDialog = true })
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Background)
                .padding(paddingValues)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Premium Tab Bar
                PremiumTabBar(
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it },
                    navController = navController
                )
                
                // Loading State
                if (state.isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Primary)
                    }
                } else {
                    // Animated Content
                    AnimatedContent(
                        targetState = selectedTab,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(300)) +
                                slideInHorizontally(
                                    initialOffsetX = { if (targetState.ordinal > initialState.ordinal) 1000 else -1000 },
                                    animationSpec = tween(300, easing = FastOutSlowInEasing)
                                ) togetherWith
                                fadeOut(animationSpec = tween(300)) +
                                slideOutHorizontally(
                                    targetOffsetX = { if (targetState.ordinal > initialState.ordinal) -1000 else 1000 },
                                    animationSpec = tween(300, easing = FastOutSlowInEasing)
                                )
                        },
                        label = "tab_content"
                    ) { tab ->
                        when (tab) {
                            FriendTab.CHAT -> ChatTabContent(
                                navController = navController,
                                friends = state.friends
                            )
                            FriendTab.FRIENDS -> FriendsTabContent(
                                navController = navController,
                                friends = state.friends,
                                onRemoveFriend = { viewModel.removeFriend(it) }
                            )
                            // "My Requests" shows requests RECEIVED by me (Incoming)
                            FriendTab.MY_REQUESTS -> ReceivedRequestsTabContent(
                                requests = state.receivedRequests,
                                onAccept = { viewModel.acceptFriendRequest(it) },
                                onReject = { viewModel.rejectFriendRequest(it) }
                            )
                            // "Sent" requests shows requests SENT by me (Outgoing)
                            FriendTab.SENT -> MyRequestsTabContent(
                                requests = state.sentRequests
                            )
                        }
                    }
                }
            }
        }
        
        // Add Friend Dialog
        if (showAddFriendDialog) {
            AddFriendDialog(
                onDismiss = { showAddFriendDialog = false },
                onSendRequest = { userId ->
                    viewModel.sendFriendRequest(userId)
                    showAddFriendDialog = false
                }
            )
        }
    }
}

@Composable
private fun PremiumTabBar(
    selectedTab: FriendTab,
    onTabSelected: (FriendTab) -> Unit,
    navController: NavController? = null
) {
    // Premium Tab Bar with Gradient Background
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Primary)
    ) {
        // Back button row
        if (navController != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { navController.navigateUp() },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                Text(
                    text = "Friends",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
        
        ScrollableTabRow(
            selectedTabIndex = selectedTab.ordinal,
            containerColor = Color.Transparent,
            contentColor = Color.White,
            indicator = {},
            divider = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(PrimaryLight)
                )
            },
            edgePadding = 16.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            FriendTab.values().forEach { tab ->
                val selected = selectedTab == tab
                
                Tab(
                    selected = selected,
                    onClick = { onTabSelected(tab) },
                    modifier = Modifier
                        .height(56.dp)
                        .padding(horizontal = 4.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        AnimatedContent(
                            targetState = selected,
                            transitionSpec = {
                                fadeIn(tween(200)) + scaleIn(tween(200), initialScale = 0.95f) togetherWith
                                    fadeOut(tween(200)) + scaleOut(tween(200), targetScale = 0.95f)
                            },
                            label = "tab_text"
                        ) { isSelected ->
                            Text(
                                text = when (tab) {
                                    FriendTab.CHAT -> "Chat"
                                    FriendTab.FRIENDS -> "Friends"
                                    FriendTab.MY_REQUESTS -> "My Requests"
                                    FriendTab.SENT -> "Sent"
                                },
                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                fontSize = if (isSelected) 15.sp else 14.sp,
                                color = if (isSelected) Color.White else Color.White.copy(alpha = 0.6f),
                                letterSpacing = if (isSelected) 0.5.sp else 0.sp
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Premium Indicator
                        Box(
                            modifier = Modifier
                                .width(if (selected) 40.dp else 0.dp)
                                .height(3.dp)
                                .clip(RoundedCornerShape(1.5.dp))
                                .background(
                                    if (selected) {
                                        PrimaryLight
                                    } else {
                                        Color.Transparent
                                    }
                                )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatTabContent(
    navController: NavController,
    friends: List<User>
) {
    if (friends.isEmpty()) {
        PremiumEmptyState(
            icon = Icons.Default.Chat,
            title = "No chats yet",
            subtitle = "Start chatting with your friends"
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(friends, key = { it.id }) { friend ->
                AnimatedFriendItem(
                    friend = friend,
                    onChatClick = { navController.navigate(Screen.Chat.createRoute(friend.id)) },
                    onCallClick = { navController.navigate(Screen.CallConnecting.createRoute(friend.id, "audio")) },
                    onProfileClick = { navController.navigate(Screen.UserProfile.createRoute(friend.id)) }
                )
            }
        }
    }
}

@Composable
private fun FriendsTabContent(
    navController: NavController,
    friends: List<User>,
    onRemoveFriend: (String) -> Unit = {}
) {
    if (friends.isEmpty()) {
        PremiumEmptyState(
            icon = Icons.Default.PersonAdd,
            title = "No friends yet",
            subtitle = "Start connecting with people\nby sending friend requests"
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(friends, key = { it.id }) { friend ->
                AnimatedFriendItem(
                    friend = friend,
                    onChatClick = { navController.navigate(Screen.Chat.createRoute(friend.id)) },
                    onCallClick = { navController.navigate(Screen.CallConnecting.createRoute(friend.id, "audio")) },
                    onProfileClick = { navController.navigate(Screen.UserProfile.createRoute(friend.id)) }
                )
            }
        }
    }
}

@Composable
private fun MyRequestsTabContent(requests: List<FriendRequest>) {
    if (requests.isEmpty()) {
        PremiumEmptyState(
            icon = Icons.Default.Send,
            title = "No pending requests",
            subtitle = "Friend requests you send\nwill appear here"
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(requests, key = { it.userId }) { request ->
                PendingRequestItem(
                    request = request,
                    onCancel = { 
                        // Cancel pending friend request
                        // The backend automatically handles cancellation via reject endpoint
                        // Since this is a "sent" request, we can reuse reject functionality
                        // TODO: Add dedicated cancel endpoint in backend if needed
                    }
                )
            }
        }
    }
}

@Composable
private fun ReceivedRequestsTabContent(
    requests: List<FriendRequest>,
    onAccept: (String) -> Unit = {},
    onReject: (String) -> Unit = {}
) {
    if (requests.isEmpty()) {
        PremiumEmptyState(
            icon = Icons.Default.Inbox,
            title = "No new requests",
            subtitle = "Friend requests from others\nwill appear here"
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(requests, key = { it.userId }) { request ->
                ReceivedRequestItem(
                    request = request,
                    onAccept = { onAccept(request.userId) },
                    onDecline = { onReject(request.userId) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AnimatedFriendItem(
    friend: User,
    onChatClick: () -> Unit,
    onCallClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        visible = true
    }
    
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(400)) + slideInVertically(tween(400), initialOffsetY = { 50 }),
        label = "friend_item"
    ) {
        Card(
            onClick = onProfileClick,
            colors = CardDefaults.cardColors(
                containerColor = ThemeSurface
            ),
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .border(1.5.dp, PrimaryLight, RoundedCornerShape(18.dp)),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Premium Profile Picture with Online Status
                Box(modifier = Modifier.size(56.dp)) {
                    // Subtle glow effect
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(PrimaryLight.copy(alpha = 0.25f), shape = CircleShape)
                            .padding(2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(ThemeSurface)
                                .border(1.5.dp, PrimaryLight, CircleShape)
                        ) {
                            ProfileImage(
                                imageUrl = friend.profileImage,
                                size = 56.dp
                            )
                        }
                    }
                    
                    // Premium Online Indicator
                    if (friend.isOnline) {
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .align(Alignment.BottomEnd)
                                .background(ThemeSurface, CircleShape)
                                .padding(2.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(CallGreen, CircleShape)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Text(
                    text = friend.getDisplayName(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary,
                    fontSize = 16.sp,
                    modifier = Modifier.weight(1f)
                )
                
                // Premium Action Buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Message Button
                    Surface(
                        onClick = onChatClick,
                        shape = CircleShape,
                        color = ThemeSurface,
                        modifier = Modifier
                            .size(46.dp)
                            .border(1.5.dp, PrimaryLight, CircleShape)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Chat,
                                contentDescription = "Chat",
                                tint = TextPrimary,
                                modifier = Modifier.size(21.dp)
                            )
                        }
                    }
                    
                    // Call Button
                    Surface(
                        onClick = onCallClick,
                        shape = CircleShape,
                        color = Color.White,
                        modifier = Modifier.size(46.dp),
                        shadowElevation = 3.dp
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Phone,
                                contentDescription = "Call",
                                tint = TextPrimary,
                                modifier = Modifier.size(21.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PendingRequestItem(
    request: FriendRequest,
    onCancel: () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        visible = true
    }
    
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(400)) + slideInVertically(tween(400), initialOffsetY = { 50 }),
        label = "request_item"
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = ThemeSurface
            ),
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .border(1.5.dp, PrimaryLight, RoundedCornerShape(18.dp)),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Premium Profile Picture with subtle glow
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .background(PrimaryLight.copy(alpha = 0.25f), shape = CircleShape)
                        .padding(2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(ThemeSurface)
                            .border(1.dp, PrimaryLight, CircleShape)
                    ) {
                        ProfileImage(
                            imageUrl = request.profileImage,
                            size = 52.dp
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = request.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(Color(0xFFFFAA00), CircleShape)
                        )
                        Text(
                            text = "Pending",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                    }
                }
                
                // Premium Cancel Button
                Button(
                    onClick = onCancel,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ThemeSurface,
                        contentColor = Color(0xFFFF4444)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 0.dp,
                        pressedElevation = 0.dp
                    ),
                    modifier = Modifier
                        .height(38.dp)
                        .border(1.dp, PrimaryLight, RoundedCornerShape(10.dp))
                ) {
                    Text(
                        text = "Cancel",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFF4444)
                    )
                }
            }
        }
    }
}

@Composable
private fun ReceivedRequestItem(
    request: FriendRequest,
    onAccept: () -> Unit,
    onDecline: () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        visible = true
    }
    
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(400)) + slideInVertically(tween(400), initialOffsetY = { 50 }),
        label = "received_item"
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = ThemeSurface
            ),
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .border(1.5.dp, PrimaryLight, RoundedCornerShape(18.dp)),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Premium Profile Picture with subtle glow
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .background(PrimaryLight.copy(alpha = 0.25f), shape = CircleShape)
                            .padding(2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(ThemeSurface)
                                .border(1.dp, PrimaryLight, CircleShape)
                        ) {
                            ProfileImage(
                                imageUrl = request.profileImage,
                                size = 52.dp
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = request.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Wants to be friends",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Accept Button - Premium White
                    Button(
                        onClick = onAccept,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Primary,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 0.dp,
                            pressedElevation = 0.dp
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Accept",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    
                    // Decline Button - Premium Dark
                    Button(
                        onClick = onDecline,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ThemeSurface,
                            contentColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 0.dp,
                            pressedElevation = 2.dp
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .border(1.dp, PrimaryLight, RoundedCornerShape(12.dp))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            tint = TextPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Decline",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AddFriendDialog(
    onDismiss: () -> Unit,
    onSendRequest: (String) -> Unit
) {
    var userId by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Background,
        title = {
            Text(
                text = "Add Friend",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        },
        text = {
            Column {
                Text(
                    text = "Enter user ID to send a friend request",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(16.dp))
                OnlyCareTextField(
                    value = userId,
                    onValueChange = { userId = it },
                    label = "User ID",
                    placeholder = "Enter User ID",
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (userId.isNotBlank()) onSendRequest(userId) },
                enabled = userId.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Send Request", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}

@Composable
private fun PremiumEmptyState(
    icon: ImageVector,
    title: String,
    subtitle: String
) {
    val scale = remember { Animatable(0.8f) }
    
    LaunchedEffect(Unit) {
        scale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
    }
    
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .scale(scale.value)
                .padding(40.dp)
        ) {
            // Icon with animated background
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(PrimaryLight.copy(alpha = 0.25f))
                    .border(2.dp, PrimaryLight, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(56.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
private fun AnimatedFloatingActionButton(onClick: () -> Unit) {
    val scale = remember { Animatable(0f) }
    
    LaunchedEffect(Unit) {
        scale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            )
        )
    }
    
    FloatingActionButton(
        onClick = onClick,
        containerColor = Primary,
        contentColor = Color.White,
        modifier = Modifier.scale(scale.value),
        elevation = FloatingActionButtonDefaults.elevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp
        )
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Add Friend",
            modifier = Modifier.size(28.dp)
        )
    }
}

