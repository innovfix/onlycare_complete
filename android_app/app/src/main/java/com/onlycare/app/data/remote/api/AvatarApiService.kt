package com.onlycare.app.data.remote.api

import com.onlycare.app.data.remote.dto.GetAvatarsResponse
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

interface AvatarApiService {
    
    @Multipart
    @POST("avatars")
    fun getAvatars(
        @Part("gender") gender: okhttp3.RequestBody // "male" or "female" (lowercase)
    ): Call<GetAvatarsResponse>
}

