package com.onlycare.app.presentation.screens.call

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import android.widget.Toast
import com.onlycare.app.presentation.components.ProfileImage
import com.onlycare.app.presentation.navigation.Screen
import com.onlycare.app.presentation.theme.Background
import com.onlycare.app.presentation.theme.Border
import com.onlycare.app.presentation.theme.Primary
import com.onlycare.app.presentation.theme.TextPrimary
import com.onlycare.app.presentation.theme.TextSecondary
import com.onlycare.app.utils.getDisplayName

@Composable
fun RandomCallScreen(
    navController: NavController,
    callType: String,
    viewModel: RandomCallViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val appContext = LocalContext.current.applicationContext

    // Pull candidate ids from previous backstack entry (Main screen) savedStateHandle
    val candidateIds = remember {
        val ids = navController.previousBackStackEntry
            ?.savedStateHandle
            ?.get<ArrayList<String>>("random_candidates")
            ?.toList()
            .orEmpty()

        navController.previousBackStackEntry?.savedStateHandle?.remove<ArrayList<String>>("random_candidates")
        ids
    }

    LaunchedEffect(callType, candidateIds) {
        viewModel.startQueue(callType = callType, candidateUserIds = candidateIds)
    }

    // Navigate when accepted
    LaunchedEffect(state.connectInfo) {
        val info = state.connectInfo ?: return@LaunchedEffect
        val route = when (state.callType.lowercase()) {
            "video" -> Screen.VideoCall.createRoute(
                userId = info.receiverId,
                callId = info.callId,
                appId = info.appId,
                token = info.token,
                channel = info.channel,
                role = "caller",
                balanceTime = info.balanceTime
            )
            else -> Screen.AudioCall.createRoute(
                userId = info.receiverId,
                callId = info.callId,
                appId = info.appId,
                token = info.token,
                channel = info.channel,
                role = "caller",
                balanceTime = info.balanceTime
            )
        }
        navController.navigate(route) {
            popUpTo(Screen.RandomCall.route) { inclusive = true }
        }
    }

    fun exit() {
        viewModel.cancel()
        navController.navigateUp()
    }

    BackHandler { exit() }

    // If random call finishes with error (nobody answered / insufficient etc), show only toast and return Home.
    var errorHandled by remember { mutableStateOf(false) }
    LaunchedEffect(state.finished, state.error) {
        if (!errorHandled && state.finished && !state.error.isNullOrBlank()) {
            errorHandled = true
            Toast.makeText(appContext, state.error ?: "Call not connected. Try again.", Toast.LENGTH_SHORT).show()
            navController.navigate(Screen.Main.route) {
                popUpTo(Screen.Main.route) { inclusive = false }
                launchSingleTop = true
            }
        }
    }

    // Match CallConnectingScreen UI style for both Random Audio & Random Video (as requested)
    val infiniteTransition = rememberInfiniteTransition(label = "randomConnecting")
    val dotAlpha1 by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 900
                0.25f at 0
                1f at 300
                0.25f at 900
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "dot1"
    )
    val dotAlpha2 by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 900
                0.25f at 0
                0.25f at 300
                1f at 600
                0.25f at 900
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "dot2"
    )
    val dotAlpha3 by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 900
                0.25f at 0
                0.25f at 600
                1f at 900
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "dot3"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        if (state.error != null && state.finished) {
            // UI intentionally blank here; toast+navigation handled in LaunchedEffect above.
            Spacer(modifier = Modifier.height(1.dp))
            return
        }

        val title = if (callType.lowercase() == "video") "Video Session" else "Audio Session"
        val remoteName = state.user?.username?.takeIf { it.isNotBlank() }
            ?: (state.user?.getDisplayName()?.takeIf { it.isNotBlank() })
            ?: "User"

        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Connecting",
                style = MaterialTheme.typography.bodyLarge,
                color = TextSecondary
            )
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(Primary.copy(alpha = dotAlpha1))
                )
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(Primary.copy(alpha = dotAlpha2))
                )
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(Primary.copy(alpha = dotAlpha3))
                )
            }
        }

        Spacer(modifier = Modifier.height(56.dp))

        // Avatars + connecting line
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Remote avatar
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .border(3.dp, Primary.copy(alpha = 0.55f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                ProfileImage(
                    imageUrl = state.user?.profileImage,
                    size = 110.dp
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height(150.dp)
                    .background(Border.copy(alpha = 0.6f))
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Local avatar
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .border(3.dp, Primary.copy(alpha = 0.55f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                ProfileImage(
                    imageUrl = state.localProfileImage,
                    size = 110.dp
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "You",
                style = MaterialTheme.typography.labelLarge,
                color = Primary
            )
        }

        Spacer(modifier = Modifier.height(26.dp))

        Text(
            text = "Finding your perfect match...",
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Searching...",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.weight(1f))

        TextButton(onClick = { exit() }) {
            Text(
                text = "Cancel",
                color = TextSecondary,
                fontSize = 16.sp
            )
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

