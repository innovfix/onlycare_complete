package com.onlycare.app.data.local

import android.content.Context
import android.content.SharedPreferences
import com.onlycare.app.domain.model.Gender
import com.onlycare.app.domain.model.Language
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManager @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREF_NAME,
        Context.MODE_PRIVATE
    )

    companion object {
        private const val PREF_NAME = "only_care_session"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_AUTH_TOKEN = "auth_token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_NAME = "name"
        private const val KEY_USERNAME = "username"
        private const val KEY_PHONE = "phone"
        private const val KEY_GENDER = "gender"
        private const val KEY_LANGUAGE = "language"
        private const val KEY_PROFILE_IMAGE = "profile_image"
        private const val KEY_BIO = "bio"
        private const val KEY_AGE = "age"
        private const val KEY_COIN_BALANCE = "coin_balance"
        private const val KEY_VOICE = "voice"
        private const val KEY_IS_VERIFIED = "is_verified"
        private const val KEY_VERIFIED_DATETIME = "verified_datetime"
        private const val KEY_VOICE_GENDER = "voice_gender"
        private const val KEY_AUDIO_STATUS = "audio_status"
        private const val KEY_VIDEO_STATUS = "video_status"
        private const val KEY_HAS_SET_AVAILABILITY = "has_set_availability"
        private const val KEY_REFERRAL_CODE = "referral_code"
    }

    // Check if user is logged in
    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    // Save user session after login/registration
    fun saveUserSession(
        userId: String,
        phone: String,
        gender: Gender,
        name: String = "",
        username: String = "",
        language: Language = Language.ENGLISH,
        profileImage: String = "",
        bio: String = "",
        age: Int = 0,
        coinBalance: Int = 0,
        voice: String = "",
        isVerified: Boolean = false,
        verifiedDatetime: String = "",
        voiceGender: String = "",
        audioStatus: Int = 0,
        videoStatus: Int = 0
    ) {
        prefs.edit().apply {
            putBoolean(KEY_IS_LOGGED_IN, true)
            putString(KEY_USER_ID, userId)
            putString(KEY_PHONE, phone)
            putString(KEY_GENDER, gender.name)
            putString(KEY_NAME, name)
            putString(KEY_USERNAME, username)
            putString(KEY_LANGUAGE, language.name)
            putString(KEY_PROFILE_IMAGE, profileImage)
            putString(KEY_BIO, bio)
            putInt(KEY_AGE, age)
            putInt(KEY_COIN_BALANCE, coinBalance)
            putString(KEY_VOICE, voice)
            putBoolean(KEY_IS_VERIFIED, isVerified)
            putString(KEY_VERIFIED_DATETIME, verifiedDatetime)
            putString(KEY_VOICE_GENDER, voiceGender)
            putInt(KEY_AUDIO_STATUS, audioStatus)
            putInt(KEY_VIDEO_STATUS, videoStatus)
            apply()
        }
    }

    // Update user profile
    fun updateUserProfile(
        name: String? = null,
        username: String? = null,
        age: Int? = null,
        bio: String? = null,
        profileImage: String? = null,
        gender: String? = null,
        language: String? = null,
        voice: String? = null,
        isVerified: Boolean? = null,
        verifiedDatetime: String? = null,
        voiceGender: String? = null,
        audioStatus: Int? = null,
        videoStatus: Int? = null
    ) {
        prefs.edit().apply {
            name?.let { putString(KEY_NAME, it) }
            username?.let { putString(KEY_USERNAME, it) }
            age?.let { putInt(KEY_AGE, it) }
            bio?.let { putString(KEY_BIO, it) }
            profileImage?.let { putString(KEY_PROFILE_IMAGE, it) }
            gender?.let { putString(KEY_GENDER, it) }
            language?.let { putString(KEY_LANGUAGE, it) }
            voice?.let { putString(KEY_VOICE, it) }
            isVerified?.let { putBoolean(KEY_IS_VERIFIED, it) }
            verifiedDatetime?.let { putString(KEY_VERIFIED_DATETIME, it) }
            voiceGender?.let { putString(KEY_VOICE_GENDER, it) }
            audioStatus?.let { putInt(KEY_AUDIO_STATUS, it) }
            videoStatus?.let { putInt(KEY_VIDEO_STATUS, it) }
            apply()
        }
    }

    // Get user data
    fun getUserId(): String {
        val userId = prefs.getString(KEY_USER_ID, "") ?: ""
        // Remove duplicate USR_ prefix if exists (e.g., USR_USR_xxx -> USR_xxx)
        return if (userId.startsWith("USR_USR_")) {
            userId.removePrefix("USR_")
        } else {
            userId
        }
    }
    fun getPhone(): String = prefs.getString(KEY_PHONE, "") ?: ""
    fun getName(): String = prefs.getString(KEY_NAME, "") ?: ""
    fun getUsername(): String = prefs.getString(KEY_USERNAME, "") ?: ""
    fun getAge(): Int = prefs.getInt(KEY_AGE, 0)
    fun getBio(): String = prefs.getString(KEY_BIO, "") ?: ""
    fun getProfileImage(): String = prefs.getString(KEY_PROFILE_IMAGE, "") ?: ""
    fun getCoinBalance(): Int = prefs.getInt(KEY_COIN_BALANCE, 0)
    fun getVoice(): String = prefs.getString(KEY_VOICE, "") ?: ""
    fun isVerified(): Boolean = prefs.getBoolean(KEY_IS_VERIFIED, false)
    fun getVerifiedDatetime(): String = prefs.getString(KEY_VERIFIED_DATETIME, "") ?: ""
    fun getVoiceGender(): String = prefs.getString(KEY_VOICE_GENDER, "") ?: ""
    fun getAudioStatus(): Int = prefs.getInt(KEY_AUDIO_STATUS, 0)
    fun getVideoStatus(): Int = prefs.getInt(KEY_VIDEO_STATUS, 0)
    fun hasSetAvailability(): Boolean = prefs.getBoolean(KEY_HAS_SET_AVAILABILITY, false)

    fun markAvailabilityAsSet() {
        prefs.edit().putBoolean(KEY_HAS_SET_AVAILABILITY, true).apply()
    }

    fun getGender(): Gender {
        val genderStr = prefs.getString(KEY_GENDER, Gender.MALE.name) ?: Gender.MALE.name
        return try {
            Gender.valueOf(genderStr)
        } catch (e: Exception) {
            Gender.MALE
        }
    }

    fun getLanguage(): Language {
        val langStr = prefs.getString(KEY_LANGUAGE, Language.ENGLISH.name) ?: Language.ENGLISH.name
        return try {
            Language.valueOf(langStr)
        } catch (e: Exception) {
            Language.ENGLISH
        }
    }

    // Update coin balance
    fun updateCoinBalance(balance: Int) {
        prefs.edit().putInt(KEY_COIN_BALANCE, balance).apply()
    }

    // Clear session on logout
    fun logout() {
        prefs.edit().clear().apply()
    }

    // Check if profile is complete
    fun isProfileComplete(): Boolean {
        return getName().isNotEmpty() && getAge() > 0
    }
    
    // Save auth token
    fun saveAuthToken(token: String) {
        prefs.edit().putString(KEY_AUTH_TOKEN, token).apply()
    }
    
    // Get auth token
    fun getAuthToken(): String? {
        return prefs.getString(KEY_AUTH_TOKEN, null)
    }
    
    // Clear auth token
    fun clearAuthToken() {
        prefs.edit().remove(KEY_AUTH_TOKEN).apply()
    }
    
    // Referral code management
    fun saveReferralCode(code: String) {
        prefs.edit().putString(KEY_REFERRAL_CODE, code.uppercase().trim()).apply()
    }
    
    fun getReferralCode(): String? {
        return prefs.getString(KEY_REFERRAL_CODE, null)?.takeIf { it.isNotBlank() }
    }
    
    fun clearReferralCode() {
        prefs.edit().remove(KEY_REFERRAL_CODE).apply()
    }
}


