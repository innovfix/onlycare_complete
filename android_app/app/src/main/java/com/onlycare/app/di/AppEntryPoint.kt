package com.onlycare.app.di

import com.onlycare.app.data.local.SessionManager
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface AppEntryPoint {
    fun sessionManager(): SessionManager
}





