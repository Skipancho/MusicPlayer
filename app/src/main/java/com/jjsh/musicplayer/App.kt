package com.jjsh.musicplayer

import android.app.Application

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        instance = this
    }
    companion object {
        lateinit var instance: App
        const val HOST = "https://skipancho.cafe24.com"
    }

}