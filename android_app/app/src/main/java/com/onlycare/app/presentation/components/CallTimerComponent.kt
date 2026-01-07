package com.onlycare.app.presentation.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.onlycare.app.presentation.theme.*
import com.onlycare.app.utils.TimeUtils

/**
 * Countdown timer component for call screen
 * Shows remaining time based on user's coin balance
 * 
 * @param remainingSeconds Remaining time in seconds
 * @param isLowTime Warning threshold flag (< 2 minutes)
 * @param modifier Optional modifier
 */
@Composable
fun CallCountdownTimer(
    remainingSeconds: Int,
    isLowTime: Boolean,
    modifier: Modifier = Modifier
) {
    // Format time display
    val timeText = TimeUtils.formatTime(remainingSeconds)
    
    // Color based on remaining time (flat colors)
    val timerColor = when {
        remainingSeconds <= 60 -> CallRed        // Last minute - critical
        remainingSeconds <= 120 -> WarningOrange // Last 2 minutes - warning
        else -> White  // Normal
    }
    
    // Pulsing animation for low time
    val infiniteTransition = rememberInfiniteTransition(label = "timer_pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isLowTime) 0.4f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )
    
    // Container
    Row(
        modifier = modifier
            .background(
                color = MediumGray.copy(alpha = 0.3f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .alpha(if (isLowTime) alpha else 1f),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        // Timer icon
        Icon(
            imageVector = Icons.Default.Timer,
            contentDescription = "Time remaining",
            tint = timerColor,
            modifier = Modifier.size(20.dp)
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        // Time text
        Text(
            text = timeText,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = timerColor
        )
        
        // Warning icon for low time
        if (isLowTime) {
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Low time warning",
                tint = Color.Red,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

/**
 * Compact countdown timer (for small spaces)
 */
@Composable
fun CompactCallCountdownTimer(
    remainingSeconds: Int,
    isLowTime: Boolean,
    modifier: Modifier = Modifier
) {
    val timeText = TimeUtils.formatTime(remainingSeconds)
    val timerColor = when {
        remainingSeconds <= 60 -> CallRed        // Last minute - critical
        remainingSeconds <= 120 -> WarningOrange // Last 2 minutes - warning
        else -> TextPrimary  // Normal (high-contrast for light surfaces like VideoCall top chip)
    }
    
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Timer,
            contentDescription = "Time",
            tint = timerColor,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = timeText,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = timerColor
        )
    }
}

/**
 * Timer with label (for detailed display)
 */
@Composable
fun LabeledCallCountdownTimer(
    remainingSeconds: Int,
    isLowTime: Boolean,
    label: String = "Time Remaining",
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CallCountdownTimer(
            remainingSeconds = remainingSeconds,
            isLowTime = isLowTime
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = White.copy(alpha = 0.7f)
        )
    }
}



