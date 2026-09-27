package com.example.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class NovaApiResponse(
    val success: Boolean? = true,
    val creator: String? = null,
    val query: String? = null,
    val results: NovaResults? = null,
    val error: String? = null
)

@JsonClass(generateAdapter = true)
data class NovaResults(
    val text: String? = null
)
