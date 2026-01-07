package com.onlycare.app.presentation.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.onlycare.app.presentation.theme.*

/**
 * Flat "white shadow" card effect (like in SelectGenderScreen):
 * a subtle, slightly-offset background layer behind the card to create depth
 * without using dark Material elevation.
 */
@Composable
fun OnlyCareSoftShadowContainer(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(18.dp),
    shadowOffsetY: Dp = 3.dp,
    shadowColor: Color = Border.copy(alpha = 0.30f),
    showTopShadow: Boolean = true,
    topShadowOffsetY: Dp = (-2).dp,
    topShadowColor: Color = shadowColor.copy(alpha = shadowColor.alpha * 0.75f),
    showSideShadows: Boolean = true,
    leftShadowOffsetX: Dp = (-2).dp,
    rightShadowOffsetX: Dp = 2.dp,
    sideShadowColor: Color = shadowColor.copy(alpha = shadowColor.alpha * 0.70f),
    content: @Composable BoxScope.() -> Unit
) {
    Box(modifier = modifier) {
        if (showTopShadow) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .offset(y = topShadowOffsetY)
                    .clip(shape)
                    .background(topShadowColor)
            )
        }
        if (showSideShadows) {
            // LEFT
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .offset(x = leftShadowOffsetX)
                    .clip(shape)
                    .background(sideShadowColor)
            )
            // RIGHT
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .offset(x = rightShadowOffsetX)
                    .clip(shape)
                    .background(sideShadowColor)
            )
        }
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(y = shadowOffsetY)
                .clip(shape)
                .background(shadowColor)
        )
        content()
    }
}

// =====================================================================================
// Premium Loading Skeletons (Shimmer)
// =====================================================================================

@Composable
fun OnlyCareShimmerBox(
    modifier: Modifier,
    shape: Shape = RoundedCornerShape(12.dp),
    baseColor: Color = ThemeSurface,
    highlightColor: Color = PrimaryLight.copy(alpha = 0.65f)
) {
    val transition = rememberInfiniteTransition(label = "onlycare_shimmer")
    val progress = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = MotionTokens.SHIMMER_MS, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "onlycare_shimmer_progress"
    )

    // Move the highlight diagonally across the box.
    val startX = -600f + (1200f * progress.value)
    val brush = Brush.linearGradient(
        colors = listOf(baseColor, highlightColor, baseColor),
        start = Offset(startX, 0f),
        end = Offset(startX + 600f, 600f)
    )

    Box(
        modifier = modifier
            .clip(shape)
            .background(brush)
            .border(1.dp, ThemeBorder.copy(alpha = 0.55f), shape)
    )
}

@Composable
fun OnlyCareSkeletonTextLine(
    modifier: Modifier = Modifier,
    widthFraction: Float = 1f,
    height: Dp = 12.dp,
    shape: Shape = RoundedCornerShape(10.dp)
) {
    OnlyCareShimmerBox(
        modifier = modifier
            .fillMaxWidth(widthFraction)
            .height(height),
        shape = shape
    )
}

@Composable
fun OnlyCareListItemSkeleton(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OnlyCareShimmerBox(
            modifier = Modifier.size(44.dp),
            shape = RoundedCornerShape(14.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            OnlyCareSkeletonTextLine(widthFraction = 0.72f, height = 14.dp)
            Spacer(modifier = Modifier.height(10.dp))
            OnlyCareSkeletonTextLine(widthFraction = 0.45f, height = 12.dp)
        }
        Spacer(modifier = Modifier.width(12.dp))
        OnlyCareShimmerBox(
            modifier = Modifier
                .width(54.dp)
                .height(18.dp),
            shape = RoundedCornerShape(999.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnlyCareTopAppBar(
    title: String,
    onBackClick: () -> Unit = {},
    showBackButton: Boolean = true,
    actions: @Composable RowScope.() -> Unit = {}
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = White,
                maxLines = 1
            )
        },
        navigationIcon = {
            if (showBackButton) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = White
                    )
                }
            }
        },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            // Porter-like: darker header bar
            containerColor = PrimaryDark,
            navigationIconContentColor = White,
            titleContentColor = White,
            actionIconContentColor = White
        )
    )
}

@Composable
fun OnlyCarePrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false
) {
    Button(
        onClick = onClick,
        enabled = enabled && !loading,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Primary,  // Royal violet button
            contentColor = White,  // White text
            disabledContainerColor = Border,
            disabledContentColor = TextTertiary
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 0.dp,  // Flat design - no shadow
            pressedElevation = 2.dp
        )
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = White,  // White loading on violet
                strokeWidth = 2.dp
            )
        } else {
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (enabled && !loading) White else TextTertiary  // White text on violet button
            )
        }
    }
}

@Composable
fun OnlyCareSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.Transparent,
            contentColor = White,
            disabledContainerColor = Color.Transparent,
            disabledContentColor = TextGray
        ),
        border = ButtonDefaults.outlinedButtonBorder.copy(
            width = 1.5.dp,
            brush = androidx.compose.ui.graphics.SolidColor(White)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun OnlyCareTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    onTrailingIconClick: () -> Unit = {},
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    onImeAction: () -> Unit = {},
    isPassword: Boolean = false,
    error: String? = null,
    maxLines: Int = 1,
    enabled: Boolean = true
) {
    var passwordVisible by remember { mutableStateOf(false) }
    
    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label, color = TextSecondary) },
            placeholder = { Text(placeholder, color = TextTertiary) },
            leadingIcon = leadingIcon?.let {
                { Icon(it, contentDescription = null, tint = TextSecondary) }
            },
            trailingIcon = {
                when {
                    isPassword -> {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "Toggle password visibility",
                                tint = TextSecondary
                            )
                        }
                    }
                    trailingIcon != null -> {
                        IconButton(onClick = onTrailingIconClick) {
                            Icon(trailingIcon, contentDescription = null, tint = TextSecondary)
                        }
                    }
                }
            },
            visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType,
                imeAction = imeAction
            ),
            keyboardActions = KeyboardActions(
                onAny = { onImeAction() }
            ),
            isError = error != null,
            enabled = enabled,
            maxLines = maxLines,
            modifier = Modifier.fillMaxWidth(),
            textStyle = LocalTextStyle.current.copy(
                color = PrimaryDark,
                shadow = null
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = PrimaryDark,
                unfocusedTextColor = PrimaryDark,
                disabledTextColor = TextTertiary,
                errorTextColor = PrimaryDark,
                focusedContainerColor = Surface,
                unfocusedContainerColor = Surface,
                disabledContainerColor = Surface.copy(alpha = 0.6f),
                errorContainerColor = Surface,
                cursorColor = Primary,
                errorCursorColor = ErrorRed,
                focusedBorderColor = Primary,
                unfocusedBorderColor = PrimaryLight,
                disabledBorderColor = PrimaryLight.copy(alpha = 0.6f),
                errorBorderColor = ErrorRed,
                focusedLeadingIconColor = Primary,
                unfocusedLeadingIconColor = TextSecondary,
                focusedTrailingIconColor = Primary,
                unfocusedTrailingIconColor = TextSecondary,
                focusedLabelColor = Primary,
                unfocusedLabelColor = TextSecondary,
                errorLabelColor = ErrorRed
            ),
            shape = RoundedCornerShape(12.dp)
        )
        
        if (error != null) {
            Text(
                text = error,
                color = ErrorRed,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
    }
}

@Composable
fun ProfileImage(
    imageUrl: String?,
    size: Dp = 80.dp,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    showOnlineStatus: Boolean = false,
    isOnline: Boolean = false
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(MediumGray)
            .then(
                if (onClick != null) Modifier.clickable(onClick = onClick)
                else Modifier
            )
    ) {
        if (imageUrl.isNullOrEmpty()) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Profile",
                tint = TextGray,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(size * 0.25f)
            )
        } else {
            AsyncImage(
                model = imageUrl,
                contentDescription = "Profile",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
        
        if (showOnlineStatus) {
            Box(
                modifier = Modifier
                    .size(size * 0.2f)
                    .align(Alignment.BottomEnd)
                    .offset(x = (-2).dp, y = (-2).dp)
                    .clip(CircleShape)
                    .background(if (isOnline) OnlineGreen else TextGray)
                    .border(2.dp, Black, CircleShape)
            )
        }
    }
}

@Composable
fun LoadingIndicator(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = White,
            strokeWidth = 3.dp
        )
    }
}

@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    actionText: String? = null,
    onActionClick: () -> Unit = {}
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = TextGray,
            modifier = Modifier.size(64.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = White,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = TextGray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
        
        if (actionText != null) {
            Spacer(modifier = Modifier.height(24.dp))
            OnlyCarePrimaryButton(
                text = actionText,
                onClick = onActionClick,
                modifier = Modifier
                    .padding(horizontal = 32.dp)
                    .widthIn(max = 200.dp)
            )
        }
    }
}

@Composable
fun ConfirmationDialog(
    show: Boolean,
    onDismiss: () -> Unit,
    title: String,
    message: String,
    confirmText: String = "Confirm",
    dismissText: String = "Cancel",
    onConfirm: () -> Unit
) {
    if (show) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(onClick = onConfirm) {
                    Text(confirmText, color = White)
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text(dismissText, color = TextGray)
                }
            },
            containerColor = MediumGray,
            textContentColor = White,
            titleContentColor = White
        )
    }
}

@Composable
fun EndCallConfirmationDialog(
    show: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    if (show) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(
                    text = "End Call?",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = White
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to end this call?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextGray
                )
            },
            confirmButton = {
                TextButton(
                    onClick = onConfirm,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = CallRed
                    )
                ) {
                    Text(
                        text = "Yes",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = onDismiss,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = TextGray
                    )
                ) {
                    Text(
                        text = "Cancel",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            },
            containerColor = MediumGray,
            textContentColor = White,
            titleContentColor = White,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
fun RatingBar(
    rating: Float,
    modifier: Modifier = Modifier,
    maxRating: Int = 5,
    size: Dp = 16.dp,
    onRatingChange: ((Float) -> Unit)? = null
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        repeat(maxRating) { index ->
            val filled = index < rating.toInt()
            
            Icon(
                imageVector = Icons.Default.Circle,
                contentDescription = "Star ${index + 1}",
                tint = if (filled) Color(0xFFFFC107) else TextGray,
                modifier = Modifier
                    .size(size)
                    .then(
                        if (onRatingChange != null) {
                            Modifier.clickable { onRatingChange((index + 1).toFloat()) }
                        } else Modifier
                    )
            )
        }
    }
}

