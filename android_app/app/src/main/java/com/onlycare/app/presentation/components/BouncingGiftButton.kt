package com.onlycare.app.presentation.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.onlycare.app.presentation.theme.OnlineGreen

/**
 * Bouncing gift button that shows on the right side of the screen
 * Continuously bounces to attract attention
 * Clicking it opens the gift bottom sheet
 */
@Composable
fun BouncingGiftButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Continuous bounce animation - slower and smaller
    val infiniteTransition = rememberInfiniteTransition(label = "bounce")
    
    val bounceOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -6f,  // Reduced from -12f for smaller jump
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),  // Increased from 500 for slower animation
            repeatMode = RepeatMode.Reverse
        ),
        label = "bounceOffset"
    )
    
    Box(
        modifier = modifier
            .size(50.dp)  // Slightly smaller
            .graphicsLayer {
                translationY = bounceOffset
            }
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "🎁",
            fontSize = 32.sp
        )
    }
}

