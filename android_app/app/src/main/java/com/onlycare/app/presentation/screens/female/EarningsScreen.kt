package com.onlycare.app.presentation.screens.female

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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavController
import com.onlycare.app.presentation.navigation.Screen
import com.onlycare.app.presentation.components.OnlyCareListItemSkeleton
import com.onlycare.app.presentation.components.OnlyCareSoftShadowContainer
import com.onlycare.app.presentation.theme.*
import com.onlycare.app.presentation.theme.Surface as ThemeSurface
import com.onlycare.app.presentation.theme.Border as ThemeBorder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EarningsScreen(
    navController: NavController,
    viewModel: EarningsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var showPaymentMethodSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    val lifecycleOwner = LocalLifecycleOwner.current

    // Refresh amounts quickly whenever we return to this screen
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshFast()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    
    Scaffold(
        containerColor = Background,  // White background
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Earnings",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = White,  // White text on purple
                        fontSize = 22.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = White  // White icon on purple
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
containerColor = Primary  // Purple app bar
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Background)  // White background
                .padding(paddingValues)
                .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))
            
            // Premium Balance Card
            OnlyCareSoftShadowContainer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp),
                shape = RoundedCornerShape(24.dp),
                shadowOffsetY = 6.dp,
                shadowColor = ThemeBorder.copy(alpha = 0.28f),
                showSideShadows = true
            ) {
                Card(
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    colors = CardDefaults.cardColors(
                        // Porter BLUE: darker premium card
                        containerColor = PrimaryDark
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .border(1.dp, Primary, RoundedCornerShape(24.dp))
                            .padding(24.dp)
                    ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Top section with icon and label
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.White.copy(alpha = 0.1f))
                                    .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccountBalanceWallet,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = "Current Balance",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "Available for withdrawal",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.92f),
                                    fontSize = 12.sp
                                )
                            }
                        }
                        
                        // Balance amount
                        Text(
                            text = "₹${String.format("%.1f", state.currentBalance)}",
                            style = MaterialTheme.typography.displayLarge,
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 56.sp,
                            letterSpacing = (-1).sp
                        )
                        
                        // Withdraw button
                        Button(
                            onClick = { showPaymentMethodSheet = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(16.dp),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 6.dp,
                                pressedElevation = 10.dp
                            )
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccountBalanceWallet,
                                    contentDescription = null,
                                    tint = Color.Black,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Withdraw",
                                    color = Color.Black,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 18.sp,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }
                    }
                }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Transaction History Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Transactions",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    fontSize = 20.sp
                )
                
                TextButton(
                    onClick = { navController.navigate(Screen.Transactions.route) }
                ) {
                    Text(
                        text = "View All",
                        color = Primary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Recent Transactions List
            if (state.isLoadingTransactions) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    repeat(4) {
                        OnlyCareSoftShadowContainer(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            shadowOffsetY = 4.dp,
                            shadowColor = ThemeBorder.copy(alpha = 0.28f),
                            showSideShadows = true
                        ) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, PrimaryLight, RoundedCornerShape(16.dp)),
                                colors = CardDefaults.cardColors(containerColor = ThemeSurface),
                                shape = RoundedCornerShape(16.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                            ) {
                                OnlyCareListItemSkeleton()
                            }
                        }
                    }
                }
            } else if (state.recentTransactions.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = White),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Receipt,
                            contentDescription = null,
                            tint = Color(0xFF666666),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No transactions yet",
                            color = Color(0xFF888888),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    state.recentTransactions.take(5).forEach { transaction ->
                        TransactionItem(transaction = transaction)
                    }
                }
            }
        }
    }
    
    // Payment Method Bottom Sheet
    if (showPaymentMethodSheet) {
        ModalBottomSheet(
            onDismissRequest = { showPaymentMethodSheet = false },
            sheetState = sheetState,
            containerColor = ThemeSurface,
            dragHandle = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .width(40.dp)
                            .height(4.dp)
                            .background(ThemeBorder, RoundedCornerShape(2.dp))
                    )
                }
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ThemeSurface)
                    .padding(horizontal = 20.dp)
                    .padding(top = 8.dp, bottom = 40.dp)
            ) {
                // Title
                Text(
                    text = "Payment Method",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                
                Text(
                    text = "Select your preferred withdrawal method",
                    fontSize = 13.sp,
                    color = TextSecondary,
                    modifier = Modifier.padding(bottom = 20.dp)
                )
                
                // UPI Payment Option (Always Selected)
                PremiumPaymentMethodOption(
                    icon = Icons.Default.AccountBalance,
                    title = "UPI Payment",
                    subtitle = "Fast & Secure withdrawal",
                    isSelected = true,
                    onClick = { }
                )
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Continue Button
                Button(
                    onClick = {
                        showPaymentMethodSheet = false
                        navController.navigate(Screen.Withdraw.route)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Primary,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(16.dp),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 0.dp,
                        pressedElevation = 0.dp
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Continue",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp,
                        letterSpacing = 0.5.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PremiumPaymentMethodOption(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = ThemeSurface
        ),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 2.dp,
                color = if (isSelected) Primary else PrimaryLight,
                shape = RoundedCornerShape(18.dp)
            ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Radio button
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .border(
                        width = 2.dp,
                        color = if (isSelected) Primary else PrimaryLight,
                        shape = CircleShape
                    )
                    .background(
                        if (isSelected) Primary else Color.Transparent
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Icon
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(PrimaryLight.copy(alpha = if (isSelected) 0.35f else 0.22f))
                    .border(
                        1.dp,
                        if (isSelected) Primary else PrimaryLight,
                        RoundedCornerShape(14.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Text
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    fontSize = 13.sp,
                    color = TextSecondary,
                    fontWeight = FontWeight.Medium
                )
            }
            
            // Checkmark indicator
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun TransactionItem(transaction: com.onlycare.app.domain.model.Transaction) {
    val (iconBackground, iconTint) = when (transaction.type) {
        com.onlycare.app.domain.model.TransactionType.WITHDRAWAL ->
            PrimaryLight.copy(alpha = 0.25f) to Primary
        com.onlycare.app.domain.model.TransactionType.CALL ->
            SuccessGreen.copy(alpha = 0.18f) to SuccessGreen
        com.onlycare.app.domain.model.TransactionType.BONUS ->
            WarningOrange.copy(alpha = 0.18f) to WarningOrange
        com.onlycare.app.domain.model.TransactionType.PURCHASE ->
            PrimaryLight.copy(alpha = 0.18f) to Primary
        com.onlycare.app.domain.model.TransactionType.GIFT ->
            PrimaryLight.copy(alpha = 0.18f) to Primary
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = White),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconBackground)
                    .border(1.dp, ThemeBorder.copy(alpha = 0.8f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (transaction.type) {
                        com.onlycare.app.domain.model.TransactionType.WITHDRAWAL -> Icons.Default.AccountBalance
                        com.onlycare.app.domain.model.TransactionType.CALL -> Icons.Default.Phone
                        com.onlycare.app.domain.model.TransactionType.BONUS -> Icons.Default.Circle
                        com.onlycare.app.domain.model.TransactionType.PURCHASE -> Icons.Default.AddCircle
                        com.onlycare.app.domain.model.TransactionType.GIFT -> Icons.Default.CardGiftcard
                    },
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(22.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(14.dp))
            
            // Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.title.ifEmpty {
                        when (transaction.type) {
                            com.onlycare.app.domain.model.TransactionType.WITHDRAWAL -> "Withdrawal"
                            com.onlycare.app.domain.model.TransactionType.CALL -> "Call Earnings"
                            com.onlycare.app.domain.model.TransactionType.BONUS -> "Referral Bonus"
                            com.onlycare.app.domain.model.TransactionType.PURCHASE -> "Coin Purchase"
                            com.onlycare.app.domain.model.TransactionType.GIFT -> "Gift Received"
                        }
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formatTransactionDate(transaction.timestamp),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    fontSize = 13.sp
                )
            }
            
            // Amount
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = if (transaction.type == com.onlycare.app.domain.model.TransactionType.WITHDRAWAL) 
                        "-₹${transaction.amount}" 
                    else 
                        "+₹${transaction.amount}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (transaction.type == com.onlycare.app.domain.model.TransactionType.WITHDRAWAL) 
                        Color(0xFFFF6B6B) 
                    else 
                        Color(0xFF4CAF50),
                    fontSize = 16.sp
                )
            }
        }
    }
}

private fun formatTransactionDate(timestamp: Long): String {
    val sdf = java.text.SimpleDateFormat("MMM dd, yyyy • HH:mm", java.util.Locale.getDefault())
    return sdf.format(java.util.Date(timestamp))
}


