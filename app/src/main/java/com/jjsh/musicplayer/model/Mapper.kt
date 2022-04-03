package com.jjsh.musicplayer.model

fun MusicDto.mapper() : List<MusicModel> = musics.mapIndexed { index, musicEntity ->
    musicEntity.mapper(index.toLong())
}

fun MusicEntity.mapper(id : Long) : MusicModel =
    MusicModel(id, track, streamUrl, artist, coverUrl)