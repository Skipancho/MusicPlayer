package com.jjsh.musicplayer.api

import com.jjsh.musicplayer.model.MusicDto
import retrofit2.Call
import retrofit2.http.GET

interface MusicApi {
    @GET("/test1/file/sample.json")
    fun getMusics() : Call<MusicDto>

    companion object{
        val instance = ApiGenerator().generate(MusicApi::class.java)
    }
}