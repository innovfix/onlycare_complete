package com.onlycare.app.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    // SessionManager is provided via @Inject constructor in SessionManager class
    // No need for explicit @Provides here
}
