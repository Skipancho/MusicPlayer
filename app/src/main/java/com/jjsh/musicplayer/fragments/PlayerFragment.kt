package com.jjsh.musicplayer.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.jjsh.musicplayer.R
import com.jjsh.musicplayer.adapter.MusicAdapter
import com.jjsh.musicplayer.api.MusicApi
import com.jjsh.musicplayer.databinding.FragmentPlayerBinding
import com.jjsh.musicplayer.model.MusicDto
import com.jjsh.musicplayer.model.MusicModel
import com.jjsh.musicplayer.model.mapper
import com.jjsh.musicplayer.utils.ImageLoader
import com.jjsh.musicplayer.viewmodel.PlayerViewModel
import kotlinx.coroutines.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PlayerFragment : Fragment() {

    private lateinit var binding : FragmentPlayerBinding
    private lateinit var adapter : MusicAdapter
    private lateinit var player : ExoPlayer
    private val musicList = mutableListOf<MusicModel>()

    private val viewModel by lazy{
        ViewModelProvider(this)[PlayerViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPlayerBinding.inflate(inflater,container,false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initPlayer()
        initController()
        initSeekBar()
        initRecyclerView()
        initPlayListBtn()

        getMusicsFromServer()
    }

    private fun initController(){
        binding.playControlImageView.setOnClickListener {
            if (player.isPlaying) player.pause()
            else player.play()
        }

        binding.skipNextImageView.setOnClickListener {
            val next = viewModel.nextMusic() ?: return@setOnClickListener
            playMusic(next)
        }

        binding.skipPrevImageView.setOnClickListener {
            val prev = viewModel.prevMusic() ?: return@setOnClickListener
            playMusic(prev)
        }
    }

    private fun initRecyclerView(){
        adapter = MusicAdapter(musicList){playMusic(it)}
        binding.playListRecyclerView.adapter = adapter
        binding.playListRecyclerView.layoutManager = LinearLayoutManager(context)
    }

    private fun initSeekBar(){
        binding.playerSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {

            }

            override fun onStartTrackingTouch(p0: SeekBar?) {

            }

            override fun onStopTrackingTouch(p0: SeekBar) {
                player.seekTo(p0.progress*1000L)
            }

        })
    }

    private fun initPlayListBtn(){
        binding.playListImageView.setOnClickListener {
            if (viewModel.position.value == -1) return@setOnClickListener

            binding.playerViewGroup.isVisible = !binding.playerViewGroup.isVisible
            binding.playListGroup.isVisible = ! binding.playListGroup.isVisible
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun initPlayer(){
        context?.let {
            player = ExoPlayer.Builder(it).build()
        }
        binding.playerView.player = player
        player.addListener(object : Player.Listener{
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                super.onIsPlayingChanged(isPlaying)

                binding.playControlImageView.setImageResource(
                    if (isPlaying) R.drawable.ic_baseline_pause_48
                    else R.drawable.ic_baseline_play_arrow_24
                )
            }

            @SuppressLint("NotifyDataSetChanged")
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                super.onMediaItemTransition(mediaItem, reason)
                val newIndex = mediaItem?.mediaId ?: return
                viewModel.position.value = newIndex.toInt()
                adapter.submitList(viewModel.getList())

                binding.playListRecyclerView.scrollToPosition(newIndex.toInt())

                updatePlayer(viewModel.curMusic())
                adapter.notifyDataSetChanged()
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                super.onPlaybackStateChanged(playbackState)
                GlobalScope.launch(Dispatchers.Main) { updateSeek() }
            }
        })
    }

    private fun updatePlayer(curMusic : MusicModel?){
        if (curMusic == null) return

        binding.trackTextView.text = curMusic.track
        binding.artistTextView.text = curMusic.artist

        ImageLoader.loadImage(curMusic.coverUrl){binding.coverImageView.setImageBitmap(it)}
    }

    private suspend fun updateSeek(){
        val duration = if (player.duration >= 0) player.duration else 0
        val position = player.currentPosition

       viewModel.updateSeekUi(duration, position)

        val state = player.playbackState

        if (state != Player.STATE_ENDED && state != Player.STATE_IDLE){
            delay(1000)
            updateSeek()
        }
    }

    private fun getMusicsFromServer(){
        try {
            val call = MusicApi.instance.getMusics()
            call.enqueue(object : Callback<MusicDto> {
               override fun onResponse(call: Call<MusicDto>, response: Response<MusicDto>) {
                    response.body()?.let { musicDto ->
                        viewModel.musicList = musicDto.mapper().toMutableList()
                        setMusicList(viewModel.getList())
                        adapter.submitList(viewModel.getList())
                    }
                }

                override fun onFailure(call: Call<MusicDto>, t: Throwable) {
                    //do nothing
                }

            })
        }catch (e : Exception){
           Toast.makeText(context,"에러",Toast.LENGTH_SHORT).show()
        }
    }

    private fun setMusicList( list : List<MusicModel>){
        context?.let {
            player.addMediaItems(list.map {
                MediaItem.Builder()
                    .setMediaId(it.id.toString())
                    .setUri(it.streamUrl)
                    .build()
            })
            player.prepare()
        }
    }

    private fun playMusic(music : MusicModel){
        viewModel.updatePosition(music)
        player.seekTo(viewModel.position.value?:return,0)
    }

    override fun onStop() {
        super.onStop()
        player.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        player.release()
    }

    companion object{
        fun newInstance() = PlayerFragment()
    }
}