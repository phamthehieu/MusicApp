package com.example.musicapp.data.network

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.QueryMap

interface RetrofitTesting {
    @GET("search")
    fun search(@Query("q") query: String): Call<ResponseBody>
}