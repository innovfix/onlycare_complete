package com.onlycare.app.presentation.screens.auth

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.onlycare.app.data.local.SessionManager
import com.onlycare.app.domain.model.Gender
import com.onlycare.app.presentation.navigation.Screen
import com.onlycare.app.presentation.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(
    navController: NavController,
    sessionManager: SessionManager = hiltViewModel<SplashViewModel>().sessionManager
) {
    // Premium fade-in and scale animations
    val alpha = remember { Animatable(0f) }
    val scale = remember { Animatable(0.85f) }
    // Continuous glow reveal
    val glowProgress by rememberInfiniteTransition(label = "splashGlow").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "glowProgress"
    )
    
    LaunchedEffect(Unit) {
        // Minimal premium animations
        launch {
            alpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
            )
        }
        launch {
            scale.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = 600,
                    easing = FastOutSlowInEasing
                )
            )
        }
        
        delay(1800) // 1.8 seconds - minimal, premium timing
        
        // Check if user is already logged in
        if (sessionManager.isLoggedIn()) {
            // If user has a real userId (not temp), they've completed registration
            // Go to Main screen regardless of profile completeness check
            if (!sessionManager.getUserId().startsWith("temp_")) {
                val isFemale = sessionManager.getGender() == Gender.FEMALE
                val voiceSubmitted = sessionManager.getVoice().isNotEmpty()
                val isVerified = sessionManager.isVerified()

                if (isFemale && voiceSubmitted && !isVerified) {
                    // Female completed voice submission but is not yet verified by admin
                    navController.navigate(Screen.VerificationPending.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                } else {
                // Existing user - go to Main screen
                navController.navigate(Screen.Main.route) {
                    popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            } else {
                // Temp user - still in registration flow, go to gender selection
                navController.navigate(Screen.SelectGender.route) {
                    popUpTo(Screen.Splash.route) { inclusive = true }
                }
            }
        } else {
            navController.navigate(Screen.Login.route) {
                popUpTo(Screen.Splash.route) { inclusive = true }
            }
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background),  // Flat white background - Royal Violet theme
        contentAlignment = Alignment.Center
    ) {
        // ONLY CARE - Premium Text Stacked Vertically
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .alpha(alpha.value)
                .scale(scale.value)
        ) {
            // ONLY - Top (Royal Violet theme)
            Box {
                // Base dim text
                Text(
                    text = "ONLY",
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextTertiary,  // Light gray for base text
                    fontSize = 52.sp,
                    letterSpacing = 12.sp,
                    textAlign = TextAlign.Center
                )
                
                // Glowing text with Royal Violet
                Text(
                    text = "ONLY",
                    modifier = Modifier
                        .clipToBounds()
                        .drawWithContent {
                            clipRect(
                                left = 0f,
                                top = 0f,
                                right = size.width * glowProgress,
                                bottom = size.height
                            ) {
                                this@drawWithContent.drawContent()
                            }
                        },
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = Primary,  // Royal Violet glow
                    fontSize = 52.sp,
                    letterSpacing = 12.sp,
                    textAlign = TextAlign.Center
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // CARE - Bottom (Royal Violet theme)
            Box {
                // Base dim text
                Text(
                    text = "CARE",
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextTertiary,  // Light gray for base text
                    fontSize = 52.sp,
                    letterSpacing = 12.sp,
                    textAlign = TextAlign.Center
                )
                
                // Glowing text with Royal Violet
                Text(
                    text = "CARE",
                    modifier = Modifier
                        .clipToBounds()
                        .drawWithContent {
                            clipRect(
                                left = 0f,
                                top = 0f,
                                right = size.width * glowProgress,
                                bottom = size.height
                            ) {
                                this@drawWithContent.drawContent()
                            }
                        },
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = Primary,  // Royal Violet glow
                    fontSize = 52.sp,
                    letterSpacing = 12.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

