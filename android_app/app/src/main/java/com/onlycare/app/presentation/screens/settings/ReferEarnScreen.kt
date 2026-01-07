package com.onlycare.app.presentation.screens.settings

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.onlycare.app.presentation.components.*
import com.onlycare.app.presentation.theme.*
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReferEarnScreen(
    navController: NavController,
    viewModel: ReferEarnViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val isFemale = viewModel.isFemale
    val lifecycleOwner = LocalLifecycleOwner.current

    // Always refresh when opening this screen + when resuming back to it
    LaunchedEffect(Unit) {
        viewModel.refresh()
    }
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refresh()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    
    // Determine reward type and values
    val rewardType = state.rewardType ?: if (isFemale) "RUPEES" else "COINS"
    val perInviteReward = if (isFemale) {
        state.rupeesPerInvite ?: 50 // Default 50 Rs for female
    } else {
        state.coinsPerInvite ?: 10 // Default 10 coins for male
    }
    val totalEarned = if (isFemale) {
        state.totalRupeesEarned ?: 0
    } else {
        state.totalCoinsEarned ?: 0
    }
    val rewardLabel = if (isFemale) "Total Rupees Earned" else "Total Coins Earned"
    val perInviteLabel = if (isFemale) "Per Invite (Rs)" else "Per Invite (Coins)"
    
    // Show copy success toast
    LaunchedEffect(state.copySuccess) {
        if (state.copySuccess) {
            Toast.makeText(context, "Code copied to clipboard!", Toast.LENGTH_SHORT).show()
        }
    }
    
    // Show error toast
    LaunchedEffect(state.error) {
        state.error?.let { error ->
            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }
    
    Scaffold(
        containerColor = Background,
        topBar = {
            OnlyCareTopAppBar(
                title = "Refer & Earn",
                onBackClick = { navController.navigateUp() }
            )
        }
    ) { paddingValues ->
        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Primary)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Background)
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(12.dp))
                
                // Premium Statistics Cards
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // My Invites Card
                    PremiumStatCard(
                        icon = Icons.Default.Person,
                        label = "My Invites",
                        value = state.myInvites.toString(),
                        modifier = Modifier.weight(1f)
                    )
                    
                    // Per Invite Card
                    PremiumStatCard(
                        icon = Icons.Default.Circle,
                        label = perInviteLabel,
                        value = perInviteReward.toString(),
                        modifier = Modifier.weight(1f)
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Premium Total Coins Earned Card
                OnlyCareSoftShadowContainer(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    shadowOffsetY = 4.dp,
                    shadowColor = Border.copy(alpha = 0.28f)
                ) {
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = Background,
                        shadowElevation = 0.dp
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            contentAlignment = Alignment.Center
                        ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = rewardLabel,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                fontSize = 16.sp
                            )
                            
                            Spacer(modifier = Modifier.height(10.dp))
                            
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                if (isFemale) {
                                    // Show ₹ symbol for rupees
                                    Text(
                                        text = "₹",
                                        style = MaterialTheme.typography.headlineLarge,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = TextPrimary,
                                        fontSize = 32.sp
                                    )
                                } else {
                                    GoldCoinIcon(size = 24.dp)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = totalEarned.toString(),
                                    style = MaterialTheme.typography.headlineLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = TextPrimary,
                                    fontSize = 32.sp
                                )
                            }
                        }
                    }
                }
                }
                
                Spacer(modifier = Modifier.height(14.dp))
                
                // Premium Referral Code Card
                OnlyCareSoftShadowContainer(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    shadowOffsetY = 4.dp,
                    shadowColor = Border.copy(alpha = 0.28f)
                ) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        color = Background,
                        shadowElevation = 0.dp,
                        tonalElevation = 0.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "My Invite Code",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                
                                // Copy Icon
                                IconButton(
                                    onClick = {
                                        state.referralCode?.let { code ->
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                            val clip = ClipData.newPlainText("Referral Code", code)
                                            clipboard.setPrimaryClip(clip)
                                            viewModel.onCopyCode()
                                            Toast.makeText(context, "Code copied!", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "Copy",
                                        tint = Primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            // Premium Code Display
                            Text(
                                text = state.referralCode ?: "Loading...",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextPrimary,
                                fontSize = 22.sp,
                                letterSpacing = 1.5.sp,
                                modifier = Modifier.fillMaxWidth()
                            )
                            
                            Spacer(modifier = Modifier.height(14.dp))
                            
                            // Premium WhatsApp Share Button
                            Button(
                                onClick = {
                                    // Share via WhatsApp using pre-built message from API
                                    val rewardText = if (isFemale) {
                                        "${perInviteReward} Rs"
                                    } else {
                                        "${perInviteReward} coins"
                                    }
                                    val defaultMessage = if (isFemale) {
                                        "Join me on OnlyCare! Use my referral code ${state.referralCode ?: "ONLYCARE"}. When you complete KYC, I earn $rewardText. Download now."
                                    } else {
                                        "Join me on OnlyCare! Use my referral code ${state.referralCode ?: "ONLYCARE"}. When you register, I earn $rewardText. Download now."
                                    }
                                    val message = state.shareMessage ?: defaultMessage
                                    val intent = Intent(Intent.ACTION_VIEW).apply {
                                        data = Uri.parse("https://wa.me/?text=${Uri.encode(message)}")
                                    }
                                    context.startActivity(intent)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF25D366),
                                    contentColor = White
                                ),
                                elevation = ButtonDefaults.buttonElevation(
                                    defaultElevation = 0.dp,
                                    pressedElevation = 0.dp
                                ),
                                contentPadding = PaddingValues(vertical = 13.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = White
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Share on WhatsApp",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = White
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(18.dp))
                
                // How to Earn Section
                Text(
                    text = if (isFemale) "How to Earn Rs?" else "How to Get Coins?",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Premium Instructions Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Share Your Link
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            modifier = Modifier.size(68.dp),
                            shape = CircleShape,
                            color = CardBackground,
                            shadowElevation = 0.dp
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .border(
                                        width = 1.dp,
                                        color = Border,
                                        shape = CircleShape
                                    )
                                    .background(Background),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = null,
                                    tint = Primary,
                                    modifier = Modifier.size(30.dp)
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(10.dp))
                        
                        Text(
                            text = "Share your\nlink",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    
                    // Arrow
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier
                            .size(24.dp)
                            .padding(horizontal = 6.dp)
                    )
                    
                    // Get Free Reward
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            modifier = Modifier.size(68.dp),
                            shape = CircleShape,
                            color = CardBackground,
                            shadowElevation = 0.dp
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .border(
                                        width = 1.dp,
                                        color = Border,
                                        shape = CircleShape
                                    )
                                    .background(Background),
                                contentAlignment = Alignment.Center
                            ) {
                                GoldCoinIcon(size = 30.dp)
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(10.dp))
                        
                        Text(
                            text = if (isFemale) "Earn 50 Rs\n(After KYC)" else "Get FREE\nCoins",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // extra bottom space so last text never clips behind system nav
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

/**
 * Premium Statistics Card
 */
@Composable
fun PremiumStatCard(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    OnlyCareSoftShadowContainer(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        shadowOffsetY = 4.dp,
        shadowColor = Border.copy(alpha = 0.28f)
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = Background
            ),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Icon with gradient background
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = Background,
                        shape = CircleShape
                    )
                    .border(
                        width = 1.dp,
                        color = Border,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (label == "Per Invite") {
                    GoldCoinIcon(size = 22.dp)
                } else {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(10.dp))
            
            // Value
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Label
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.3.sp
            )
        }
    }
    }
}

@Composable
private fun GoldCoinIcon(
    size: Dp,
    modifier: Modifier = Modifier
) {
    // Simple 3-layer "3D" coin (no shadows)
    val darkGold = Color(0xFFB8860B)
    val midGold = Color(0xFFDAA520)
    val brightGold = Color(0xFFFFD700)

    Box(modifier = modifier.size(size)) {
        Icon(
            imageVector = Icons.Default.Circle,
            contentDescription = null,
            tint = darkGold,
            modifier = Modifier
                .size(size)
                .offset(x = 1.dp, y = 2.dp)
        )
        Icon(
            imageVector = Icons.Default.Circle,
            contentDescription = null,
            tint = midGold,
            modifier = Modifier
                .size(size - 1.dp)
                .offset(y = 1.dp)
        )
        Icon(
            imageVector = Icons.Default.Circle,
            contentDescription = null,
            tint = brightGold,
            modifier = Modifier.size(size - 2.dp)
        )
    }
}

