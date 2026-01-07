package com.onlycare.app.presentation.screens.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.onlycare.app.presentation.components.*
import com.onlycare.app.presentation.navigation.Screen
import com.onlycare.app.presentation.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(
    navController: NavController,
    viewModel: ChatListViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    
    Scaffold(
        containerColor = PureBlack,
        topBar = {
            OnlyCareTopAppBar(
                title = "Messages",
                onBackClick = { navController.navigateUp() }
            )
        }
    ) { paddingValues ->
        if (state.conversations.isEmpty()) {
            EmptyState(
                icon = Icons.Default.ChatBubbleOutline,
                title = "No Conversations",
                message = "Start chatting with people you call"
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(PureBlack)
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.conversations) { conversation ->
                    Surface(
                        onClick = {
                            navController.navigate(Screen.Chat.createRoute(conversation.userId))
                        },
                        color = SurfaceBlack,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.border(
                            width = 1.dp,
                            color = BorderPrimary,
                            shape = RoundedCornerShape(16.dp)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Premium Profile Image
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(RoundedCornerShape(30.dp))
                                    .background(CardBlack)
                                    .border(1.dp, BorderPrimary, RoundedCornerShape(30.dp))
                            ) {
                                ProfileImage(
                                    imageUrl = conversation.userImage,
                                    size = 56.dp,
                                    showOnlineStatus = true,
                                    isOnline = conversation.isOnline
                                )
                            }
                            
                            Spacer(modifier = Modifier.width(16.dp))
                            
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = conversation.userName,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    
                                    Text(
                                        text = formatTimestamp(conversation.lastMessageTime),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextTertiary
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(6.dp))
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = conversation.lastMessage,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextSecondary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )
                                    
                                    if (conversation.unreadCount > 0) {
                                        Badge(
                                            containerColor = White,
                                            contentColor = Black
                                        ) {
                                            Text(
                                                text = if (conversation.unreadCount > 9) "9+" else conversation.unreadCount.toString(),
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    
    return when {
        diff < 60000 -> "Just now"
        diff < 3600000 -> "${diff / 60000}m ago"
        diff < 86400000 -> "${diff / 3600000}h ago"
        else -> SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date(timestamp))
    }
}

