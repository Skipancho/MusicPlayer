package com.jjsh.musicplayer.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.jjsh.musicplayer.model.MusicModel
import java.util.concurrent.TimeUnit


class PlayerViewModel(app : Application) : AndroidViewModel(app) {
    var musicList = mutableListOf<MusicModel>()
    val position = MutableLiveData(-1)
    val max = MutableLiveData(100)
    val progress = MutableLiveData(0)
    val playTime = MutableLiveData("00:00")
    val totalTime = MutableLiveData("00:00")


    fun getList() : List<MusicModel> =
        musicList.onEachIndexed { idx, music ->
            music.isPlaying = idx == position.value
        }

    fun updatePosition(music : MusicModel){
        position.postValue(musicList.indexOf(music))
    }

    fun curMusic() = if (musicList.isNullOrEmpty()) null else musicList[position.value!!]

    fun updateSeekUi(duration: Long, position : Long){
        max.value =(duration/1000).toInt()
        progress.value = (position/1000).toInt()
        playTime.value = String.format(
            "%02d:%02d",
            TimeUnit.MINUTES.convert(position, TimeUnit.MILLISECONDS),
            (position/1000)%60
        )
        totalTime.value = (String.format(
            "%02d:%02d",
            TimeUnit.MINUTES.convert(duration, TimeUnit.MILLISECONDS),
            (duration/1000)%60
        ))
    }

    fun nextMusic() : MusicModel?{
        if (musicList.isNullOrEmpty()) return null
        var cur = position.value!!

        cur = if (cur  < musicList.lastIndex) cur + 1 else 0

        return musicList[cur]
    }

    fun prevMusic() : MusicModel?{
        if (musicList.isNullOrEmpty()) return null
        var cur = position.value!!

        cur = if (cur > 0) cur - 1 else musicList.lastIndex

        return musicList[cur]
    }
}