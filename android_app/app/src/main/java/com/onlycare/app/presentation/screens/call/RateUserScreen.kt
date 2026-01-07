package com.onlycare.app.presentation.screens.call

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.navigation.NavController
import com.onlycare.app.presentation.components.*
import com.onlycare.app.presentation.navigation.Screen
import com.onlycare.app.presentation.theme.*
import com.onlycare.app.utils.getDisplayName

@Composable
fun RateUserScreen(
    navController: NavController,
    userId: String,
    callId: String,
    viewModel: RateUserViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var rating by remember { mutableStateOf(0) }
    var selectedTags by remember { mutableStateOf(setOf<String>()) }
    var additionalComments by remember { mutableStateOf("") }
    var blockThisUser by remember { mutableStateOf(false) }
    
    // Load user details
    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            viewModel.loadUser(userId)
        }
    }
    
    // Handle submit success
    LaunchedEffect(state.submitSuccess) {
        if (state.submitSuccess) {
            navController.navigate(Screen.Main.route) {
                popUpTo(Screen.Main.route) { inclusive = true }
            }
            viewModel.clearSubmitSuccess()
        }
    }
    
    // Handle system back button - make it behave like "Skip" button (navigate to home)
    BackHandler {
        navController.navigate(Screen.Main.route) {
            popUpTo(Screen.Main.route) { inclusive = true }
        }
    }
    
    val ratingGroup = remember(rating) {
        when (rating) {
            1, 2 -> 1
            3 -> 2
            4, 5 -> 3
            else -> 0
        }
    }

    val tagsForRating = remember(ratingGroup) {
        when (ratingGroup) {
            1 -> listOf("Not Replying", "Abusive language", "Rude Behaviour", "Bad Connectivity")
            2 -> listOf("Boring", "Disinterested", "Bad Conversation", "Lack of Enthusiasm")
            3 -> listOf("Fun Conversation", "Helpful Advice", "Friendly Conversation", "Pleasant Voice")
            else -> emptyList()
                }
    }

    LaunchedEffect(ratingGroup) {
        // Clear tags when the rating category changes (to avoid mixing options)
        selectedTags = emptySet()
                }

    // Reset block checkbox if user is not female (or state changes)
    LaunchedEffect(state.isFemaleUser) {
        if (!state.isFemaleUser) blockThisUser = false
    }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Background)
            .statusBarsPadding()
    ) {
        // Close button (acts like Skip)
        IconButton(
            onClick = {
                navController.navigate(Screen.Main.route) {
                    popUpTo(Screen.Main.route) { inclusive = true }
                }
            },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close",
                tint = TextSecondary
            )
        }

        when {
            state.isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Primary
                )
            }
            state.error != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = state.error ?: "Error loading user",
                        color = CallRed,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OnlyCarePrimaryButton(
                        text = "Go Home",
                        onClick = {
                            navController.navigate(Screen.Main.route) {
                                popUpTo(Screen.Main.route) { inclusive = true }
                            }
                        }
                    )
                }
            }
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp)
                        .padding(top = 28.dp, bottom = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val name = state.user?.getDisplayName() ?: "User"

                    Text(
                        text = "How was your session with",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Text(
                        text = name,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    Text(
                        text = "Your feedback helps us improve",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                    
                    Spacer(modifier = Modifier.height(22.dp))

                    OnlyCareSoftShadowContainer(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        shadowOffsetY = 4.dp,
                        shadowColor = Border.copy(alpha = 0.28f)
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = ThemeSurface)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Rate your experience",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                StarRatingBar(
                        rating = rating,
                                    onRatingChange = { rating = it },
                                    size = 44.dp
                                )

                                Spacer(modifier = Modifier.height(22.dp))

                                Text(
                                    text = "What did you like? (Optional)",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary,
                                    modifier = Modifier.align(Alignment.Start)
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                if (tagsForRating.isEmpty()) {
                                    Text(
                                        text = "Select a star rating to see options",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextTertiary,
                                        modifier = Modifier.align(Alignment.Start)
                                    )
                                } else {
                                    LazyVerticalGrid(
                                        columns = GridCells.Fixed(2),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .heightIn(min = 0.dp, max = 240.dp),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(12.dp),
                                        userScrollEnabled = false
                                    ) {
                                        items(tagsForRating) { tag ->
                                            FilterChip(
                                                text = tag,
                                                selected = selectedTags.contains(tag),
                                                onClick = {
                                                    selectedTags =
                                                        if (selectedTags.contains(tag)) selectedTags - tag
                                                        else selectedTags + tag
                                                }
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(18.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Additional Comments (Optional)",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "${additionalComments.length}/100",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextTertiary
                                    )
                                }
                    
                                Spacer(modifier = Modifier.height(10.dp))
                    
                    OutlinedTextField(
                                    value = additionalComments,
                                    onValueChange = { newValue ->
                                        if (newValue.length <= 100) {
                                            additionalComments = newValue
                                        }
                                    },
                        placeholder = { Text("Share your experience...", color = TextTertiary) },
                        modifier = Modifier.fillMaxWidth(),
                                    minLines = 3,
                        maxLines = 4,
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            cursorColor = Primary,
                            focusedBorderColor = Primary,
                                        unfocusedBorderColor = Border,
                                        focusedContainerColor = Background,
                                        unfocusedContainerColor = Background
                        ),
                                    shape = RoundedCornerShape(14.dp)
                    )
                    
                                // Show block checkbox only for female users (as per requirement)
                    if (state.isFemaleUser) {
                                    Spacer(modifier = Modifier.height(14.dp))
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable(enabled = !state.isSubmitting) {
                                                blockThisUser = !blockThisUser
                                            },
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Checkbox(
                                            checked = blockThisUser,
                                            onCheckedChange = { checked ->
                                                if (!state.isSubmitting) blockThisUser = checked
                                            },
                                            colors = CheckboxDefaults.colors(
                                                checkedColor = Primary,
                                                uncheckedColor = Border,
                                                checkmarkColor = White
                                            )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Block this user",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = TextPrimary
                                        )
                            }
                        }
                        
                                state.submitError?.let { error ->
                                    Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = error,
                                color = CallRed,
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.align(Alignment.Start)
                            )
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    OnlyCarePrimaryButton(
                        text = "Submit",
                        onClick = {
                            viewModel.submitRating(
                                userId = userId,
                                callId = callId,
                                rating = rating.toFloat(),
                                selectedTags = selectedTags.toList(),
                                additionalComment = additionalComments,
                                shouldBlockUser = blockThisUser
                            )
                        },
                        enabled = rating > 0 && !state.isSubmitting,
                        loading = state.isSubmitting
                    )
                }
            }
        }
    }
}

@Composable
private fun StarRatingBar(
    rating: Int,
    onRatingChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    maxRating: Int = 5,
    size: androidx.compose.ui.unit.Dp = 40.dp
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(maxRating) { index ->
            val starNumber = index + 1
            val filled = starNumber <= rating
            Icon(
                imageVector = if (filled) Icons.Filled.Star else Icons.Outlined.StarBorder,
                contentDescription = "Star $starNumber",
                tint = if (filled) androidx.compose.ui.graphics.Color(0xFFFFC107) else Border,
                modifier = Modifier
                    .size(size)
                    .clickable { onRatingChange(starNumber) }
            )
        }
    }
}

@Composable
private fun FilterChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 44.dp)
            .clickable(onClick = onClick),
        color = if (selected) Primary.copy(alpha = 0.10f) else Background,
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) Primary else Border
        ),
        shape = RoundedCornerShape(999.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = if (selected) PrimaryDark else TextPrimary,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
            )
        }
    }
}



