package com.onlycare.app.presentation.screens.gift

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onlycare.app.data.remote.dto.SendGiftData
import com.onlycare.app.data.repository.ApiDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GiftState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val giftSent: SendGiftData? = null
)

@HiltViewModel
class GiftViewModel @Inject constructor(
    private val repository: ApiDataRepository
) : ViewModel() {
    
    companion object {
        private const val TAG = "GiftViewModel"
    }
    
    private val _state = MutableStateFlow(GiftState())
    val state: StateFlow<GiftState> = _state.asStateFlow()
    
    fun sendGift(
        userId: String,
        receiverId: String,
        giftId: Int,
        onSuccess: (SendGiftData) -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            Log.d(TAG, "========================================")
            Log.d(TAG, "📡 API CALL: Sending Gift")
            Log.d(TAG, "========================================")
            Log.d(TAG, "Endpoint: POST /auth/send_gifts")
            Log.d(TAG, "User ID: $userId")
            Log.d(TAG, "Receiver ID: $receiverId")
            Log.d(TAG, "Gift ID: $giftId")
            Log.d(TAG, "Status: Sending...")
            
            _state.value = _state.value.copy(isLoading = true, error = null)
            
            repository.sendGift(userId, receiverId, giftId)
                .onSuccess { giftData ->
                    Log.d(TAG, "✅ API SUCCESS: Gift Sent Successfully")
                    Log.d(TAG, "   Gift Icon: ${giftData.giftIcon}")
                    Log.d(TAG, "   Gift Coins: ${giftData.giftCoins}")
                    Log.d(TAG, "   Sender: ${giftData.senderName}")
                    Log.d(TAG, "   Receiver: ${giftData.receiverName}")
                    Log.d(TAG, "========================================")
                    
                    _state.value = _state.value.copy(
                        isLoading = false,
                        giftSent = giftData,
                        error = null
                    )
                    onSuccess(giftData)
                }
                .onFailure { exception ->
                    val errorMessage = exception.message ?: "Failed to send gift"
                    Log.e(TAG, "❌ API FAILED: Send Gift")
                    Log.e(TAG, "   Error: $errorMessage")
                    Log.d(TAG, "========================================")
                    
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = errorMessage
                    )
                    onError(errorMessage)
                }
        }
    }
    
    fun clearState() {
        _state.value = GiftState()
    }
}


