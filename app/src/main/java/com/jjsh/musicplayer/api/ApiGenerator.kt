package com.jjsh.musicplayer.api

import com.jjsh.musicplayer.App
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ApiGenerator {
    fun <T> generate(api : Class<T>) : T = Retrofit.Builder()
        .baseUrl(App.HOST)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(api)
}