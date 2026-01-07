package com.onlycare.app.presentation.screens.gift

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import com.onlycare.app.presentation.theme.OnlineGreen
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import kotlinx.coroutines.launch
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.onlycare.app.data.remote.dto.GiftData
import com.onlycare.app.presentation.theme.OnlineGreen

private const val TAG_GIFT_BOTTOM_SHEET = "GiftBottomSheet"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GiftBottomSheet(
    receiverId: String,
    callType: String,
    onGiftSelected: (GiftData) -> Unit,
    onDismiss: () -> Unit,
    giftImageViewModel: GiftImageViewModel = hiltViewModel(),
    giftViewModel: GiftViewModel = hiltViewModel(),
    getRemainingTime: suspend (String) -> Result<String>,
    calculateAvailableCoins: (String, String) -> Int
) {
    val giftImageState by giftImageViewModel.state.collectAsState()
    val giftState by giftViewModel.state.collectAsState()
    
    var availableCoins by remember { mutableStateOf(0) }
    var isLoadingCoins by remember { mutableStateOf(false) }
    
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    
    // Handle back button press OUTSIDE ModalBottomSheet for Android 13+ compatibility
    BackHandler(enabled = sheetState.isVisible) {
        scope.launch {
            sheetState.hide()
            onDismiss()
        }
    }
    
    // Fetch gifts when sheet opens
    LaunchedEffect(Unit) {
        Log.d(TAG_GIFT_BOTTOM_SHEET, "========================================")
        Log.d(TAG_GIFT_BOTTOM_SHEET, "🎁 GIFT BOTTOM SHEET OPENED")
        Log.d(TAG_GIFT_BOTTOM_SHEET, "========================================")
        Log.d(TAG_GIFT_BOTTOM_SHEET, "Receiver ID: $receiverId")
        Log.d(TAG_GIFT_BOTTOM_SHEET, "Call Type: $callType")
        Log.d(TAG_GIFT_BOTTOM_SHEET, "📡 Calling API: POST /auth/gifts_list")
        Log.d(TAG_GIFT_BOTTOM_SHEET, "========================================")
        giftImageViewModel.fetchGiftImages()
    }
    
    // Get remaining time and calculate coins
    LaunchedEffect(callType) {
        Log.d(TAG_GIFT_BOTTOM_SHEET, "💰 Fetching remaining time for coin calculation...")
        Log.d(TAG_GIFT_BOTTOM_SHEET, "📡 Calling API: POST /auth/get_remaining_time")
        Log.d(TAG_GIFT_BOTTOM_SHEET, "   Call Type: $callType")
        isLoadingCoins = true
        getRemainingTime(callType).onSuccess { remainingTime ->
            availableCoins = calculateAvailableCoins(remainingTime, callType)
            Log.d(TAG_GIFT_BOTTOM_SHEET, "✅ Remaining Time: $remainingTime")
            Log.d(TAG_GIFT_BOTTOM_SHEET, "✅ Available Coins: $availableCoins")
            isLoadingCoins = false
        }.onFailure { error ->
            Log.e(TAG_GIFT_BOTTOM_SHEET, "❌ Failed to get remaining time: ${error.message}")
            isLoadingCoins = false
        }
    }
    
    // Handle gift sent success
    LaunchedEffect(giftState.giftSent) {
        giftState.giftSent?.let {
            sheetState.hide()
            onDismiss()
        }
    }
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = Modifier.fillMaxHeight(0.7f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header
            Text(
                text = "Send Gift",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                textAlign = TextAlign.Center
            )
            
            // Available coins display - Hidden for audio calls
            if (callType.equals("video", ignoreCase = true)) {
                if (isLoadingCoins) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(20.dp)
                            .align(Alignment.CenterHorizontally)
                    )
                } else {
                    Text(
                        text = "Available Coins: $availableCoins",
                        fontSize = 14.sp,
                        color = OnlineGreen,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
            
            // Error message
            giftImageState.error?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    textAlign = TextAlign.Center
                )
            }
            
            giftState.error?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    textAlign = TextAlign.Center
                )
            }
            
            // Gifts grid
            if (giftImageState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(giftImageState.gifts) { gift ->
                        GiftItem(
                            gift = gift,
                            availableCoins = availableCoins,
                            onClick = {
                                if (availableCoins >= gift.coins) {
                                    Log.d(TAG_GIFT_BOTTOM_SHEET, "========================================")
                                    Log.d(TAG_GIFT_BOTTOM_SHEET, "🎁 GIFT SELECTED IN BOTTOM SHEET")
                                    Log.d(TAG_GIFT_BOTTOM_SHEET, "========================================")
                                    Log.d(TAG_GIFT_BOTTOM_SHEET, "Gift ID: ${gift.id}")
                                    Log.d(TAG_GIFT_BOTTOM_SHEET, "Gift Icon: ${gift.giftIcon}")
                                    Log.d(TAG_GIFT_BOTTOM_SHEET, "Gift Coins: ${gift.coins}")
                                    Log.d(TAG_GIFT_BOTTOM_SHEET, "Available Coins: $availableCoins")
                                    Log.d(TAG_GIFT_BOTTOM_SHEET, "Receiver ID: $receiverId")
                                    Log.d(TAG_GIFT_BOTTOM_SHEET, "Call Type: $callType")
                                    Log.d(TAG_GIFT_BOTTOM_SHEET, "📡 Will call API: POST /auth/send_gifts")
                                    Log.d(TAG_GIFT_BOTTOM_SHEET, "========================================")
                                    onGiftSelected(gift)
                                } else {
                                    Log.w(TAG_GIFT_BOTTOM_SHEET, "⚠️ Insufficient coins! Gift costs ${gift.coins} but only $availableCoins available")
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GiftItem(
    gift: GiftData,
    availableCoins: Int,
    onClick: () -> Unit
) {
    val canAfford = availableCoins >= gift.coins
    
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (canAfford) Color.White else Color.Gray.copy(alpha = 0.3f)
            )
            .clickable(enabled = canAfford, onClick = onClick)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Image(
            painter = rememberAsyncImagePainter(gift.giftIcon),
            contentDescription = "Gift ${gift.id}",
            modifier = Modifier
                .size(60.dp)
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "🪙",
                fontSize = 12.sp
            )
            Text(
                text = "${gift.coins}",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (canAfford) Color.Black else Color.Gray
            )
        }
    }
}

