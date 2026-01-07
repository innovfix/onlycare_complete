package com.onlycare.app.utils

import android.util.Log
import com.onlycare.app.data.local.SessionManager
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Simple debug helper to add coins for testing
 * No database or API needed!
 */
@Singleton
class DebugCoinHelper @Inject constructor(
    private val sessionManager: SessionManager
) {
    companion object {
        private const val TAG = "DebugCoinHelper"
    }

    /**
     * Add coins directly to local storage (for testing only)
     * This updates the coin balance in SharedPreferences
     */
    fun addTestCoins(amount: Int = 1000) {
        val currentBalance = sessionManager.getCoinBalance()
        val newBalance = currentBalance + amount
        
        sessionManager.updateCoinBalance(newBalance)
        
        Log.d(TAG, "✅ Added $amount coins for testing")
        Log.d(TAG, "   Old balance: $currentBalance")
        Log.d(TAG, "   New balance: $newBalance")
    }

    /**
     * Set coins to specific amount
     */
    fun setTestCoins(amount: Int = 1000) {
        sessionManager.updateCoinBalance(amount)
        Log.d(TAG, "✅ Set coins to $amount for testing")
    }

    /**
     * Get current coin balance
     */
    fun getCurrentBalance(): Int {
        return sessionManager.getCoinBalance()
    }
}




