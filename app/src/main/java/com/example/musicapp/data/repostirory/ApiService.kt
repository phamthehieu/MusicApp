package com.example.musicapp.data.repostirory

import com.example.musicapp.common.Common.BASE_URL
import com.example.musicapp.common.Common.RAPIDAPI_HOST
import com.example.musicapp.common.Common.RAPIDAPI_KEY
import com.example.musicapp.data.network.RetrofitTesting
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ApiService {

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .client(
            OkHttpClient.Builder()
                .addInterceptor { chain ->
                    val request = chain.request().newBuilder()
                        .addHeader(
                            "X-RapidAPI-Key",
                            RAPIDAPI_KEY
                        )
                        .addHeader("X-RapidAPI-Host", RAPIDAPI_HOST)
                        .build()
                    chain.proceed(request)
                }
                .build())
        .build()

    private val service = retrofit.create(RetrofitTesting::class.java)

    suspend fun search(query: String): String {
        return withContext(Dispatchers.IO) {
            val call = service.search(query)
            val response = call.execute()
            if (response.isSuccessful) {
                response.body()?.string() ?: ""
            } else {
                ""
            }
        }
    }

}