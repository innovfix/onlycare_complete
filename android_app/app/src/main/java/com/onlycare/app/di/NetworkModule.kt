package com.onlycare.app.di

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.onlycare.app.data.local.SessionManager
import com.onlycare.app.data.remote.api.*
import com.onlycare.app.data.remote.interceptor.AuthInterceptor
import com.onlycare.app.data.remote.interceptor.ErrorInterceptor
import com.onlycare.app.data.remote.interceptor.LoggingInterceptor
import com.onlycare.app.websocket.WebSocketManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    
    // Base URL - PRODUCTION (Permanent solution)
    // Ready for Play Store and live deployment
    private const val BASE_URL = "https://onlycare.in/api/v1/"
    
    init {
        // Debug: Print BASE_URL to verify configuration
        android.util.Log.e("NetworkModule", "╔═══════════════════════════════════════════════════════════╗")
        android.util.Log.e("NetworkModule", "║  NETWORK CONFIGURATION - PRODUCTION 🚀                    ║")
        android.util.Log.e("NetworkModule", "╚═══════════════════════════════════════════════════════════╝")
        android.util.Log.e("NetworkModule", "")
        android.util.Log.e("NetworkModule", "🌐 BASE_URL = $BASE_URL")
        android.util.Log.e("NetworkModule", "")
        android.util.Log.e("NetworkModule", "✅ Production Mode - Connecting to live server")
        android.util.Log.e("NetworkModule", "✅ SSL/HTTPS enabled")
        android.util.Log.e("NetworkModule", "✅ Ready for Google Play")
        android.util.Log.e("NetworkModule", "")
        android.util.Log.e("NetworkModule", "╚═══════════════════════════════════════════════════════════╝")
    }
    
    @Provides
    @Singleton
    fun provideGson(): Gson {
        return GsonBuilder()
            .setLenient()
            // Don't serialize nulls - Laravel's "sometimes" validator expects fields to be omitted if null
            .create()
    }
    
    @Provides
    @Singleton
    fun provideAuthInterceptor(sessionManager: SessionManager): AuthInterceptor {
        return AuthInterceptor(sessionManager)
    }
    
    @Provides
    @Singleton
    fun provideLoggingInterceptor(): LoggingInterceptor {
        return LoggingInterceptor()
    }
    
    @Provides
    @Singleton
    fun provideErrorInterceptor(): ErrorInterceptor {
        return ErrorInterceptor()
    }
    
    @Provides
    @Singleton
    fun provideOkHttpClient(
        authInterceptor: AuthInterceptor,
        loggingInterceptor: LoggingInterceptor,
        errorInterceptor: ErrorInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                // Add ngrok bypass header (optional, doesn't hurt for localhost)
                val request = chain.request().newBuilder()
                    .addHeader("ngrok-skip-browser-warning", "true")
                    .build()
                chain.proceed(request)
            }
            .addInterceptor(errorInterceptor)
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .build()
    }
    
    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        gson: Gson
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }
    
    @Provides
    @Singleton
    fun provideWebSocketManager(sessionManager: SessionManager): WebSocketManager {
        return WebSocketManager(sessionManager)
    }
    
    // API Services
    
    @Provides
    @Singleton
    fun provideAuthApiService(retrofit: Retrofit): AuthApiService {
        return retrofit.create(AuthApiService::class.java)
    }
    
    @Provides
    @Singleton
    fun provideUserApiService(retrofit: Retrofit): UserApiService {
        return retrofit.create(UserApiService::class.java)
    }
    
    @Provides
    @Singleton
    fun provideCallApiService(retrofit: Retrofit): CallApiService {
        return retrofit.create(CallApiService::class.java)
    }
    
    @Provides
    @Singleton
    fun provideWalletApiService(retrofit: Retrofit): WalletApiService {
        return retrofit.create(WalletApiService::class.java)
    }
    
    @Provides
    @Singleton
    fun provideEarningsApiService(retrofit: Retrofit): EarningsApiService {
        return retrofit.create(EarningsApiService::class.java)
    }
    
    @Provides
    @Singleton
    fun provideChatApiService(retrofit: Retrofit): ChatApiService {
        return retrofit.create(ChatApiService::class.java)
    }
    
    @Provides
    @Singleton
    fun provideKycApiService(retrofit: Retrofit): KycApiService {
        return retrofit.create(KycApiService::class.java)
    }
    
    @Provides
    @Singleton
    fun provideFriendApiService(retrofit: Retrofit): FriendApiService {
        return retrofit.create(FriendApiService::class.java)
    }
    
    @Provides
    @Singleton
    fun provideContentApiService(retrofit: Retrofit): ContentApiService {
        return retrofit.create(ContentApiService::class.java)
    }
    
    @Provides
    @Singleton
    fun provideReferralApiService(retrofit: Retrofit): ReferralApiService {
        return retrofit.create(ReferralApiService::class.java)
    }
    
    @Provides
    @Singleton
    fun provideAvatarApiService(retrofit: Retrofit): AvatarApiService {
        return retrofit.create(AvatarApiService::class.java)
    }
    
    @Provides
    @Singleton
    fun provideGiftApiService(retrofit: Retrofit): GiftApiService {
        return retrofit.create(GiftApiService::class.java)
    }
}
