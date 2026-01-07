package com.onlycare.app.utils

import com.onlycare.app.domain.model.Gender
import com.onlycare.app.domain.model.Language
import com.onlycare.app.domain.model.User

/**
 * Utility functions for consistent user/creator name display across the app.
 * Ensures the same name is shown in male side, female side, admin, and all call screens.
 */
object UserDisplayUtils {
    
    /**
     * Tamil female names list - used for generating consistent display names
     * for Tamil female users based on their user ID hash.
     */
    private val tamilFemaleNames = listOf(
        "Aadhira",
        "Aishwarya",
        "Ananya",
        "Anitha",
        "Bhavani",
        "Deepika",
        "Dharani",
        "Divya",
        "Gayathri",
        "Geetha",
        "Harini",
        "Indhu",
        "Janani",
        "Kavya",
        "Keerthana",
        "Lakshmi",
        "Lalitha",
        "Madhumitha",
        "Mahalakshmi",
        "Meenakshi",
        "Nandhini",
        "Narmadha",
        "Nithya",
        "Pavithra",
        "Priyanka",
        "Ranjani",
        "Revathi",
        "Sangeetha",
        "Saranya",
        "Sharmila",
        "Shruthi",
        "Sindhu",
        "Sowmya",
        "Subhashini",
        "Sujatha",
        "Swathi",
        "Tamilselvi",
        "Thamarai",
        "Uma",
        "Vaani",
        "Varsha",
        "Vidhya",
        "Vijayalakshmi",
        "Yasodha",
        "Yazhini",
        "Yogalakshmi",
        "Thenmozhi",
        "Poornima",
        "Kalpana",
        "Mythili"
    )
    
    /**
     * Gets the display name for a user.
     * For Tamil female users, returns a consistent Tamil name based on user ID hash.
     * For all other users, returns their actual name.
     * 
     * This ensures the same name is displayed everywhere:
     * - Male home screen (creator list)
     * - Female profile screen
     * - Admin screens
     * - Call screens (incoming, audio, video)
     * - User profile screens
     * - All other places where user names are shown
     */
    fun getDisplayName(user: User): String {
        return if (user.gender == Gender.FEMALE && user.language == Language.TAMIL) {
            getTamilNameForUserId(user.id, user.name)
        } else {
            user.name
        }
    }
    
    /**
     * Gets the display name from user properties directly.
     * Useful when you only have user ID, gender, language, and name.
     */
    fun getDisplayName(
        userId: String,
        userName: String,
        gender: Gender,
        language: Language
    ): String {
        return if (gender == Gender.FEMALE && language == Language.TAMIL) {
            getTamilNameForUserId(userId, userName)
        } else {
            userName
        }
    }
    
    /**
     * Generates a consistent Tamil name for a given user ID.
     * The same user ID will always get the same Tamil name.
     */
    private fun getTamilNameForUserId(userId: String, fallbackName: String): String {
        if (tamilFemaleNames.isEmpty()) return fallbackName
        val idx = kotlin.math.abs(userId.hashCode()) % tamilFemaleNames.size
        return tamilFemaleNames[idx]
    }
}

/**
 * Extension function for User to get display name easily.
 */
fun User.getDisplayName(): String {
    return UserDisplayUtils.getDisplayName(this)
}
