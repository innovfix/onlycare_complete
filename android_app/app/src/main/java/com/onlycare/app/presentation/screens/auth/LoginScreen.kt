package com.onlycare.app.presentation.screens.auth

import android.app.Activity
import androidx.fragment.app.FragmentActivity
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import android.util.Log
import android.widget.Toast
import com.onlycare.app.BuildConfig
import com.onlycare.app.R
import com.onlycare.app.presentation.navigation.Screen
import com.onlycare.app.presentation.theme.*
import com.truecaller.android.sdk.oAuth.CodeVerifierUtil
import com.truecaller.android.sdk.oAuth.TcOAuthCallback
import com.truecaller.android.sdk.oAuth.TcOAuthData
import com.truecaller.android.sdk.oAuth.TcOAuthError
import com.truecaller.android.sdk.oAuth.TcSdk
import com.truecaller.android.sdk.oAuth.TcSdkOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.math.BigInteger
import java.security.SecureRandom

@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val prefillPhone = navController.currentBackStackEntry?.arguments?.getString("phone").orEmpty()
    val TAG = "LoginScreen"
    val context = LocalContext.current

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

    // Truecaller (auto-detect phone number if available)
    val activity = view.context as? FragmentActivity
    val truecallerClientId = stringResource(id = R.string.clientID)
    var truecallerAutoAttempted by remember { mutableStateOf(false) }
    var truecallerCodeVerifier by remember { mutableStateOf<String?>(null) }
    // If user starts manual OTP flow (focus/type/send), never start Truecaller (prevents banner leaking to Home)
    var suppressTruecallerAutoStart by remember { mutableStateOf(false) }
    var truecallerInitialized by remember { mutableStateOf(false) }
    val isTruecallerInstalled = remember(context) {
        runCatching {
            @Suppress("DEPRECATION")
            context.packageManager.getPackageInfo("com.truecaller", 0)
            true
        }.getOrDefault(false)
    }

    val sendOtp: () -> Unit = {
        suppressTruecallerAutoStart = true
        truecallerAutoAttempted = true
        viewModel.onSendOTP()
    }

    // Prefill phone when coming from OTP "Change"
    LaunchedEffect(prefillPhone) {
        if (prefillPhone.isNotBlank() && state.phone.isBlank()) {
            viewModel.onPhoneChange(prefillPhone)
        }
    }

    val tcOAuthCallback = remember {
        object : TcOAuthCallback {
            override fun onSuccess(tcOAuthData: TcOAuthData) {
                val code = tcOAuthData.authorizationCode
                val codeVerifier = truecallerCodeVerifier

                if (code.isNullOrBlank() || codeVerifier.isNullOrBlank()) {
                    Log.w(TAG, "Truecaller onSuccess but missing code/codeVerifier")
                    return
                }

                // Skip OTP: authenticate directly with backend using Truecaller OAuth code + verifier
                viewModel.loginWithTruecaller(code = code, codeVerifier = codeVerifier)
            }

            override fun onVerificationRequired(tcOAuthError: TcOAuthError?) {
                Log.w(TAG, "Truecaller onVerificationRequired: $tcOAuthError")
                // Fallback to manual phone + OTP (and stop further auto attempts for this session)
                suppressTruecallerAutoStart = true
                truecallerAutoAttempted = true
            }

            override fun onFailure(tcOAuthError: TcOAuthError) {
                Log.e(TAG, "Truecaller onFailure: $tcOAuthError")
                // Fallback to manual phone + OTP (and stop further auto attempts for this session)
                suppressTruecallerAutoStart = true
                truecallerAutoAttempted = true

                // Common when using Truecaller "test mode": number not whitelisted.
                val errorText = tcOAuthError.toString()
                if (errorText.contains("test mode", ignoreCase = true) ||
                    errorText.contains("40306")
                ) {
                    Toast.makeText(
                        context,
                        "Truecaller test-mode restriction on this number. Please use OTP or whitelist the number in Truecaller console.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    fun startTruecaller(act: FragmentActivity) {

        // Don’t attempt if Client ID is not configured yet
        if (truecallerClientId.isBlank() || truecallerClientId == "YOUR_TRUECALLER_CLIENT_ID") {
            return
        }

        // Generate PKCE parameters
        val stateRequested = BigInteger(130, SecureRandom()).toString(32)
        val codeVerifier = CodeVerifierUtil.generateRandomCodeVerifier()
        val codeChallenge = CodeVerifierUtil.getCodeChallenge(codeVerifier)

        truecallerCodeVerifier = codeVerifier

        // Configure OAuth parameters
        TcSdk.getInstance().setOAuthState(stateRequested)
        codeChallenge?.let { TcSdk.getInstance().setCodeChallenge(it) }
        TcSdk.getInstance().setOAuthScopes(arrayOf("openid", "phone"))

        // Start Truecaller flow
        try {
            Log.d(TAG, "Truecaller getAuthorizationCode() starting")
        TcSdk.getInstance().getAuthorizationCode(act)
        } catch (t: Throwable) {
            Log.e(TAG, "Truecaller getAuthorizationCode() failed", t)
        }
    }

    /**
     * Auto-attempt Truecaller only if user does NOT start manual OTP flow.
     *
     * CRITICAL: We do NOT initialize the Truecaller SDK until we actually want to start the flow.
     * Initializing early can cause the Truecaller consent banner to appear later (even after OTP login).
     */
    LaunchedEffect(activity, state.phone, suppressTruecallerAutoStart) {
        val act = activity ?: return@LaunchedEffect

        Log.d(
            TAG,
            "Truecaller auto-start check: phone='${state.phone}', suppress=$suppressTruecallerAutoStart, attempted=$truecallerAutoAttempted, initialized=$truecallerInitialized, clientIdBlank=${truecallerClientId.isBlank()}"
        )

        if (suppressTruecallerAutoStart) return@LaunchedEffect
        if (state.phone.isNotBlank()) return@LaunchedEffect
        if (truecallerAutoAttempted) return@LaunchedEffect

        // Give OTP users time to focus/type/tap Send OTP before starting Truecaller.
        delay(1100)

        // Re-check after delay
        Log.d(
            TAG,
            "Truecaller after delay: phone='${state.phone}', suppress=$suppressTruecallerAutoStart, attempted=$truecallerAutoAttempted"
        )
        if (suppressTruecallerAutoStart) return@LaunchedEffect
        if (state.phone.isNotBlank()) return@LaunchedEffect
        if (truecallerAutoAttempted) return@LaunchedEffect

        // Don’t attempt if Client ID is not configured yet
        if (truecallerClientId.isBlank() || truecallerClientId == "YOUR_TRUECALLER_CLIENT_ID") return@LaunchedEffect

        try {
            Log.d(TAG, "Truecaller auto-start: attempting init/usable check")
            if (!truecallerInitialized) {
            val tcSdkOptions = TcSdkOptions.Builder(act, tcOAuthCallback)
                    // Verify Truecaller users only via the SDK; non-TC users use OTP.
                    .sdkOptions(TcSdkOptions.OPTION_VERIFY_ONLY_TC_USERS)
                .build()
                // IMPORTANT: Initialize on main thread for device/OEM compatibility
                TcSdk.init(tcSdkOptions)
                truecallerInitialized = true
            }

            val usable = TcSdk.getInstance().isOAuthFlowUsable
            Log.d(TAG, "Truecaller isOAuthFlowUsable=$usable")
            if (usable) {
                truecallerAutoAttempted = true
                startTruecaller(act)
            } else {
                Log.d(TAG, "Truecaller not usable on this device (isOAuthFlowUsable=false)")
            }
        } catch (t: Throwable) {
            Log.e(TAG, "Truecaller init/auto-start failed", t)
            // Ignore: fallback to manual OTP
        }
    }

    // If user starts typing, suppress Truecaller for this session (manual OTP path)
    LaunchedEffect(state.phone) {
        if (state.phone.isNotBlank()) {
            suppressTruecallerAutoStart = true
            truecallerAutoAttempted = true
        }
    }
    
    // Animation states
    val logoAlpha = remember { Animatable(0f) }
    val logoScale = remember { Animatable(0.7f) }
    val titleAlpha = remember { Animatable(0f) }
    val taglineAlpha = remember { Animatable(0f) }
    val formAlpha = remember { Animatable(0f) }
    val termsAlpha = remember { Animatable(0f) }
    
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
            termsAlpha.animateTo(1f, tween(500, easing = FastOutSlowInEasing))
        }
    }
    
    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) {
            val otpId = state.otpId
            if (otpId != null) {
                // User is going manual OTP route - ensure any Truecaller consent UI is dismissed
                // Add small delay to let Truecaller consent finish if it was showing
                delay(100)
                
                navController.navigate(Screen.VerifyOTP.createRoute(state.phone, otpId)) {
                    // Remove Login from back stack to ensure Truecaller consent UI is cleaned up
                    popUpTo(Screen.Login.route) { inclusive = true }
                }
                viewModel.resetState()
            }
        }
    }

    // Navigate immediately after successful Truecaller login (skip OTP)
    LaunchedEffect(state.truecallerLoginSuccess) {
        if (state.truecallerLoginSuccess) {
            if (state.truecallerIsNewUser) {
                navController.navigate(Screen.SelectGender.route) {
                    popUpTo(Screen.Login.route) { inclusive = true }
                }
            } else {
                navController.navigate(Screen.Main.route) {
                    popUpTo(Screen.Login.route) { inclusive = true }
                }
            }
            viewModel.resetTruecallerState()
        }
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
                    // Premium Phone Input
                    PremiumPhoneInput(
                        value = state.phone,
                        onValueChange = viewModel::onPhoneChange,
                        error = state.phoneError,
                        onDone = sendOtp,
                        onFocused = {
                            suppressTruecallerAutoStart = true
                            truecallerAutoAttempted = true
                        }
                    )
                
                    Spacer(modifier = Modifier.height(20.dp))
                
                // Referral checkbox
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = state.hasReferral,
                        onCheckedChange = { viewModel.onToggleReferral(it) },
                        colors = CheckboxDefaults.colors(
                            checkedColor = Primary,
                            uncheckedColor = Border
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Have a referral code?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
                
                // Referral input + Apply button (shown when checkbox is checked)
                if (state.hasReferral) {
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = state.referralCode,
                                onValueChange = viewModel::onReferralCodeChange,
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                placeholder = { Text("Ex: MQJB2008", color = TextTertiary) },
                                label = { Text("Referral code") },
                                isError = state.referralError != null,
                                textStyle = MaterialTheme.typography.bodyLarge.copy(
                                    color = TextPrimary
                                ),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary,
                                    focusedBorderColor = if (state.referralError != null) ErrorRed else Primary,
                                    unfocusedBorderColor = if (state.referralError != null) ErrorRed else Border,
                                    focusedLabelColor = Primary,
                                    unfocusedLabelColor = TextSecondary
                                ),
                                shape = RoundedCornerShape(14.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                            )
                            
                            Spacer(modifier = Modifier.width(10.dp))
                            
                            Button(
                                onClick = viewModel::onApplyReferral,
                                enabled = state.referralCode.length == 8 && !state.isLoading,
                                modifier = Modifier.height(56.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (state.referralApplied) Primary.copy(alpha = 0.7f) else Primary,
                                    contentColor = White,
                                    disabledContainerColor = Border,
                                    disabledContentColor = TextTertiary
                                ),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Text(
                                    text = if (state.referralApplied) "APPLIED" else "APPLY",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }
                        
                        // Referral error message
                        if (!state.referralError.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = state.referralError!!,
                                color = ErrorRed,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Premium 3D Button
                Premium3DButton(
                    text = "Send OTP",
                    onClick = sendOtp,
                    loading = state.isLoading,
                    enabled = state.phone.length >= 10 &&
                        (!state.hasReferral || state.referralApplied) &&
                        state.termsAccepted
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Terms & Conditions checkbox (required) - shown below Send OTP button
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .alpha(termsAlpha.value)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = state.termsAccepted,
                            onCheckedChange = viewModel::onTermsAcceptedChange,
                            colors = CheckboxDefaults.colors(
                                checkedColor = Primary,
                                uncheckedColor = Border
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        val annotated = buildAnnotatedString {
                            append("I accept ")

                            pushStringAnnotation(tag = "TERMS", annotation = "terms")
                            withStyle(
                                SpanStyle(
                                    color = Primary,
                                    fontWeight = FontWeight.SemiBold,
                                    textDecoration = TextDecoration.Underline
                                )
                            ) {
                                append("terms & conditions")
                            }
                            pop()

                            append(", and ")

                            pushStringAnnotation(tag = "PRIVACY", annotation = "privacy")
                            withStyle(
                                SpanStyle(
                                    color = Primary,
                                    fontWeight = FontWeight.SemiBold,
                                    textDecoration = TextDecoration.Underline
                                )
                            ) {
                                append("community guidelines & moderation policy")
                            }
                            pop()

                            append(" of OnlyCare")
                        }

                        ClickableText(
                            text = annotated,
                            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary),
                            modifier = Modifier.weight(1f),
                            onClick = { offset ->
                                annotated.getStringAnnotations(tag = "TERMS", start = offset, end = offset)
                                    .firstOrNull()
                                    ?.let { navController.navigate(Screen.Terms.route) }

                                annotated.getStringAnnotations(tag = "PRIVACY", start = offset, end = offset)
                                    .firstOrNull()
                                    ?.let { navController.navigate(Screen.CommunityGuidelines.route) }
                            }
                        )
                    }

                    if (!state.termsAccepted) {
                        Text(
                            text = "Please accept Terms & Conditions to continue",
                            style = MaterialTheme.typography.bodySmall,
                            color = ErrorRed,
                            modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                        )
                    }
                }

                // If SMS delivery is not working, backend may still return OTP in response (dev/testing).
                if (BuildConfig.DEBUG && !state.debugOtp.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "OTP (testing): ${state.debugOtp}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Use this OTP to continue (SMS not received).",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

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
private fun PremiumPhoneInput(
    value: String,
    onValueChange: (String) -> Unit,
    error: String?,
    onDone: () -> Unit,
    onFocused: () -> Unit = {}
) {
    var isFocused by remember { mutableStateOf(false) }
    var selectedCountryCode by remember { mutableStateOf("+91") }
    var showCountryPicker by remember { mutableStateOf(false) }
    
    Column {
        // Label
        Text(
            text = "PHONE NUMBER",
            style = MaterialTheme.typography.labelMedium,
            color = TextSecondary,
            letterSpacing = 1.2.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )
        
        // Input container with 3D effect
        Box {
            // Subtle shadow layer (flat design)
            Box(
                modifier = Modifier
                    .offset(y = 2.dp)
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Border.copy(alpha = 0.3f))  // Subtle gray shadow
            )
            
            // Main input (Royal Violet theme)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(White)  // White input background
                    .border(
                        width = if (isFocused) 2.dp else 1.dp,
                        color = if (error != null) ErrorRed  // Red for errors
                               else if (isFocused) Primary   // Royal Violet when focused
                               else Border,  // Light gray when unfocused
                        shape = RoundedCornerShape(14.dp)
                    )
                    .padding(horizontal = 18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Country code selector
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { showCountryPicker = !showCountryPicker }
                        .padding(end = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = selectedCountryCode,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary  // Dark text for country code (+91)
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Select country",
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                // Vertical divider
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(32.dp)
                        .background(BorderSecondary)
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                // Phone icon
                Icon(
                    imageVector = Icons.Default.Phone,
                    contentDescription = null,
                    tint = TextSecondary,  // Always gray
                    modifier = Modifier.size(20.dp)
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                // Input field
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier
                        .weight(1f)
                        .onFocusChanged {
                            isFocused = it.isFocused
                            if (it.isFocused) onFocused()
                        },
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        color = TextPrimary,  // Dark text for phone number input
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 0.5.sp
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Phone,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { onDone() }
                    ),
                    singleLine = true,
                    decorationBox = { innerTextField ->
                        if (value.isEmpty()) {
                            Text(
                                text = "Enter your number",
                                style = MaterialTheme.typography.bodyLarge,
                                color = TextTertiary,
                                letterSpacing = 0.3.sp
                            )
                        }
                        innerTextField()
                    }
                )
            }
        }
        
        // Error message
        if (error != null) {
            Text(
                text = error,
                style = MaterialTheme.typography.bodySmall,
                color = ErrorRed,  // Use theme error color
                modifier = Modifier.padding(start = 4.dp, top = 8.dp)
            )
        }
    }
}

@Composable
private fun Premium3DButton(
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
    
    // Main button - flat design without shadow
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
