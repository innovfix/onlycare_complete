package com.onlycare.app.presentation.screens.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onlycare.app.data.local.SessionManager
import com.onlycare.app.data.repository.ApiDataRepository
import com.onlycare.app.domain.model.Gender
import com.onlycare.app.domain.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import android.util.Log
import javax.inject.Inject

data class MaleHomeState(
    val femaleUsers: List<User> = emptyList(),
    val femaleAvatarUrls: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val coinBalance: Int = 0
)

@HiltViewModel
class MaleHomeViewModel @Inject constructor(
    private val repository: ApiDataRepository,
    private val sessionManager: SessionManager
) : ViewModel() {
    
    private val _state = MutableStateFlow(MaleHomeState())
    val state: StateFlow<MaleHomeState> = _state.asStateFlow()
    
    private var refreshJob: Job? = null
    
    init {
        loadFemaleUsers()
        loadFemaleAvatarFallbacks()
        loadCoinBalance()
        startAutoRefresh()
    }

    private fun startAutoRefresh() {
        refreshJob?.cancel()
        refreshJob = viewModelScope.launch {
            while (true) {
                delay(30_000) // Refresh every 30 seconds
                Log.d("MaleHomeVM", "⏰ Auto-refreshing female users list...")
                loadFemaleUsers(isRefresh = true)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        refreshJob?.cancel()
    }

    private fun loadFemaleAvatarFallbacks() {
        viewModelScope.launch {
            // Fetch female avatar gallery once; use as fallback images in MaleHome cards.
            repository.getAvatars(Gender.FEMALE).onSuccess { avatars ->
                val urls = avatars.map { it.imageUrl }.filter { it.isNotBlank() }
                if (urls.isNotEmpty()) {
                    _state.update { it.copy(femaleAvatarUrls = urls) }
                }
            }.onFailure {
                // Silently ignore; cards will just show placeholder if no avatar gallery is available.
            }
        }
    }
    
    fun getCoinBalance(): Int {
        return _state.value.coinBalance
    }
    
    private fun loadCoinBalance() {
        viewModelScope.launch {
            repository.getWalletBalance().onSuccess { balance ->
                _state.update { it.copy(coinBalance = balance) }
                // Also update SessionManager for backward compatibility
                sessionManager.updateCoinBalance(balance)
            }.onFailure {
                // On failure, fallback to cached value
                val cachedBalance = sessionManager.getCoinBalance()
                _state.update { it.copy(coinBalance = cachedBalance) }
            }
        }
    }
    
    fun loadFemaleUsers(isRefresh: Boolean = false) {
        _state.update { 
            if (isRefresh) it.copy(isRefreshing = true, error = null)
            else it.copy(isLoading = true, error = null)
        }
        
        viewModelScope.launch {
            repository.getFemaleUsers().collect { result ->
                result.onSuccess { users ->
                    // ✅ DEBUG LOGGING for condition 1, 3, 4, 5
                    Log.d("MaleHomeVM", "📥 Loaded ${users.size} female users from API")
                    users.forEach { user ->
                        Log.d("MaleHomeVM", "   👤 Creator: ${user.name} (${user.id})")
                        Log.d("MaleHomeVM", "     - Audio Enabled: ${user.audioCallEnabled}")
                        Log.d("MaleHomeVM", "     - Video Enabled: ${user.videoCallEnabled}")
                        Log.d("MaleHomeVM", "     - Is Online: ${user.isOnline}")
                    }

                    _state.update {
                        it.copy(
                            femaleUsers = users,
                            isLoading = false,
                            isRefreshing = false
                        )
                    }
                }.onFailure { error ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            error = error.message ?: "Failed to load users"
                        )
                    }
                }
            }
        }
    }
    
    fun refresh() {
        loadFemaleUsers(isRefresh = true)
        loadCoinBalance() // Refresh balance when user pulls to refresh
    }
    
    /**
     * Update online datetime
     * Called when the screen is shown/resumed for male users
     * Only calls API if gender is MALE
     */
    fun updateOnlineDatetime() {
        // Only update if gender is MALE
        if (sessionManager.getGender() != Gender.MALE) {
            return
        }
        
        viewModelScope.launch {
            repository.updateOnlineDatetime().onSuccess {
                // Successfully updated online datetime
                // No need to update state, just log for debugging
            }.onFailure {
                // Silently fail - don't show error to user for this background operation
            }
        }
    }
}
