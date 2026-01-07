package com.onlycare.app.presentation.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.onlycare.app.presentation.components.*
import com.onlycare.app.presentation.theme.*
import com.onlycare.app.presentation.navigation.Screen

@Composable
fun AccountPrivacyScreen(navController: NavController) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Premium Header with Back Button
            PremiumHeader(
                title = "Privacy",
                onBackClick = { navController.navigateUp() }
            )
            
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 20.dp)
            ) {
                // Privacy Policy Card
                item {
                    PremiumPrivacyCard(
                        icon = Icons.Default.Policy,
                        title = "Privacy Policy",
                        subtitle = "Read our privacy policy",
                        onClick = { navController.navigate(Screen.PrivacyPolicy.route) }
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                }
                
                // Delete Account Card
                item {
                    PremiumPrivacyCard(
                        icon = Icons.Default.DeleteForever,
                        title = "Delete Account",
                        subtitle = "Permanently delete your account",
                        onClick = { showDeleteDialog = true },
                        tint = ErrorRed,
                        isDanger = true
                    )
                }
            }
        }
    }
    
    // Delete Account Confirmation Dialog
    if (showDeleteDialog) {
        ConfirmationDialog(
            show = true,
            onDismiss = { showDeleteDialog = false },
            title = "Delete Account",
            message = "Are you sure you want to permanently delete your account? This action cannot be undone.",
            confirmText = "Delete",
            onConfirm = {
                showDeleteDialog = false
                // Delete account functionality (requires backend API endpoint)
                // For now, navigate to login screen as a placeholder
                // TODO: Add delete account API endpoint in backend
                navController.navigate(Screen.Login.route) {
                    popUpTo(0) { inclusive = true }
                }
            }
        )
    }
}

// Premium Header Component
@Composable
private fun PremiumHeader(
    title: String,
    onBackClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Background)
            .padding(horizontal = 16.dp, vertical = 16.dp)
            .statusBarsPadding(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBackClick) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = Primary
            )
        }
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            fontSize = 20.sp
        )
    }
}

// Premium Privacy Card Component
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PremiumPrivacyCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    tint: Color = Primary,
    isDanger: Boolean = false
) {
    OnlyCareSoftShadowContainer(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp),
        shadowOffsetY = 4.dp,
        shadowColor = Border.copy(alpha = 0.28f)
    ) {
        Card(
            onClick = onClick,
            colors = CardDefaults.cardColors(
                containerColor = Background
            ),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    if (isDanger) {
                        ErrorRed.copy(alpha = 0.06f)
                    } else {
                        Color.Transparent
                    }
                )
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon with background
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        if (isDanger) {
                            ErrorRed.copy(alpha = 0.15f)
                        } else {
                            PrimaryLight.copy(alpha = 0.2f)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = tint,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isDanger) ErrorRed else TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isDanger) ErrorRed else TextSecondary,
                    fontSize = 13.sp
                )
            }
            
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = if (isDanger) tint.copy(alpha = 0.6f) else Color(0xFF666666),
                modifier = Modifier.size(24.dp)
            )
        }
    }
    }
}

