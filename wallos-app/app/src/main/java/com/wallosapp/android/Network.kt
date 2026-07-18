package com.wallosapp.android

import kotlinx.serialization.Serializable
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import okhttp3.MediaType.Companion.toMediaType
import kotlinx.serialization.json.Json
import retrofit2.http.*

// 1. kotlinx.serialization API Data Models
@Serializable
data class ApiSubscription(
    val id: Int? = null,
    val name: String,
    val price: Double,
    val currency: String = "$",
    val cycle: String,
    val category: String,
    val date: String
)

// 2. Retrofit API Service Interface
interface WallosApiService {
    @GET("api/subscriptions")
    suspend fun getSubscriptions(
        @Header("X-Api-Key") apiKey: String
    ): List<ApiSubscription>

    @POST("api/subscriptions")
    suspend fun createSubscription(
        @Header("X-Api-Key") apiKey: String,
        @Body subscription: ApiSubscription
    ): ApiSubscription

    @DELETE("api/subscriptions/{id}")
    suspend fun deleteSubscription(
        @Header("X-Api-Key") apiKey: String,
        @Path("id") id: Int
    )
}

// 3. Dynamic Retrofit Client Manager
object WallosClient {
    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    private var activeUrl: String = ""
    private var activeService: WallosApiService? = null

    /**
     * Dynamically resolves and creates the Retrofit service instance.
     */
    fun getService(baseUrl: String): WallosApiService {
        val normalizedUrl = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
        if (normalizedUrl == activeUrl && activeService != null) {
            return activeService!!
        }

        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        val contentType = "application/json".toMediaType()
        val retrofit = Retrofit.Builder()
            .baseUrl(normalizedUrl)
            .client(client)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()

        activeUrl = normalizedUrl
        activeService = retrofit.create(WallosApiService::class.java)
        return activeService!!
    }
}
