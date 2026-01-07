package com.onlycare.app.presentation.screens.auth

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.onlycare.app.domain.model.Gender
import com.onlycare.app.domain.model.Interest
import com.onlycare.app.presentation.components.OnlyCarePrimaryButton
import com.onlycare.app.presentation.components.InterestChip
import com.onlycare.app.presentation.navigation.Screen
import com.onlycare.app.presentation.theme.*

@Composable
fun SetupProfileScreen(
    navController: NavController,
    viewModel: SetupProfileViewModel = hiltViewModel()
) {
    val userGender = viewModel.getGender()
    var name by remember { mutableStateOf(if (userGender == Gender.FEMALE) viewModel.generateRandomName() else "") }
    var age by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var selectedInterests by remember { mutableStateOf<Set<Interest>>(emptySet()) }
    var showValidationErrors by remember { mutableStateOf(false) } // Track if Continue was clicked
    val interests = Interest.values().toList()
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    
    // Safety check: Male users should not be on this screen (they register in SelectLanguageScreen)
    LaunchedEffect(userGender) {
        if (userGender == Gender.MALE) {
            // Redirect male users to GrantPermissions (they should have already registered)
            navController.navigate(Screen.GrantPermissions.route) {
                popUpTo(Screen.Login.route) { inclusive = true }
            }
        }
    }
    
    // Get saved avatar ID from session (selected on gender screen)
    val savedAvatarId = viewModel.getSavedAvatar()
    val initialIndex = remember(state.avatars, savedAvatarId) {
        // Compare by ID since we now store avatar ID instead of URL
        state.avatars.indexOfFirst { it.id.toString() == savedAvatarId }.takeIf { it >= 0 } ?: 0
    }
    var selectedAvatarIndex by remember { mutableStateOf(initialIndex) }
    var showAvatarPicker by remember { mutableStateOf(false) }
    
    // Update selected index when avatars are loaded
    LaunchedEffect(state.avatars.size) {
        if (state.avatars.isNotEmpty() && savedAvatarId.isNotEmpty()) {
            // Compare by ID since we now store avatar ID instead of URL
            val index = state.avatars.indexOfFirst { it.id.toString() == savedAvatarId }
            if (index >= 0) {
                selectedAvatarIndex = index
            }
        }
    }
    
    val nameError = when {
        name.isNotEmpty() && name.length < 3 -> "Name must be at least 3 characters"
        else -> null
    }
    
    val ageError = when {
        age.isNotEmpty() && (age.toIntOrNull() ?: 0) < 18 -> "You must be 18 or older"
        age.isNotEmpty() && (age.toIntOrNull() ?: 0) > 99 -> "Invalid age"
        else -> null
    }
    
    // Bio validation - mandatory for female users (only show error after Continue clicked)
    val bioError = when {
        !showValidationErrors -> null // Don't show error initially
        userGender == Gender.FEMALE && bio.isEmpty() -> "Bio is required"
        userGender == Gender.FEMALE && bio.length < 10 -> "Bio must be at least 10 characters"
        else -> null
    }

    // Ensure input text is clearly visible on light fields
    val fieldTextColor = PrimaryDark

    fun interestAccentColor(interest: Interest): androidx.compose.ui.graphics.Color {
        return when (interest) {
            // Porter BLUE theme: avoid violet/indigo accents
            Interest.MUSIC -> Primary
            Interest.MOVIES -> PrimaryDark
            Interest.SPORTS -> androidx.compose.ui.graphics.Color(0xFF0EA5E9)       // sky
            Interest.GAMING -> androidx.compose.ui.graphics.Color(0xFFEC4899)       // pink
            Interest.TRAVEL -> androidx.compose.ui.graphics.Color(0xFF14B8A6)       // teal
            Interest.FOOD -> androidx.compose.ui.graphics.Color(0xFFF59E0B)         // amber
            Interest.PHOTOGRAPHY -> androidx.compose.ui.graphics.Color(0xFF22C55E)  // green
            Interest.ART -> PrimaryLight
            Interest.BOOKS -> androidx.compose.ui.graphics.Color(0xFF3B82F6)        // blue
            Interest.FITNESS -> androidx.compose.ui.graphics.Color(0xFF10B981)      // emerald
            Interest.TECHNOLOGY -> androidx.compose.ui.graphics.Color(0xFF06B6D4)   // cyan
            Interest.FASHION -> androidx.compose.ui.graphics.Color(0xFFF97316)      // orange
        }
    }
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            // Back button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.Start
            ) {
                IconButton(onClick = { navController.navigateUp() }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Primary
                    )
                }
            }
        }
        
        item {
            Spacer(modifier = Modifier.height(20.dp))
            
            Text(
                text = "Setup Your Profile",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            
            Spacer(modifier = Modifier.height(40.dp))
            
            // Profile Avatar - Shows selected avatar image
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clickable { showAvatarPicker = !showAvatarPicker },
                contentAlignment = Alignment.Center
            ) {
                // Avatar circle (clipped) - keeps image round
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clip(CircleShape)
                        .background(ThemeSurface)
                        .border(1.5.dp, PrimaryLight, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                if (state.isLoadingAvatars) {
                    CircularProgressIndicator(
                        color = Primary,
                        modifier = Modifier.size(40.dp)
                    )
                } else if (state.avatars.isNotEmpty() && selectedAvatarIndex < state.avatars.size) {
                    AsyncImage(
                        model = state.avatars[selectedAvatarIndex].imageUrl,
                        contentDescription = "Avatar",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Avatar",
                        tint = Primary,
                        modifier = Modifier.size(56.dp)
                    )
                }
                }

                // Edit badge OUTSIDE the clipped circle (so it won't get cut)
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .align(Alignment.BottomEnd)
                        .offset(x = 6.dp, y = 6.dp)
                        .clip(CircleShape)
                        .background(Primary)
                        .border(1.5.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Change Avatar",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Tap to change avatar",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
            
            // Avatar Picker (shows when clicked)
            if (showAvatarPicker) {
                Spacer(modifier = Modifier.height(16.dp))
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = ThemeSurface),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Choose Your Avatar",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        if (state.isLoadingAvatars) {
                            CircularProgressIndicator(color = Primary)
                        } else if (state.avatars.isEmpty()) {
                            Text(
                                text = "No avatars available",
                                color = TextSecondary,
                                fontSize = 14.sp
                            )
                        } else {
                            // Avatar Grid
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp)
                            ) {
                                itemsIndexed(state.avatars) { index, avatar ->
                                    AvatarItem(
                                        imageUrl = avatar.imageUrl,
                                        selected = selectedAvatarIndex == index,
                                        onClick = { 
                                            selectedAvatarIndex = index
                                            showAvatarPicker = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
        
        item {
            // Name Field - Hidden for female users (system generates random name)
            if (userGender != Gender.FEMALE) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name", color = TextSecondary) },
                    placeholder = { Text("Enter your name", color = TextTertiary) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    textStyle = androidx.compose.ui.text.TextStyle(color = fieldTextColor, shadow = null),
                    isError = nameError != null,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = fieldTextColor,
                        unfocusedTextColor = fieldTextColor,
                        cursorColor = Primary,
                        focusedBorderColor = Primary,
                        unfocusedBorderColor = PrimaryLight,
                        focusedContainerColor = ThemeSurface,
                        unfocusedContainerColor = ThemeSurface
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                if (nameError != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(nameError, color = ErrorRed, fontSize = 12.sp)
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
        
        item {
            // Age Field
            OutlinedTextField(
                value = age,
                onValueChange = { age = it.filter { char -> char.isDigit() }.take(2) },
                label = { Text("Age", color = TextSecondary) },
                placeholder = { Text("Enter your age", color = TextTertiary) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                textStyle = androidx.compose.ui.text.TextStyle(color = fieldTextColor, shadow = null),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = ageError != null,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = fieldTextColor,
                    unfocusedTextColor = fieldTextColor,
                    cursorColor = Primary,
                    focusedBorderColor = Primary,
                    unfocusedBorderColor = PrimaryLight,
                    focusedContainerColor = ThemeSurface,
                    unfocusedContainerColor = ThemeSurface
                ),
                shape = RoundedCornerShape(12.dp)
            )
            if (ageError != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(ageError, color = ErrorRed, fontSize = 12.sp)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        item {
            // Bio Field
            OutlinedTextField(
                value = bio,
                onValueChange = { bio = it.take(200) },
                label = {
                    Text(
                        if (userGender == Gender.FEMALE) "Bio *" else "Bio (Optional)",
                        color = TextSecondary
                    )
                },
                placeholder = { Text("Tell us about yourself", color = TextTertiary) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 4,
                textStyle = androidx.compose.ui.text.TextStyle(color = fieldTextColor, shadow = null),
                isError = bioError != null,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = fieldTextColor,
                    unfocusedTextColor = fieldTextColor,
                    cursorColor = Primary,
                    focusedBorderColor = Primary,
                    unfocusedBorderColor = PrimaryLight,
                    focusedContainerColor = ThemeSurface,
                    unfocusedContainerColor = ThemeSurface
                ),
                shape = RoundedCornerShape(12.dp)
            )
            if (bioError != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(bioError, color = ErrorRed, fontSize = 12.sp)
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
        
        item {
            // Interests Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Select Your Interests",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = " *",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = ErrorRed
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        item {
            // Interests Grid
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Wider pills (2 per row) like your sample image
                interests.chunked(2).forEach { rowInterests ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        rowInterests.forEach { interest ->
                            val accent = interestAccentColor(interest)
                            val iconBg = accent.copy(alpha = if (selectedInterests.contains(interest)) 0.22f else 0.12f)
                            InterestChip(
                                text = interest.displayName,
                                icon = interest.icon,
                                iconTint = accent,
                                iconBackgroundColor = iconBg,
                                iconBorderColor = accent.copy(alpha = 0.35f),
                                selectedContainerColor = White,
                                unselectedContainerColor = White,
                                selected = selectedInterests.contains(interest),
                                onClick = {
                                    selectedInterests = if (selectedInterests.contains(interest)) {
                                        selectedInterests - interest
                                    } else {
                                        selectedInterests + interest
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        // Add empty spaces for incomplete rows
                        repeat(2 - rowInterests.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
        
        item {
            // Continue Button
            OnlyCarePrimaryButton(
                text = "Continue",
                onClick = {
                    // Show validation errors
                    showValidationErrors = true
                    
                    // Validate interests
                    if (selectedInterests.isEmpty()) {
                        Toast.makeText(context, "Please select at least 1 interest", Toast.LENGTH_SHORT).show()
                        return@OnlyCarePrimaryButton
                    }
                    
                    // Validate bio for female users
                    if (userGender == Gender.FEMALE) {
                        if (bio.isEmpty()) {
                            Toast.makeText(context, "Bio is required", Toast.LENGTH_SHORT).show()
                            return@OnlyCarePrimaryButton
                        }
                        if (bio.length < 10) {
                            Toast.makeText(context, "Bio must be at least 10 characters", Toast.LENGTH_SHORT).show()
                            return@OnlyCarePrimaryButton
                        }
                    }
                    
                    // Validate other fields
                    if (name.length < 3) {
                        return@OnlyCarePrimaryButton
                    }
                    if (age.toIntOrNull() !in 18..99) {
                        return@OnlyCarePrimaryButton
                    }
                    if (state.avatars.isEmpty()) {
                        return@OnlyCarePrimaryButton
                    }
                    
                    // All validations passed - proceed
                    val selectedAvatar = if (state.avatars.isNotEmpty() && selectedAvatarIndex < state.avatars.size) {
                        state.avatars[selectedAvatarIndex].id.toString()  // Pass avatar ID instead of URL
                    } else {
                        ""
                    }
                    // Convert interests to list of strings
                    val interestsList = selectedInterests.map { it.name }
                    
                    // Only female users register here (male users registered in SelectLanguageScreen)
                    if (userGender == Gender.FEMALE) {
                        viewModel.saveProfile(
                            name = name,
                            age = age.toInt(),
                            bio = bio,
                            profileImage = selectedAvatar,
                            interests = interestsList,
                            onSuccess = {
                                // Navigate to VoiceIdentification for female users
                                navController.navigate(Screen.VoiceIdentification.route) {
                                    popUpTo(Screen.Login.route) { inclusive = false }
                                }
                            },
                            onFailure = { error ->
                                val msg = error.removePrefix("Registration failed:").trim().ifBlank {
                                    "Registration failed. Please try again."
                                }
                                Toast.makeText(
                                    context,
                                    msg,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        )
                    } else {
                        // Male users should not reach here (should have registered in SelectLanguageScreen)
                        // But if they do, just navigate
                        navController.navigate(Screen.GrantPermissions.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                },
                enabled = name.length >= 3 
                    && age.toIntOrNull() in 18..99 
                    && state.avatars.isNotEmpty()
                    && !state.isRegistering
            )
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun AvatarItem(
    imageUrl: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(64.dp)
            .clip(CircleShape)
            .background(if (selected) PrimaryLight.copy(alpha = 0.35f) else ThemeSurface)
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) Primary else PrimaryLight,
                shape = CircleShape
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = imageUrl,
            contentDescription = "Avatar",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    }
}
