package com.jjsh.musicplayer.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.Exception
import java.net.URL

object ImageLoader {
    private val cache = mutableMapOf<String, Bitmap>()

    fun loadImage(url: String, completed: (Bitmap?) -> Unit) {
        if (url.isEmpty()){
            completed(null)
            return
        }else if (cache.containsKey(url)){
            completed(cache[url])
            return
        }
        GlobalScope.launch(Dispatchers.IO){
            try {
                val bitmap = BitmapFactory.decodeStream(URL(url).openStream())
                cache[url] = bitmap
                withContext(Dispatchers.Main){ completed(bitmap) }
            }catch (e : Exception){
                withContext(Dispatchers.Main){ completed(null)}
            }
        }
    }
}