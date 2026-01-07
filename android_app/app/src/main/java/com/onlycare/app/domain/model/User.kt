package com.onlycare.app.domain.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

data class User(
    val id: String = "",
    val name: String = "",
    val username: String = "",
    val age: Int = 0,
    val gender: Gender = Gender.MALE,
    val phone: String = "",
    val profileImage: String = "",
    val bio: String = "",
    val language: Language = Language.ENGLISH,
    val interests: List<String> = emptyList(),
    val isOnline: Boolean = false,
    val lastSeen: Long = 0L,
    val rating: Float = 0f,
    val totalRatings: Int = 0,
    val coinBalance: Int = 0,
    val totalEarnings: Int = 0,
    val audioCallEnabled: Boolean = false,
    val videoCallEnabled: Boolean = false,
    val isVerified: Boolean = false
)

enum class Gender {
    MALE, FEMALE
}

enum class Language(val displayName: String) {
    ENGLISH("English"),
    HINDI("हिंदी"),
    TAMIL("தமிழ்"),
    TELUGU("తెలుగు"),
    KANNADA("ಕನ್ನಡ"),
    MALAYALAM("മലയാളം"),
    BENGALI("বাংলা"),
    MARATHI("मराठी")
}

enum class Interest(val displayName: String, val icon: ImageVector) {
    MUSIC("Music", Icons.Default.MusicNote),
    MOVIES("Movies", Icons.Default.Movie),
    SPORTS("Sports", Icons.Default.SportsSoccer),
    GAMING("Gaming", Icons.Default.SportsEsports),
    TRAVEL("Travel", Icons.Default.Flight),
    FOOD("Food", Icons.Default.Restaurant),
    PHOTOGRAPHY("Photography", Icons.Default.CameraAlt),
    ART("Art", Icons.Default.Palette),
    BOOKS("Books", Icons.Default.MenuBook),
    FITNESS("Fitness", Icons.Default.FitnessCenter),
    TECHNOLOGY("Technology", Icons.Default.Computer),
    FASHION("Fashion", Icons.Default.Checkroom)
}



