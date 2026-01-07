package com.onlycare.app.presentation.screens.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.onlycare.app.domain.model.Message
import com.onlycare.app.presentation.components.ProfileImage
import com.onlycare.app.presentation.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ChatScreen(
    navController: NavController,
    viewModel: ChatViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var messageText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    
    Scaffold(
        topBar = {
            Surface(
                color = Black,
                tonalElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = White
                        )
                    }
                    
                    ProfileImage(
                        imageUrl = null,
                        size = 40.dp,
                        showOnlineStatus = true,
                        isOnline = true
                    )
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Priya",
                            style = MaterialTheme.typography.titleMedium,
                            color = White
                        )
                        Text(
                            text = "Online",
                            style = MaterialTheme.typography.bodySmall,
                            color = OnlineGreen
                        )
                    }
                    
                    IconButton(onClick = { /* More options */ }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More",
                            tint = White
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Messages List
            LazyColumn(
                modifier = Modifier.weight(1f),
                state = listState,
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                reverseLayout = true
            ) {
                items(state.messages.reversed()) { message ->
                    MessageBubble(
                        message = message,
                        isCurrentUser = message.senderId == "current_user"
                    )
                }
            }
            
            // Message Input
            Surface(
                color = DarkGray,
                tonalElevation = 3.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = messageText,
                        onValueChange = { messageText = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Type a message...", color = TextGray) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = White,
                            unfocusedTextColor = White,
                            focusedContainerColor = MediumGray,
                            unfocusedContainerColor = MediumGray,
                            focusedBorderColor = DividerGray,
                            unfocusedBorderColor = DividerGray,
                            cursorColor = White
                        ),
                        shape = RoundedCornerShape(24.dp),
                        maxLines = 3
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    IconButton(
                        onClick = {
                            if (messageText.isNotBlank()) {
                                viewModel.sendMessage(messageText)
                                messageText = ""
                            }
                        },
                        enabled = messageText.isNotBlank()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Send",
                            tint = if (messageText.isNotBlank()) White else TextGray
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MessageBubble(
    message: Message,
    isCurrentUser: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isCurrentUser) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            color = if (isCurrentUser) White else MediumGray,
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isCurrentUser) 16.dp else 4.dp,
                bottomEnd = if (isCurrentUser) 4.dp else 16.dp
            ),
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    text = message.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isCurrentUser) Black else White
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = formatTimestamp(message.timestamp),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isCurrentUser) Black.copy(alpha = 0.6f) else TextGray
                )
            }
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}



