package com.onlycare.app.presentation.screens.main

import android.util.Log
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.onlycare.app.presentation.components.*
import com.onlycare.app.presentation.navigation.Screen
import com.onlycare.app.presentation.theme.*
import com.onlycare.app.utils.getDisplayName

@Composable
fun MaleProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    var showLogoutDialog by remember { mutableStateOf(false) }
    val state by viewModel.state.collectAsState()
    
    // Refresh profile data when screen is opened
    LaunchedEffect(Unit) {
        Log.d("profilePageLog", "📱 MaleProfileScreen opened - LaunchedEffect triggered")
        Log.d("profilePageLog", "Calling viewModel.refresh() to fetch latest profile data")
        viewModel.refresh()
    }
    
    // Show loading state
    if (state.isLoading && state.user == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Background),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Primary)
        }
        return
    }
    
    val user = state.user ?: return
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        // Premium Profile Header
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Primary)  // Flat royal violet header
                    .padding(vertical = 32.dp, horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Animated Profile Picture with Edit Overlay
                PremiumProfileWithEdit(
                    imageUrl = user.profileImage,
                    onEditClick = { navController.navigate(Screen.EditProfile.route) }
                )
                
                // Log profile image URL for debugging
                LaunchedEffect(user.profileImage) {
                    Log.d("profilePageLog", "🖼️ Profile image URL set: ${user.profileImage ?: "null"}")
                    Log.d("profilePageLog", "Profile image will be displayed: ${user.profileImage != null && user.profileImage.isNotEmpty()}")
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                Text(
                    text = user.getDisplayName(),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
                
                // Username if available
                user.username?.let { username ->
                    if (username.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                        text = "@$username",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
        
        // 2x2 Premium Grid Cards
        item {
            Spacer(modifier = Modifier.height(20.dp))
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // First Row: Wallet + Transactions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    PremiumGridCard(
                        icon = Icons.Default.AccountBalanceWallet,
                        title = "Wallet",
                        modifier = Modifier.weight(1f),
                        onClick = { navController.navigate(Screen.Wallet.route) }
                    )
                    
                    PremiumGridCard(
                        icon = Icons.Default.Receipt,
                        title = "Transactions",
                        modifier = Modifier.weight(1f),
                        onClick = { navController.navigate(Screen.Transactions.route) }
                    )
                }
                
                // Second Row: Refer & Earn + Privacy
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    PremiumGridCard(
                        icon = Icons.Default.Share,
                        title = "Refer & Earn",
                        modifier = Modifier.weight(1f),
                        onClick = { navController.navigate(Screen.ReferEarn.route) }
                    )
                    
                    PremiumGridCard(
                        icon = Icons.Default.Security,
                        title = "Privacy",
                        modifier = Modifier.weight(1f),
                        onClick = { navController.navigate(Screen.AccountPrivacy.route) }
                    )
                }
            }
        }
        
        // WhatsApp Channels Card
        item {
            Spacer(modifier = Modifier.height(20.dp))
            
            @OptIn(ExperimentalMaterial3Api::class)
            OnlyCareSoftShadowContainer(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(20.dp),
                shadowOffsetY = 4.dp,
                shadowColor = Border.copy(alpha = 0.28f)
            ) {
                Card(
                    onClick = {
                        // Open WhatsApp support channel
                        val whatsappUrl = "https://wa.me/918012345678" // Replace with actual support number
                        android.content.Intent(android.content.Intent.ACTION_VIEW).also { intent ->
                            intent.data = android.net.Uri.parse(whatsappUrl)
                            navController.context.startActivity(intent)
                        }
                    },
                    colors = CardDefaults.cardColors(
                        containerColor = Background
                    ),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Background)
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                    // WhatsApp Icon with gradient background
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                Color(0xFF25D366)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Chat,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "WhatsApp Channel",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Get updates & exclusive content",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF25D366),
                            fontSize = 13.sp
                        )
                    }
                    
                    // Arrow with WhatsApp color
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF25D366).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = null,
                            tint = Color(0xFF25D366),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
            }
        }
        
        // My Friends Card with Subtitle
        item {
            Spacer(modifier = Modifier.height(20.dp))
            
            @OptIn(ExperimentalMaterial3Api::class)
            OnlyCareSoftShadowContainer(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(20.dp),
                shadowOffsetY = 4.dp,
                shadowColor = Border.copy(alpha = 0.28f)
            ) {
                Card(
                    onClick = { navController.navigate(Screen.Friends.route) },
                    colors = CardDefaults.cardColors(
                        containerColor = Background
                    ),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Background)
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                    val friendsStyle = remember { settingsIconStyleForTitle("My Friends", Primary) }
                    // Icon with colored background
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(friendsStyle.background)
                            .border(1.dp, friendsStyle.border, RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.People,
                            contentDescription = null,
                            tint = friendsStyle.foreground,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "My Friends",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                        text = "View friends & pending requests",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        fontSize = 13.sp
                        )
                    }
                    
                    Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(24.dp)
                    )
                }
            }
            }
        }
        
        // Settings Section with Header
        item {
            Spacer(modifier = Modifier.height(32.dp))
            
            // Section Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(24.dp)
                        .background(Primary, RoundedCornerShape(2.dp))
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    fontSize = 18.sp
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // Settings Items
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PremiumSettingsItem(
                    icon = Icons.Default.Description,
                    title = "Terms & Conditions",
                    onClick = { navController.navigate(Screen.Terms.route) }
                )
                
                PremiumSettingsItem(
                    icon = Icons.Default.Policy,
                    title = "Refund & Cancellation",
                    onClick = { navController.navigate(Screen.RefundPolicy.route) }
                )
                
                PremiumSettingsItem(
                    icon = Icons.Default.Group,
                    title = "Community Guidelines & Moderation Policy",
                    onClick = { navController.navigate(Screen.CommunityGuidelines.route) }
                )
            }
        }
        
        // Logout
        item {
            Spacer(modifier = Modifier.height(16.dp))
            
            PremiumSettingsItem(
                icon = Icons.Default.Logout,
                title = "Logout",
                onClick = { showLogoutDialog = true },
                tint = Color(0xFFFF4444)
            )
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
    
    // Logout Confirmation Dialog
    ConfirmationDialog(
        show = showLogoutDialog,
        onDismiss = { showLogoutDialog = false },
        title = "Logout",
        message = "Are you sure you want to logout?",
        confirmText = "Logout",
        onConfirm = {
            showLogoutDialog = false
            viewModel.logout()
            navController.navigate(Screen.Login.route) {
                popUpTo(0) { inclusive = true }
            }
        }
    )
}

// Premium Profile with Animated Border and Edit Overlay
@Composable
private fun PremiumProfileWithEdit(
    imageUrl: String?,
    onEditClick: () -> Unit
) {
    Box(
        modifier = Modifier.size(120.dp),
        contentAlignment = Alignment.Center
    ) {
        // Flat border (no sweep gradient)
        Box(
            modifier = Modifier
                .size(120.dp)
                .border(2.dp, Border, CircleShape)
        )
        
        // Profile Image (clickable to edit)
        Box(
            modifier = Modifier
                .size(112.dp)
                .clip(CircleShape)
                .background(CardBackground)
                .clickable(onClick = onEditClick)
        ) {
            ProfileImage(
                imageUrl = imageUrl,
                size = 112.dp
            )
        }
        
        // Edit Icon Overlay
        Surface(
            onClick = onEditClick,
            shape = CircleShape,
            color = PrimaryLight,
            modifier = Modifier
                .size(36.dp)
                .align(Alignment.BottomEnd)
                .offset(x = (-4).dp, y = (-4).dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit Profile",
                    tint = Primary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

// Premium 2x2 Grid Card
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PremiumGridCard(
    icon: ImageVector,
    title: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val iconStyle = remember(title) { profileIconStyleForTitle(title) }
    // User request: white card + soft shadow (no blue tint)
    OnlyCareSoftShadowContainer(
        // Make the 2x2 grid cards smaller (less tall)
        modifier = modifier.height(112.dp),
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
            modifier = Modifier.fillMaxSize(),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                    .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Icon with background
            Box(
                modifier = Modifier
                        .size(46.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(iconStyle.background)
                    .border(1.dp, iconStyle.border, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconStyle.foreground,
                        modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                fontSize = 13.sp
            )
        }
    }
    }
}

// Premium Settings Item
@Composable
private fun PremiumSettingsItem(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
    tint: Color = Primary
) {
    val iconStyle = remember(title, tint) { settingsIconStyleForTitle(title, tint) }
    OnlyCareSoftShadowContainer(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        shadowOffsetY = 3.dp,
        shadowColor = Border.copy(alpha = 0.24f)
    ) {
        Surface(
            onClick = onClick,
            color = Background,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Background)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconStyle.background)
                    .border(1.dp, iconStyle.border, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconStyle.foreground,
                    modifier = Modifier.size(22.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = if (title == "Logout") ErrorRed else TextPrimary,
                modifier = Modifier.weight(1f),
                fontSize = 15.sp
            )
            
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
    }
}

private data class IconStyle(
    val background: Color,
    val foreground: Color,
    val border: Color
)

private fun profileIconStyleForTitle(title: String): IconStyle {
    return when (title) {
        "Wallet" -> IconStyle(
            background = Color(0xFFFFEEF2), // soft pink
            foreground = Color(0xFFEC4899), // pink
            border = Color(0xFFEC4899).copy(alpha = 0.18f)
        )
        "Transactions" -> IconStyle(
            background = Color(0xFFECFDF5), // soft green
            foreground = Color(0xFF10B981), // emerald
            border = Color(0xFF10B981).copy(alpha = 0.18f)
        )
        "Refer & Earn" -> IconStyle(
            background = Color(0xFFF5F3FF), // soft violet
            foreground = Color(0xFF7C3AED), // violet
            border = Color(0xFF7C3AED).copy(alpha = 0.18f)
        )
        "Privacy" -> IconStyle(
            background = Color(0xFFEFF6FF), // soft blue
            foreground = Primary, // brand blue
            border = Primary.copy(alpha = 0.18f)
        )
        else -> IconStyle(
            background = PrimaryLight.copy(alpha = 0.22f),
            foreground = Primary,
            border = Primary.copy(alpha = 0.14f)
        )
    }
}

private fun settingsIconStyleForTitle(title: String, fallbackTint: Color): IconStyle {
    return when (title) {
        "Terms & Conditions", "Terms & Condition" -> IconStyle(
            background = Color(0xFFFFFBEB), // soft amber
            foreground = Color(0xFFF59E0B), // amber
            border = Color(0xFFF59E0B).copy(alpha = 0.18f)
        )
        "Refund & Cancellation" -> IconStyle(
            background = Color(0xFFECFDF5), // soft green
            foreground = Color(0xFF10B981),
            border = Color(0xFF10B981).copy(alpha = 0.18f)
        )
        "Community Guidelines & Moderation Policy" -> IconStyle(
            background = Color(0xFFEFFDFB), // soft teal
            foreground = Color(0xFF06B6D4), // cyan
            border = Color(0xFF06B6D4).copy(alpha = 0.18f)
        )
        "Help and Support", "Help & Support", "Support" -> IconStyle(
            background = Color(0xFFF1F5F9), // soft slate
            foreground = TextSecondary,
            border = TextSecondary.copy(alpha = 0.12f)
        )
        "Logout" -> IconStyle(
            background = ErrorRed.copy(alpha = 0.10f),
            foreground = ErrorRed,
            border = ErrorRed.copy(alpha = 0.18f)
        )
        "My Friends" -> IconStyle(
            background = Color(0xFFFFEEF2),
            foreground = Color(0xFFEC4899),
            border = Color(0xFFEC4899).copy(alpha = 0.18f)
        )
        else -> IconStyle(
            background = PrimaryLight.copy(alpha = 0.18f),
            foreground = fallbackTint,
            border = fallbackTint.copy(alpha = 0.14f)
        )
    }
}

// Keep old ProfileMenuItem for backward compatibility (used by FemaleProfileScreen and Settings)
@Composable
fun ProfileMenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    onClick: () -> Unit,
    tint: androidx.compose.ui.graphics.Color = TextPrimary
) {
    Surface(
        onClick = onClick,
        color = PureBlack,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(12.dp))
    ) {
        Row(
            modifier = Modifier
                .background(SurfaceBlack)
                .border(1.dp, BorderSecondary, RoundedCornerShape(12.dp))
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(CardBlack)
                    .border(1.dp, BorderPrimary, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = tint,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = tint,
                modifier = Modifier.weight(1f)
            )
            
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = TextTertiary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}


