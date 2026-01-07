package com.onlycare.app.presentation.screens.auth

import android.Manifest
import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.onlycare.app.presentation.components.OnlyCarePrimaryButton
import com.onlycare.app.presentation.components.OnlyCareSecondaryButton
import com.onlycare.app.presentation.navigation.Screen
import com.onlycare.app.presentation.theme.*

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun VoiceIdentificationScreen(
    navController: NavController,
    viewModel: VoiceIdentificationViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val tamilPrompt = remember { viewModel.getTamilVoicePromptOrNull() }
    
    // Request RECORD_AUDIO permission
    val audioPermissionState = rememberPermissionState(
        permission = Manifest.permission.RECORD_AUDIO
    )
    
    // Show error messages as Toast
    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let { error ->
            Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)  // Flat white background - Royal Violet theme
    ) {
        // Back Button
        IconButton(
            onClick = { navController.navigateUp() },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = TextPrimary
            )
        }
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(60.dp))
        
            Text(
                text = "Voice Identification",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Record a voice sample for verification.\nThis helps us ensure authenticity and security.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Microphone Icon with Animation
            MicrophoneIndicator(
                isRecording = state.isRecording,
                recordingComplete = state.recordingComplete,
                isUploading = state.isUploading
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Status Text
            when {
                state.isUploading -> {
                    CircularProgressIndicator(
                        color = Primary,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Uploading voice...",
                        style = MaterialTheme.typography.titleMedium,
                        color = Primary,
                        fontWeight = FontWeight.Bold
                    )
                }
                state.recordingComplete -> {
                    Text(
                        text = "✓ Recording complete!",
                        style = MaterialTheme.typography.titleMedium,
                        color = OnlineGreen,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Duration: ${state.recordingDuration / 1000}s",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
                state.isRecording -> {
                    Text(
                        text = "Recording...",
                        style = MaterialTheme.typography.titleMedium,
                        color = ErrorRed,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    // Recording Timer
                    Text(
                        text = formatRecordingTime(state.currentRecordingTime),
                        style = MaterialTheme.typography.headlineMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Speak clearly for at least 5 seconds",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
                else -> {
                    Text(
                        text = "Touch and hold to record",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Hold for at least 5 seconds",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary.copy(alpha = 0.7f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Tamil sentence prompt (show immediately on screen for Tamil users)
            if (tamilPrompt != null) {
                Text(
                    text = tamilPrompt,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = if (state.isRecording) FontWeight.Bold else FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Action Buttons
            if (!state.recordingComplete && !state.isUploading) {
                // Press & hold to record. Under 5s will be rejected by ViewModel.
                HoldToRecordButton(
                    enabled = !state.isUploading,
                    isHolding = state.isRecording,
                    onHoldStart = {
                        // Check and request permission
                        if (audioPermissionState.status.isGranted) {
                            viewModel.startRecording()
                        } else {
                            audioPermissionState.launchPermissionRequest()
                        }
                    },
                    onHoldEnd = {
                        if (state.isRecording) {
                            viewModel.stopRecording()
                        }
                    }
                )
            } else if (state.recordingComplete && !state.isUploading) {
                OnlyCarePrimaryButton(
                    text = "Submit & Continue",
                    onClick = {
                        viewModel.uploadVoice(
                            onSuccess = {
                                Toast.makeText(
                                    context,
                                    "Voice uploaded successfully!",
                                    Toast.LENGTH_SHORT
                                ).show()
                                navController.navigate(Screen.VerificationPending.route) {
                                    popUpTo(Screen.Login.route) { inclusive = false }
                                }
                            },
                            onFailure = { error ->
                                Toast.makeText(
                                    context,
                                    "Upload failed: $error",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        )
                    }
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OnlyCareSecondaryButton(
                    text = "Record Again",
                    onClick = {
                        viewModel.resetRecording()
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// Helper function to format recording time as MM:SS
@Composable
private fun HoldToRecordButton(
    enabled: Boolean,
    isHolding: Boolean,
    onHoldStart: () -> Unit,
    onHoldEnd: () -> Unit
) {
    val bg = if (enabled) Primary else Border
    val label = if (isHolding) "Recording…" else "Hold to Record"

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .pointerInput(enabled) {
                if (!enabled) return@pointerInput
                detectTapGestures(
                    onPress = {
                        onHoldStart()
                        try {
                            // Wait until user releases
                            tryAwaitRelease()
                        } finally {
                            onHoldEnd()
                        }
                    }
                )
            },
        shape = RoundedCornerShape(16.dp),
        color = bg,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = label,
                color = if (enabled) White else TextTertiary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private fun formatRecordingTime(milliseconds: Long): String {
    val totalSeconds = milliseconds / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}

@Composable
private fun MicrophoneIndicator(
    isRecording: Boolean,
    recordingComplete: Boolean,
    isUploading: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition(label = "mic_pulse")
    
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale_animation"
    )
    
    Box(
        modifier = Modifier.size(140.dp),
        contentAlignment = Alignment.Center
    ) {
        // Animated background circle for recording
        if (isRecording) {
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .scale(scale)
                    .background(
                        color = ErrorRed.copy(alpha = 0.1f),  // Flat subtle red background
                        shape = CircleShape
                    )
            )
        }
        
        // Icon container
        Surface(
            modifier = Modifier.size(100.dp),
            shape = CircleShape,
            color = when {
                isUploading -> AccentWhite.copy(alpha = 0.2f)
                isRecording -> ErrorRed.copy(alpha = 0.2f)
                recordingComplete -> OnlineGreen.copy(alpha = 0.2f)
                else -> White.copy(alpha = 0.1f)
            }
        ) {
            Box(
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Microphone",
                    modifier = Modifier.size(50.dp),
                    tint = when {
                        isUploading -> AccentWhite
                        isRecording -> ErrorRed
                        recordingComplete -> OnlineGreen
                        else -> White
                    }
                )
            }
        }
    }
}
