package com.onlycare.app.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.onlycare.app.domain.model.User
import com.onlycare.app.presentation.theme.*
import com.onlycare.app.utils.getDisplayName

@Composable
fun UserCard(
    user: User,
    onUserClick: () -> Unit,
    onAudioCallClick: () -> Unit,
    onVideoCallClick: () -> Unit,
    modifier: Modifier = Modifier,
    showCallButtons: Boolean = true
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onUserClick),
        colors = CardDefaults.cardColors(
            containerColor = MediumGray
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Image
            ProfileImage(
                imageUrl = user.profileImage,
                size = 80.dp,
                showOnlineStatus = true,
                isOnline = user.isOnline
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Name
            Text(
                text = user.getDisplayName(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            // Age
            Text(
                text = "${user.age} years",
                style = MaterialTheme.typography.bodySmall,
                color = TextGray
            )
            
            // Rating
            if (user.rating > 0) {
                Spacer(modifier = Modifier.height(4.dp))
                RatingBar(
                    rating = user.rating,
                    size = 14.dp
                )
            }
            
            // Status
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (user.isOnline) "Online" else "Offline",
                style = MaterialTheme.typography.bodySmall,
                color = if (user.isOnline) OnlineGreen else TextGray,
                fontSize = 12.sp
            )
            
            // Call Buttons
            if (showCallButtons) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Call buttons show based on user's availability settings
                    // They are active even if user is offline, to match the female's set condition
                    if (user.audioCallEnabled) {
                        SmallIconButton(
                            icon = Icons.Default.Phone,
                            text = "Audio",
                            onClick = onAudioCallClick,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (user.videoCallEnabled) {
                        SmallIconButton(
                            icon = Icons.Default.Videocam,
                            text = "Video",
                            onClick = onVideoCallClick,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun UserListItem(
    user: User,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    trailing: @Composable (() -> Unit)? = null
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = DarkGray
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProfileImage(
                imageUrl = user.profileImage,
                size = 56.dp,
                showOnlineStatus = true,
                isOnline = user.isOnline
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = user.getDisplayName(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextGray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            
            if (trailing != null) {
                Spacer(modifier = Modifier.width(16.dp))
                trailing()
            }
        }
    }
}

@Composable
fun SmallIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = White,
    contentColor: Color = Black
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(40.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            contentColor = contentColor
        ),
        shape = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun CoinBalance(
    coins: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .clickable(onClick = onClick),
        color = MediumGray,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Circle,
                contentDescription = "Coins",
                tint = WarningOrange,  // Amber for coins
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = coins.toString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = White
            )
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add",
                tint = OnlineGreen,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun InterestChip(
    text: String,
    icon: ImageVector? = null,
    selected: Boolean = false,
    iconTint: Color? = null,
    iconBackgroundColor: Color? = null,
    iconBorderColor: Color? = null,
    selectedContainerColor: Color = PrimaryLight,
    unselectedContainerColor: Color = White,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(56.dp)
            .then(
                if (onClick != null) Modifier.clickable(onClick = onClick)
                else Modifier
            ),
        color = if (selected) selectedContainerColor else unselectedContainerColor,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) Primary else Border  // Purple border when selected
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (icon != null) {
                    val resolvedIconTint = iconTint ?: if (selected) Primary else TextSecondary
                    val resolvedIconBg = iconBackgroundColor
                        ?: if (selected) Primary.copy(alpha = 0.15f) else Surface
                    val resolvedIconBorder = iconBorderColor ?: if (selected) Primary else Border

                    // Premium icon with flat background
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                resolvedIconBg
                            )
                            .border(
                                width = 1.dp,
                                color = resolvedIconBorder,
                                shape = RoundedCornerShape(8.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = resolvedIconTint,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                }
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (selected) Primary else TextPrimary,  // Purple when selected, dark otherwise
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 15.sp,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }
    }
}

@Composable
fun SelectionChip(
    text: String,
    icon: ImageVector,
    selected: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        color = if (selected) White else CardBlack,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) White else BorderPrimary
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) Black else White,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = if (selected) Black else White,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                fontSize = 15.sp
            )
        }
    }
}

@Composable
fun LanguageChip(
    text: String,
    selected: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        color = if (selected) White else CardBlack,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) White else BorderPrimary
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = if (selected) Black else White,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                fontSize = 14.sp,
                maxLines = 1,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun CallButton(
    callType: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .size(72.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = CallGreen
        ),
        shape = CircleShape,
        contentPadding = PaddingValues(0.dp)
    ) {
        Icon(
            imageVector = if (callType == "audio") Icons.Default.Phone else Icons.Default.Videocam,
            contentDescription = callType,
            tint = White,
            modifier = Modifier.size(32.dp)
        )
    }
}

@Composable
fun EndCallButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .size(72.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = CallRed
        ),
        shape = CircleShape,
        contentPadding = PaddingValues(0.dp)
    ) {
        Icon(
            imageVector = Icons.Default.CallEnd,
            contentDescription = "End Call",
            tint = White,
            modifier = Modifier.size(32.dp)
        )
    }
}

