package com.example.data.api

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface NovaApiService {
    @GET("api/ai/ai/nova")
    suspend fun queryNova(
        @Query("q") query: String,
        @Query("apikey") apiKey: String
    ): Response<ResponseBody>
}
