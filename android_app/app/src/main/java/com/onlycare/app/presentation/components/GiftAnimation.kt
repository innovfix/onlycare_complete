package com.onlycare.app.presentation.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.delay

/**
 * Enhanced gift animation component
 * Shows animated gift with scale, rotation, and movement effects
 */
@Composable
fun GiftAnimation(
    giftIcon: String?,
    message: String = "Gift Received",
    onAnimationComplete: () -> Unit = {}
) {
    var isAnimating by remember { mutableStateOf(false) }
    
    // Scale animation - starts small, grows big
    val scale by animateFloatAsState(
        targetValue = if (isAnimating) 1.5f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )
    
    // Rotation animation
    val rotation by animateFloatAsState(
        targetValue = if (isAnimating) 360f else 0f,
        animationSpec = tween(2000, easing = LinearEasing),
        label = "rotation"
    )
    
    // Y position animation - bounce effect
    val bounceY by animateFloatAsState(
        targetValue = if (isAnimating) -50f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "bounceY"
    )
    
    // Fade animation
    val animatedAlpha by animateFloatAsState(
        targetValue = if (isAnimating) 1f else 0f,
        animationSpec = tween(500),
        label = "alpha"
    )
    
    // When gift icon changes, start animation
    LaunchedEffect(giftIcon) {
        if (giftIcon != null && giftIcon.isNotEmpty()) {
            android.util.Log.d("GiftAnimation", "🎬 Starting gift animation for: $giftIcon")
            isAnimating = true
            
            // Wait for animation to complete (3 seconds)
            delay(3000)
            
            // Fade out
            delay(500)
            
            isAnimating = false
            android.util.Log.d("GiftAnimation", "✅ Gift animation completed")
            onAnimationComplete()
        }
    }
    
    if (giftIcon != null && giftIcon.isNotEmpty() && isAnimating) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .zIndex(1000f) // Ensure it's on top
                .graphicsLayer {
                    this.alpha = animatedAlpha
                },
            contentAlignment = Alignment.Center
        ) {
            // Animated gift image with effects
            Box(
                modifier = Modifier
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        rotationZ = rotation
                        translationY = bounceY
                    }
                    .size(120.dp)
                    .background(
                        Color.White.copy(alpha = 0.9f),
                        CircleShape
                    )
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = rememberAsyncImagePainter(giftIcon),
                    contentDescription = "Gift",
                    modifier = Modifier.size(100.dp)
                )
            }
            
            // Gift message text below
            Text(
                text = "🎁 $message 🎁",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier
                    .graphicsLayer {
                        translationY = bounceY + 100f
                        this.alpha = animatedAlpha
                    }
                    .padding(top = 140.dp),
                textAlign = TextAlign.Center
            )
        }
    }
}
