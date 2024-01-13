package com.om.mymovie.api

import android.content.Context
import com.jenish.videodownloader.util.ShardPreference
import com.om.mymovie.ResponseMovie
import com.om.mymovie.ads.Glob
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

class RetrofitClient(private val context: Context) {
//    private const val BASE_URL = "https://movietrailrswatch.onrender.com/"

    private val interceptor = Interceptor { chain ->
        val request = chain.request().newBuilder()
//            .addHeader("Authorization", "Bearer 914c8081c0fbc417e62a135966381641")
            .addHeader("Authorization", Glob.apiKey.toString())
            .build()
        chain.proceed(request)
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(interceptor)
        .build()

    val instance: ApiService by lazy {
        val retrofit = Retrofit.Builder()
//            .baseUrl(BASE_URL)
            .baseUrl(Glob.api.toString())
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()

        retrofit.create(ApiService::class.java)
    }
}

interface ApiService {
    @GET("get?key=111")
    /*@GET("get?key=111")*/
    fun getMovies(@Query("page") page: Int): Call<ResponseMovie>
}