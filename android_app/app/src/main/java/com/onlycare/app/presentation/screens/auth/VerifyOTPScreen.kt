package com.onlycare.app.presentation.screens.auth

import android.app.Activity
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.onlycare.app.BuildConfig
import com.onlycare.app.R
import com.onlycare.app.presentation.navigation.Screen
import com.onlycare.app.presentation.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun VerifyOTPScreen(
    navController: NavController,
    viewModel: VerifyOTPViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val phone = navController.currentBackStackEntry?.arguments?.getString("phone") ?: ""

    // Make status bar match the Primary header on this screen (white icons on blue)
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Primary.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = false
            }
        }
    }
    
    // Animation states
    val logoAlpha = remember { Animatable(0f) }
    val logoScale = remember { Animatable(0.7f) }
    val titleAlpha = remember { Animatable(0f) }
    val taglineAlpha = remember { Animatable(0f) }
    val formAlpha = remember { Animatable(0f) }
    val resendAlpha = remember { Animatable(0f) }
    
    LaunchedEffect(Unit) {
        // Sequential elegant animations
        launch {
            logoAlpha.animateTo(1f, tween(600, easing = FastOutSlowInEasing))
        }
        launch {
            logoScale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow))
        }
        delay(200)
        launch {
            titleAlpha.animateTo(1f, tween(500, easing = FastOutSlowInEasing))
        }
        delay(150)
        launch {
            taglineAlpha.animateTo(1f, tween(500, easing = FastOutSlowInEasing))
        }
        delay(200)
        launch {
            formAlpha.animateTo(1f, tween(600, easing = FastOutSlowInEasing))
        }
        delay(300)
        launch {
            resendAlpha.animateTo(1f, tween(500, easing = FastOutSlowInEasing))
        }
    }
    
    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) {
            if (state.isNewUser) {
                // New user - go to profile creation flow
                navController.navigate(Screen.SelectGender.route) {
                    popUpTo(Screen.Login.route) { inclusive = true }
                }
            } else {
                // Existing user - go directly to main screen
                navController.navigate(Screen.Main.route) {
                    popUpTo(Screen.Login.route) { inclusive = true }
                }
            }
        }
    }
    
    // Handle system back button explicitly
    androidx.activity.compose.BackHandler {
        navController.popBackStack()
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Primary)  // Full Primary behind (requested)
            .imePadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Centered header text block (in the blue area)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                OnlyCareAuthHero(
                    modifier = Modifier.fillMaxWidth(),
                    carouselAlpha = logoAlpha.value,
                    carouselScale = logoScale.value,
                    titleAlpha = titleAlpha.value,
                    subtitleAlpha = taglineAlpha.value,
                    title = "Welcome to Only Care",
                    subtitle = "Connect with people through voice and video calls"
                )
            }

            // Bottom form card (like sample)
            Card(
                modifier = Modifier.fillMaxWidth(),
                // Full-bleed card: rounded only on top, flush on bottom/edges
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                colors = CardDefaults.cardColors(containerColor = White)
            ) {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .navigationBarsPadding()
                        .padding(horizontal = 18.dp, vertical = 18.dp)
                ) {
                // Phone row + Change
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "OTP sent to",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = phone,
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Text(
                        text = "Change",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .padding(start = 12.dp)
                            .clickable {
                                navController.navigate(Screen.Login.createRoute(phone)) {
                                    popUpTo(Screen.Login.route) { inclusive = true }
                                    launchSingleTop = true
                                }
                            }
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                PremiumOTPInput(
                    value = state.otp,
                    onValueChange = viewModel::onOtpChange,
                    error = state.otpError,
                    onDone = viewModel::onVerify
                )

                if (BuildConfig.DEBUG && !state.debugOtp.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "OTP (testing): ${state.debugOtp}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Use this OTP to continue (SMS not received).",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                PremiumVerifyButton(
                    text = "Verify OTP",
                    onClick = viewModel::onVerify,
                    loading = state.isLoading,
                    enabled = state.otp.length == 6
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Didn't receive code?  ",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        letterSpacing = 0.2.sp,
                        modifier = Modifier.alpha(resendAlpha.value)
                    )
                    if (state.resendSecondsRemaining > 0) {
                        Text(
                            text = "Retry in (${state.resendSecondsRemaining})",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.3.sp,
                            modifier = Modifier.alpha(resendAlpha.value)
                        )
                    } else {
                    Text(
                        text = "Resend",
                        style = MaterialTheme.typography.bodySmall,
                        color = Primary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        modifier = Modifier
                            .alpha(resendAlpha.value)
                            .clickable { viewModel.onResendOtp() }
                    )
                    }
                }
                // end Row
            }
        }
        }

        // Back button at top (Moved to end to be on top of other elements)
        IconButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
                .statusBarsPadding()
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = White
            )
        }
    }
}

@Composable
private fun OnlyCareAuthHero(
    modifier: Modifier = Modifier,
    carouselAlpha: Float,
    carouselScale: Float,
    titleAlpha: Float,
    subtitleAlpha: Float,
    title: String,
    subtitle: String
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val drawables = remember {
            listOf(
                R.drawable.auth_splash_1,
                R.drawable.auth_splash_2,
                R.drawable.auth_splash_3
            )
        }

        AuthSplashCarousel(
            drawableIds = drawables,
            modifier = Modifier
                .size(120.dp)
                .alpha(carouselAlpha)
                .scale(carouselScale)
        )

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.ExtraBold,
            color = White,
            fontSize = 30.sp,
            letterSpacing = 0.2.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .alpha(titleAlpha)
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = White.copy(alpha = 0.92f),
            textAlign = TextAlign.Center,
            lineHeight = 20.sp,
            modifier = Modifier
                .fillMaxWidth()
                .alpha(subtitleAlpha)
                .padding(horizontal = 12.dp)
        )
    }
}

// ============================================
// PREMIUM COMPONENTS
// ============================================

@Composable
private fun PremiumOTPInput(
    value: String,
    onValueChange: (String) -> Unit,
    error: String?,
    onDone: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    
    Column {
        Text(
            text = "Enter 6-digit OTP",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                .padding(bottom = 14.dp)
                    )

                BasicTextField(
                    value = value,
            onValueChange = { raw ->
                val filtered = raw.filter { it.isDigit() }.take(6)
                onValueChange(filtered)
            },
                    modifier = Modifier
                        .fillMaxWidth()
                .focusRequester(focusRequester)
                .onFocusChanged { isFocused = it.isFocused }
                .clickable { focusRequester.requestFocus() },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                onDone = { if (value.length == 6) onDone() }
                    ),
                    singleLine = true,
                    decorationBox = { innerTextField ->
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        repeat(6) { index ->
                            val ch = value.getOrNull(index)?.toString().orEmpty()
                            val isActiveBox = isFocused && (index == value.length.coerceAtMost(5))
                            val borderColor = when {
                                error != null -> ErrorRed
                                isActiveBox -> Primary
                                else -> PrimaryLight
                            }
                            val borderWidth = if (isActiveBox) 2.dp else 1.dp
                            Box(
                                modifier = Modifier
                                    .size(width = 44.dp, height = 54.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(ThemeSurface)
                                    .border(borderWidth, borderColor, RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                            Text(
                                    text = ch,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = TextPrimary
                                )
                            }
                        }
                    }
                }

                // Keep actual input active but hidden
                Box(modifier = Modifier.size(0.dp)) { innerTextField() }
                    }
                )
        
        // Error message
        if (error != null) {
            Text(
                text = error,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFFF4444),
                modifier = Modifier.padding(start = 4.dp, top = 8.dp)
            )
        }
    }
}

@Composable
private fun PremiumVerifyButton(
    text: String,
    onClick: () -> Unit,
    loading: Boolean,
    enabled: Boolean
) {
    val scale = remember { Animatable(1f) }
    
    LaunchedEffect(enabled) {
        if (enabled) {
            // Subtle breathing animation
            scale.animateTo(
                targetValue = 1.02f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2000, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                )
            )
        }
    }
    
    // Verify button - flat design without shadow
    Button(
        onClick = onClick,
        enabled = enabled && !loading,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .scale(scale.value),
        colors = ButtonDefaults.buttonColors(
            containerColor = Primary,  // Royal Violet button
            contentColor = White,      // White text on violet
            disabledContainerColor = Border,  // Light gray when disabled
            disabledContentColor = TextTertiary  // Gray text when disabled
        ),
        shape = RoundedCornerShape(14.dp),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 0.dp,  // Flat design - no shadow
            pressedElevation = 2.dp
        )
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                color = White,  // White spinner on violet button
                strokeWidth = 2.5.dp
            )
        } else {
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp,
                fontSize = 17.sp,
                color = if (enabled) White else TextTertiary  // White or gray text
            )
        }
    }
}
