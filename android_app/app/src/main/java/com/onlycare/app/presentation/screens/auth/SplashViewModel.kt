package com.onlycare.app.presentation.screens.auth

import androidx.lifecycle.ViewModel
import com.onlycare.app.data.local.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    val sessionManager: SessionManager
) : ViewModel()


