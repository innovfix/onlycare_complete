package com.onlycare.app.presentation.screens.wallet

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import com.onlycare.app.domain.model.CoinPackage
import com.onlycare.app.presentation.components.EmptyState
import com.onlycare.app.presentation.components.OnlyCareListItemSkeleton
import com.onlycare.app.presentation.components.OnlyCarePrimaryButton
import com.onlycare.app.presentation.components.OnlyCareSoftShadowContainer
import com.onlycare.app.presentation.components.OnlyCareSkeletonTextLine
import com.onlycare.app.presentation.components.OnlyCareTopAppBar
import com.onlycare.app.presentation.navigation.Screen
import com.onlycare.app.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletScreen(
    navController: NavController,
    message: String? = null,
    viewModel: WalletViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var selectedPackage by remember { mutableStateOf<CoinPackage?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Message/refresh coming back from Payment screen
    val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle
    val shouldRefresh = savedStateHandle?.get<Boolean>("wallet_refresh") == true
    
    // Show message if provided
    LaunchedEffect(message) {
        if (message != null && message.isNotEmpty()) {
            delay(600) // Delay for smooth transition and visibility
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Long
            )
        }
    }

    LaunchedEffect(shouldRefresh) {
        if (shouldRefresh) {
            viewModel.retry()
            savedStateHandle?.remove<Boolean>("wallet_refresh")
        }
    }
    
    Scaffold(
        containerColor = Background,
        topBar = {
            OnlyCareTopAppBar(
                title = "Wallet",
                onBackClick = { navController.navigateUp() }
            )
        },
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) { data ->
                // Premium Elevated Snackbar with Icon
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = Surface,
                    shadowElevation = 0.dp,
                    tonalElevation = 0.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Info Icon
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = PrimaryLight
                        ) {
                            Icon(
                                imageVector = Icons.Default.Circle,
                                contentDescription = null,
                                tint = Primary,
                                modifier = Modifier
                                    .padding(10.dp)
                                    .size(28.dp)
                            )
                        }
                        
                        // Message Text
                        Text(
                            text = data.visuals.message,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            fontSize = 16.sp,
                            lineHeight = 22.sp,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        if (state.error != null && state.packages.isEmpty()) {
            // Full screen error
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Background)
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = state.error ?: "Unknown error",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextSecondary,
                        modifier = Modifier.padding(horizontal = 32.dp)
                    )
                    OnlyCarePrimaryButton(
                        text = "Retry",
                        onClick = { viewModel.retry() }
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Background)
                    .padding(paddingValues)
            ) {
                // Premium Balance Card
                OnlyCareSoftShadowContainer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    shape = RoundedCornerShape(18.dp),
                    shadowOffsetY = 4.dp,
                    shadowColor = Border.copy(alpha = 0.30f)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(Background)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 18.dp, vertical = 18.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                        Text(
                            text = "CURRENT BALANCE",
                            style = MaterialTheme.typography.labelLarge,
                            color = TextSecondary,
                            letterSpacing = 2.sp
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        if (state.isLoading && state.currentBalance == 0) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                OnlyCareSkeletonTextLine(
                                    widthFraction = 0.42f,
                                    height = 28.dp,
                                    shape = RoundedCornerShape(14.dp)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                OnlyCareSkeletonTextLine(
                                    widthFraction = 0.26f,
                                    height = 14.dp,
                                    shape = RoundedCornerShape(10.dp)
                                )
                            }
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                GoldCoinIcon(size = 34.dp)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = state.currentBalance.toString(),
                                    style = MaterialTheme.typography.displayMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary,
                                    letterSpacing = 1.sp
                                )
                            }
                            // Show refresh indicator if loading while balance exists
                            if (state.isLoading) {
                                Spacer(modifier = Modifier.width(8.dp))
                                CircularProgressIndicator(
                                    color = Primary,
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "Premium Coins",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextTertiary
                        )
                    }
                }
                }
            
            // Coin Packages
            Text(
                text = "Select Package",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Show loading or empty state for packages
            if (state.isLoading && state.packages.isEmpty()) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(6) { // skeleton placeholders
                        OnlyCareSoftShadowContainer(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            shadowOffsetY = 4.dp,
                            shadowColor = ThemeBorder.copy(alpha = 0.28f),
                            showSideShadows = true
                        ) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Background),
                                shape = RoundedCornerShape(18.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    OnlyCareSkeletonTextLine(widthFraction = 0.60f, height = 14.dp)
                                    OnlyCareSkeletonTextLine(widthFraction = 0.40f, height = 12.dp)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    OnlyCareListItemSkeleton(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(54.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            } else if (state.packages.isEmpty() && state.error == null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    EmptyState(
                        icon = Icons.Default.AccountBalanceWallet,
                        title = "No Packages Available",
                        message = "Coin packages will appear here"
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(state.packages.size, key = { state.packages[it].id }) { index ->
                        val pkg = state.packages[index]
                        CoinPackageCard(
                            coinPackage = pkg,
                            selected = selectedPackage?.id == pkg.id,
                            onClick = { selectedPackage = pkg }
                        )
                    }
                }
            }
            
            // Bottom Button
            if (selectedPackage != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Surface),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        OnlyCarePrimaryButton(
                            text = "Add ${selectedPackage?.coins} Coins - ₹${selectedPackage?.price}",
                            onClick = {
                                selectedPackage?.let {
                                    navController.navigate(Screen.Payment.createRoute(it.id))
                                }
                            }
                        )
                    }
                }
            }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoinPackageCard(
    coinPackage: CoinPackage,
    selected: Boolean,
    onClick: () -> Unit
) {
    OnlyCareSoftShadowContainer(
        modifier = Modifier
            // Slightly taller ratio => smaller height (more compact grid)
            .aspectRatio(0.9f),
        shape = RoundedCornerShape(18.dp),
        shadowOffsetY = 3.dp,
        shadowColor = Border.copy(alpha = 0.26f)
    ) {
        Card(
            onClick = onClick,
            colors = CardDefaults.cardColors(
                // White grid + subtle selected highlight (no blue border)
                containerColor = if (selected) PrimaryLight.copy(alpha = 0.22f) else Background
            ),
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier.matchParentSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
            // Badge - Fixed height container
            Box(
                modifier = Modifier.height(22.dp),
                contentAlignment = Alignment.Center
            ) {
                if (coinPackage.isPopular || coinPackage.isBestValue) {
                    Surface(
                        color = Primary,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = if (coinPackage.isPopular) "POPULAR" else "BEST VALUE",
                            style = MaterialTheme.typography.labelSmall,
                            color = White,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }
            
            // Coins - Main content
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    PackageCoinIcon(coins = coinPackage.coins, size = 22.dp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = coinPackage.coins.toString(),
                        fontSize = if (coinPackage.coins >= 10000) 22.sp else 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        maxLines = 1,
                        softWrap = false
                    )
                }
                
                // Original Price
                Text(
                    text = "₹${coinPackage.originalPrice.toInt()}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    textDecoration = TextDecoration.LineThrough,
                    maxLines = 1,
                    softWrap = false
                )
                
                // Current Price
                Text(
                    text = "₹${coinPackage.price.toInt()}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Primary,
                    maxLines = 1,
                    softWrap = false
                )
            }
            
            // Savings Badge - Bottom
            Surface(
                color = SuccessGreen,
                shape = RoundedCornerShape(6.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SuccessGreen)
            ) {
                Text(
                    text = "SAVE ${coinPackage.discount}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = White,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
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
    val darkGold = androidx.compose.ui.graphics.Color(0xFFB8860B)
    val midGold = androidx.compose.ui.graphics.Color(0xFFDAA520)
    val brightGold = androidx.compose.ui.graphics.Color(0xFFFFD700)

    Box(modifier = modifier.size(size)) {
        // Bottom layer
        Icon(
            imageVector = Icons.Default.Circle,
            contentDescription = null,
            tint = darkGold,
            modifier = Modifier
                .size(size)
                .offset(x = 1.dp, y = 2.dp)
        )
        // Middle layer
        Icon(
            imageVector = Icons.Default.Circle,
            contentDescription = null,
            tint = midGold,
            modifier = Modifier
                .size(size - 1.dp)
                .offset(y = 1.dp)
        )
        // Top layer
        Icon(
            imageVector = Icons.Default.Circle,
            contentDescription = null,
            tint = brightGold,
            modifier = Modifier.size(size - 2.dp)
        )
    }
}

@Composable
private fun PackageCoinIcon(
    coins: Int,
    size: Dp,
    modifier: Modifier = Modifier
) {
    when {
        coins < 500 -> {
            // 100 coins -> one coin
            GoldCoinIcon(size = size, modifier = modifier)
        }
        coins < 1000 -> {
            // 500 coins -> stack of coins
            CoinStackIcon(size = size, modifier = modifier)
        }
        coins < 2500 -> {
            // 1000 coins -> bag of coins
            CoinBagIcon(size = size, modifier = modifier)
        }
        else -> {
            // 2500 coins -> cart of coins
            CoinCartIcon(size = size, modifier = modifier)
        }
    }
}

@Composable
private fun CoinStackIcon(
    size: Dp,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.size(size + 6.dp)) {
        GoldCoinIcon(
            size = size,
            modifier = Modifier
                .align(Alignment.Center)
                .offset(x = (-3).dp, y = 2.dp)
        )
        GoldCoinIcon(
            size = size,
            modifier = Modifier
                .align(Alignment.Center)
                .offset(x = 2.dp, y = (-1).dp)
        )
    }
}

@Composable
private fun CoinBagIcon(
    size: Dp,
    modifier: Modifier = Modifier
) {
    val darkGold = androidx.compose.ui.graphics.Color(0xFFB8860B)
    val midGold = androidx.compose.ui.graphics.Color(0xFFDAA520)
    Box(modifier = modifier.size(size + 6.dp)) {
        Icon(
            imageVector = Icons.Default.ShoppingBag,
            contentDescription = null,
            tint = midGold,
            modifier = Modifier
                .size(size + 6.dp)
                .align(Alignment.Center)
        )
        // coin badge
        GoldCoinIcon(
            size = (size * 0.6f),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 2.dp, y = (-1).dp)
        )
        // small highlight dot (no shadow)
        Icon(
            imageVector = Icons.Default.Circle,
            contentDescription = null,
            tint = darkGold.copy(alpha = 0.25f),
            modifier = Modifier
                .size((size * 0.45f))
                .align(Alignment.BottomStart)
                .offset(x = 2.dp, y = (-1).dp)
        )
    }
}

@Composable
private fun CoinCartIcon(
    size: Dp,
    modifier: Modifier = Modifier
) {
    val midGold = androidx.compose.ui.graphics.Color(0xFFDAA520)
    Box(modifier = modifier.size(size + 8.dp)) {
        Icon(
            imageVector = Icons.Default.ShoppingCart,
            contentDescription = null,
            tint = midGold,
            modifier = Modifier
                .size(size + 8.dp)
                .align(Alignment.Center)
        )
        GoldCoinIcon(
            size = (size * 0.55f),
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = 2.dp, y = 1.dp)
        )
        GoldCoinIcon(
            size = (size * 0.55f),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = (-1).dp, y = 1.dp)
        )
    }
}

