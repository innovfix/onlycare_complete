package com.onlycare.app.presentation.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onlycare.app.data.local.SessionManager
import com.onlycare.app.data.repository.ApiDataRepository
import com.onlycare.app.domain.model.Gender
import com.onlycare.app.domain.model.Language
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.util.Log

data class SelectLanguageState(
    val isRegistering: Boolean = false,
    val registrationError: String? = null
)

@HiltViewModel
class SelectLanguageViewModel @Inject constructor(
    private val sessionManager: SessionManager,
    private val repository: ApiDataRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SelectLanguageState())
    val state: StateFlow<SelectLanguageState> = _state.asStateFlow()

    fun getGender() = sessionManager.getGender()
    
    fun saveLanguage(language: Language) {
        // Get existing session data and update with language
        val currentUserId = sessionManager.getUserId()
        val currentPhone = sessionManager.getPhone()
        val currentGender = sessionManager.getGender()
        val currentName = sessionManager.getName()
        val currentAge = sessionManager.getAge()
        val currentBio = sessionManager.getBio()
        val currentProfileImage = sessionManager.getProfileImage()
        val currentCoinBalance = sessionManager.getCoinBalance()
        
        // Save session with updated language
        sessionManager.saveUserSession(
            userId = currentUserId,
            phone = currentPhone,
            gender = currentGender ?: Gender.MALE,
            name = currentName,
            language = language,
            profileImage = currentProfileImage,
            bio = currentBio,
            age = currentAge,
            coinBalance = currentCoinBalance
        )
    }
    
    /**
     * Register male user after language selection
     * Male users only need: phone, gender, avatar, language
     */
    fun registerMaleUser(
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val gender = sessionManager.getGender()
        if (gender != Gender.MALE) {
            onFailure("Invalid gender for male registration")
            return
        }
        
        // Validate avatar is selected
        val avatar = sessionManager.getProfileImage()
        if (avatar.isBlank()) {
            android.util.Log.e("SelectLanguageVM", "❌ Avatar not selected! Cannot register.")
            onFailure("Please select an avatar first")
            return
        }
        
        _state.update { it.copy(isRegistering = true, registrationError = null) }
        
        viewModelScope.launch {
            val phone = sessionManager.getPhone()
            val language = sessionManager.getLanguage() ?: Language.ENGLISH
            
            android.util.Log.d("SelectLanguageVM", "========================================")
            android.util.Log.d("SelectLanguageVM", "📤 Registering Male User")
            android.util.Log.d("SelectLanguageVM", "  - Phone: $phone")
            android.util.Log.d("SelectLanguageVM", "  - Gender: $gender")
            android.util.Log.d("SelectLanguageVM", "  - Avatar ID: $avatar")
            android.util.Log.d("SelectLanguageVM", "  - Language: $language")
            android.util.Log.d("SelectLanguageVM", "========================================")
            
            // Get referral code from session manager if available
            val referralCode = sessionManager.getReferralCode()
            
            val result = repository.register(
                phone = phone,
                gender = gender,
                avatar = avatar,
                language = language,
                age = null, // Not required for male
                interests = null, // Not required for male
                description = null, // Not required for male
                referralCode = referralCode
            )
            
            result.onSuccess { response ->
                // Male→Male flow: apply referral immediately after registration (no KYC gating)
                if (!referralCode.isNullOrBlank()) {
                    repository.applyReferralCode(referralCode)
                        .onSuccess { applyResp ->
                            // Backend should credit referrer (10 coins). Referred bonus may be optional.
                            Log.d("SelectLanguageVM", "Referral applied: ${applyResp.message}")
                            applyResp.newBalance?.let { sessionManager.updateCoinBalance(it) }
                            sessionManager.clearReferralCode()
                        }
                        .onFailure { err ->
                            // Don't block registration if referral apply fails; keep code so it can be retried later.
                            Log.e("SelectLanguageVM", "Referral apply failed: ${err.message}")
                        }
                }

                // Update SessionManager with user data from registration response
                // This includes the real user ID and profile_image URL that backend generated from avatar ID
                response.user?.let { user ->
                    val currentPhone = sessionManager.getPhone()
                    val currentGender = sessionManager.getGender()
                    val currentLanguage = sessionManager.getLanguage() ?: Language.ENGLISH
                    
                    // Save complete session with REAL user ID (replaces temp ID)
                    sessionManager.saveUserSession(
                        userId = user.id, // Real user ID from backend
                        phone = user.phone ?: currentPhone,
                        gender = if (user.gender == "FEMALE") Gender.FEMALE else Gender.MALE,
                        name = user.name,
                        username = user.username ?: "",
                        language = currentLanguage,
                        profileImage = user.profileImage ?: "", // Backend returns URL here
                        bio = user.bio ?: "",
                        age = user.age ?: 0,
                        coinBalance = user.coinBalance ?: 0
                    )
                }
                _state.update { it.copy(isRegistering = false) }
                onSuccess()
            }.onFailure { error ->
                val errorMessage = error.message ?: "Registration failed"
                _state.update { 
                    it.copy(
                        isRegistering = false,
                        registrationError = errorMessage
                    ) 
                }
                onFailure(errorMessage)
            }
        }
    }
}


