package com.onlycare.app.presentation.screens.auth

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.onlycare.app.R
import com.onlycare.app.presentation.navigation.Screen
import com.onlycare.app.presentation.theme.Background
import com.onlycare.app.presentation.theme.Primary
import com.onlycare.app.presentation.theme.TextPrimary
import com.onlycare.app.presentation.theme.TextSecondary

@Composable
fun VerificationPendingScreen(
    navController: NavController,
    viewModel: VerificationPendingViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    // Block back navigation (block-all behavior)
    BackHandler(enabled = true) {
        // Do nothing
    }

    // Navigate forward once verified
    LaunchedEffect(state.isVerified) {
        if (state.isVerified) {
            // After admin verification, send user to Home.
            navController.navigate(Screen.Main.route) {
                popUpTo(Screen.VerificationPending.route) { inclusive = true }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background) // White background (matches app)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Top
    ) {
        val context = LocalContext.current
        Spacer(modifier = Modifier.height(72.dp))

        Text(
            text = "Almost done...",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.ExtraBold,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Our team will reach out to you\nwithin 24hrs via phone call.\nThis is to explain about safety\nprocedures on the platform.",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = TextSecondary,
            textAlign = TextAlign.Start,
            lineHeight = MaterialTheme.typography.titleLarge.lineHeight
        )

        Spacer(modifier = Modifier.weight(1f))

        // Requested: DotLottieAnimation from URL.
        // Implementation note: the provided URL is a standard Lottie JSON, so we load it via lottie-compose URL spec.
        val url = "https://lottie.host/2d2e8043-2514-42f3-ab6c-c74992133fe7/yk4BJBlC29.json"
        val composition by rememberLottieComposition(LottieCompositionSpec.Url(url))
        val progress by animateLottieCompositionAsState(
            composition = composition,
            isPlaying = true,
            iterations = LottieConstants.IterateForever,
            speed = 3f
        )

        // Gentle infinite rotation (matches your “rotate it” request)
        val infinite = rememberInfiniteTransition(label = "hourglassRotate")
        val rotation by infinite.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 3200, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "rotation"
        )

        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .size(170.dp),
            contentAlignment = Alignment.Center
        ) {
            if (composition == null) {
                CircularProgressIndicator(color = Primary)
            } else {
                LottieAnimation(
                    composition = composition,
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer { rotationZ = rotation }
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Bottom support email (clickable mailto)
        val email = "support@onlycare.in"
        val footer = buildAnnotatedString {
            append("For any queries please contact : ")
            pushStringAnnotation(tag = "email", annotation = email)
            pushStyle(SpanStyle(color = Primary, textDecoration = TextDecoration.Underline))
            append(email)
            pop()
            pop()
        }

        ClickableText(
            text = footer,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 18.dp),
            style = TextStyle(
                color = TextSecondary,
                fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                fontWeight = MaterialTheme.typography.bodyMedium.fontWeight
            ),
            maxLines = 1,
            overflow = TextOverflow.Clip,
            onClick = { offset ->
                footer.getStringAnnotations(tag = "email", start = offset, end = offset)
                    .firstOrNull()
                    ?.let {
                        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$email"))
                        context.startActivity(Intent.createChooser(intent, "Send email"))
                    }
            }
        )
    }
}


