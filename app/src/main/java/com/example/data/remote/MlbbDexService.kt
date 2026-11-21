package com.example.data.remote

import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface MlbbDexApi {
    @GET("api/v1/skins/")
    suspend fun getSkinsPage(
        @Query("limit") limit: Int = 100,
        @Query("page") page: Int? = null,
        @Query("offset") offset: Int? = null
    ): ResponseBody

    @GET("api/v1/heroes")
    suspend fun getHeroesPage(
        @Query("limit") limit: Int = 200,
        @Query("page") page: Int? = null
    ): ResponseBody

    @GET("api/v1/heroes/{slug}")
    suspend fun getHeroDetail(
        @Path("slug") slug: String
    ): ResponseBody
}

object MlbbDexClient {
    private const val BASE_URL = "https://mlbbdex.com/"

    val api: MlbbDexApi by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        val okHttp = OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(35, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .header("Accept", "application/json")
                    .header("User-Agent", "MegaCollectorAndroid/2.0")
                    .build()
                chain.proceed(request)
            }
            .addInterceptor(logging)
            .build()

        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttp)
            .build()
            .create(MlbbDexApi::class.java)
    }
}
