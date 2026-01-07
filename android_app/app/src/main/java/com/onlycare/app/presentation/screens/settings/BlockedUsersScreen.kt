package com.onlycare.app.presentation.screens.settings

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.onlycare.app.presentation.components.*
import com.onlycare.app.presentation.theme.*
import com.onlycare.app.utils.getDisplayName

@Composable
fun BlockedUsersScreen(
    navController: NavController,
    viewModel: BlockedUsersViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    
    // Show success message
    LaunchedEffect(state.successMessage) {
        state.successMessage?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            viewModel.clearSuccessMessage()
        }
    }
    
    Scaffold(
        topBar = {
            OnlyCareTopAppBar(
                title = "Blocked Users",
                onBackClick = { navController.navigateUp() }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                state.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = White
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
                            tint = TextGray,
                            modifier = Modifier.size(64.dp)
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = state.error ?: "Unknown error",
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextGray
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Button(
                            onClick = { viewModel.retry() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = White,
                                contentColor = Black
                            )
                        ) {
                            Text("Retry")
                        }
                    }
                }
                
                state.blockedUsers.isEmpty() -> {
                    EmptyState(
                        icon = Icons.Default.Block,
                        title = "No Blocked Users",
                        message = "You haven't blocked anyone yet"
                    )
                }
                
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(state.blockedUsers) { user ->
                            Surface(
                                color = DarkGray
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    ProfileImage(
                                        imageUrl = user.profileImage,
                                        size = 56.dp
                                    )
                                    
                                    Spacer(modifier = Modifier.width(16.dp))
                                    
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = user.getDisplayName(),
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = White
                                        )
                                        user.bio?.let { bio ->
                                            Text(
                                                text = bio.take(50) + if (bio.length > 50) "..." else "",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = TextGray
                                            )
                                        }
                                    }
                                    
                                    TextButton(
                                        onClick = { viewModel.unblockUser(user.id) },
                                        enabled = !state.isUnblocking
                                    ) {
                                        Text("Unblock", color = if (state.isUnblocking) TextGray else OnlineGreen)
                                    }
                                }
                            }
                            Divider(color = DividerGray)
                        }
                    }
                }
            }
        }
    }
}

