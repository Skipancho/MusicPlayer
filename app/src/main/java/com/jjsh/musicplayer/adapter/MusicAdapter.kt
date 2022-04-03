package com.jjsh.musicplayer.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.jjsh.musicplayer.databinding.MusicItemBinding
import com.jjsh.musicplayer.model.MusicModel

class MusicAdapter(
    private var musics  : List<MusicModel>,
    private val callback: (MusicModel) -> Unit
) :  RecyclerView.Adapter<MusicAdapter.MusicItemViewHolder>(){

    fun submitList(list : List<MusicModel>){
        musics = list
    }

    override fun getItemCount(): Int = musics.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MusicItemViewHolder {
        val binding = MusicItemBinding.inflate(
            LayoutInflater.from(parent.context),parent,false
        )
        return MusicItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MusicItemViewHolder, position: Int) {
        val music = musics[position]
        holder.bind(music)
    }

    inner class MusicItemViewHolder(
        private val binding : MusicItemBinding
    ):RecyclerView.ViewHolder(binding.root){
        fun bind(music : MusicModel){
            binding.musicModel = music
            Glide.with(binding.musicThumb.context)
                .load(music.coverUrl)
                .into(binding.musicThumb)

            itemView.setBackgroundColor(if (music.isPlaying) Color.GRAY else Color.TRANSPARENT)

            itemView.setOnClickListener {
                callback(music)
            }
        }
    }
}