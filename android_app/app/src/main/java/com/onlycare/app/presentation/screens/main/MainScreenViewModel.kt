package com.onlycare.app.presentation.screens.main

import androidx.lifecycle.ViewModel
import com.onlycare.app.data.local.SessionManager
import com.onlycare.app.domain.model.Gender
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainScreenViewModel @Inject constructor(
    private val sessionManager: SessionManager
) : ViewModel() {
    
    fun getGender(): Gender {
        return sessionManager.getGender()
    }
}


