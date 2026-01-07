package com.onlycare.app.presentation.screens.gift

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onlycare.app.data.remote.dto.GiftData
import com.onlycare.app.data.repository.ApiDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GiftImageState(
    val isLoading: Boolean = false,
    val gifts: List<GiftData> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class GiftImageViewModel @Inject constructor(
    private val repository: ApiDataRepository
) : ViewModel() {
    
    companion object {
        private const val TAG = "GiftImageViewModel"
    }
    
    private val _state = MutableStateFlow(GiftImageState())
    val state: StateFlow<GiftImageState> = _state.asStateFlow()
    
    fun fetchGiftImages() {
        viewModelScope.launch {
            Log.d(TAG, "========================================")
            Log.d(TAG, "📡 API CALL: Fetching Gift Images")
            Log.d(TAG, "========================================")
            Log.d(TAG, "Endpoint: POST /auth/gifts_list")
            Log.d(TAG, "Status: Loading...")
            
            _state.value = _state.value.copy(isLoading = true, error = null)
            
            repository.getGiftImages()
                .onSuccess { gifts ->
                    Log.d(TAG, "✅ API SUCCESS: Gift Images Fetched")
                    Log.d(TAG, "   Total Gifts: ${gifts.size}")
                    gifts.forEachIndexed { index, gift ->
                        Log.d(TAG, "   Gift ${index + 1}: ID=${gift.id}, Coins=${gift.coins}, Icon=${gift.giftIcon}")
                    }
                    Log.d(TAG, "========================================")
                    
                    _state.value = _state.value.copy(
                        isLoading = false,
                        gifts = gifts,
                        error = null
                    )
                }
                .onFailure { exception ->
                    val errorMessage = exception.message ?: "Failed to fetch gifts"
                    Log.e(TAG, "❌ API FAILED: Fetch Gift Images")
                    Log.e(TAG, "   Error: $errorMessage")
                    Log.d(TAG, "========================================")
                    
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = errorMessage
                    )
                }
        }
    }
}


