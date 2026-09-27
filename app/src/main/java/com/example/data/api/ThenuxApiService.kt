package com.example.data.api

import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface ThenuxApiService {
    @POST("api/chat")
    suspend fun chat(
        @Header("Authorization") authorization: String,
        @Body body: RequestBody
    ): Response<ResponseBody>
}
