package com.linwei.mymusic.fragment

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.PlaybackState
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import com.linwei.mymusic.MyApplication
import com.linwei.mymusic.R
import com.linwei.mymusic.constant.Constant
import com.linwei.mymusic.constant.Constant.mMediaController
import com.linwei.mymusic.constant.Constant.mMediaPlayer
import kotlinx.android.synthetic.main.play_activity_layout.view.*
import java.text.SimpleDateFormat

class SongPlayFragment (private val setbaackgroundlistener:setBackgroundListener): Fragment() {

    private lateinit var v: View
    @SuppressLint("SimpleDateFormat")
    private val timefarmat= SimpleDateFormat("mm:ss")

    private val handler = Handler()
    private val runable = object : Runnable {
        override fun run() {
            if (mMediaPlayer.isPlaying){
                val currentpostion=mMediaPlayer.currentPosition
                v.songseekBar.progress = currentpostion
                v.textcurrrentPosition.text="${timefarmat.format(currentpostion)}"
            }
            handler.postDelayed(this, 200L)
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        v = inflater.inflate(R.layout.play_activity_layout, null)
        return v
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initView()
    }

    private fun initView() {
        val description = mMediaController?.metadata!!.description
        val duration=mMediaController?.metadata!!.getLong(MediaMetadata.METADATA_KEY_DURATION).toInt()
        v.songseekBar.max = duration
        handler.post(runable)
        //初始化页面数据
        v.textduration.text="${timefarmat.format(duration)}"
        v.playtextSongName.text="${description.title}"
        v.playtextSongArits.text="${description.subtitle}"


        val playstate = mMediaController?.playbackState!!.state
        mMediaController!!.registerCallback(Controllcallback)
        if (playstate == PlaybackState.STATE_PLAYING) {
            v.imagePlay_Pause!!.setImageResource(R.drawable.ic_pause_normal)
        } else {
            v.imagePlay_Pause!!.setImageResource(R.drawable.ic_paly_normal)
        }
        v.imageAlbum!!.setImageBitmap(
            try {
                val bitmap=BitmapFactory.decodeStream(
                    activity?.contentResolver!!.openInputStream(
                        description.iconUri!!
                    )
                )
                setbaackgroundlistener.setbackground(bitmap)
                bitmap
            } catch (e: Exception) {
                val bitmap=BitmapFactory.decodeResource(context?.resources, R.drawable.music_icon)
                setbaackgroundlistener.setbackground(bitmap)
                bitmap
            }
        )

        v.imagePlay_Pause!!.setOnClickListener {
            if (mMediaPlayer.isPlaying) {
                it.imagePlay_Pause.setImageResource(R.drawable.ic_paly_normal)
                mMediaController?.transportControls?.pause()
            } else {
                it.imagePlay_Pause.setImageResource(R.drawable.ic_pause_normal)
                mMediaController?.transportControls?.play()
            }
        }
        v.imageNext!!.setOnClickListener {
            mMediaController?.transportControls?.skipToNext()
        }
        v.imagePrevious!!.setOnClickListener {
            mMediaController?.transportControls?.skipToPrevious()
        }
        initorder()
        v.imageOrder!!.setOnClickListener {
            mMediaController?.transportControls?.sendCustomAction("PlayMode", null)
        }
            v.songseekBar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener{
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    if (fromUser){
                        mMediaController?.transportControls!!.seekTo(progress.toLong())
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}

                override fun onStopTrackingTouch(seekBar: SeekBar?) { }
            })


    }

    private fun initorder() {
        when (Constant.isOrder) {
            Constant.ORDER -> {
                v.imageOrder!!.setImageResource(R.drawable.ic_loop)
            }
            Constant.UNORDER -> {
                v.imageOrder!!.setImageResource(R.drawable.ic_random)

            }
            Constant.ONEORDER -> {
                v.imageOrder!!.setImageResource(R.drawable.ic_one_loop)
            }
        }
    }

    private val Controllcallback = object : MediaController.Callback() {
        override fun onPlaybackStateChanged(state: PlaybackState?) {
            super.onPlaybackStateChanged(state)
            when (state!!.state) {
                PlaybackState.STATE_PLAYING -> {
                    v.imagePlay_Pause!!.setImageResource(R.drawable.ic_pause_normal)
                }
                PlaybackState.STATE_PAUSED -> {
                    v.imagePlay_Pause!!.setImageResource(R.drawable.ic_paly_normal)
                }
            }
        }

        override fun onMetadataChanged(metadata: MediaMetadata?) {
            super.onMetadataChanged(metadata)
            val description = metadata?.description!!
            val duration= metadata.getLong(MediaMetadata.METADATA_KEY_DURATION).toInt()
            v.songseekBar.progress=0
            v.songseekBar.max =duration
            v.textcurrrentPosition.text="00:00"
            v.textduration.text= timefarmat.format(duration)
            v.playtextSongName.text="${description.title}"
            v.playtextSongArits.text="${description.subtitle}"
            v.imageAlbum!!.setImageBitmap(
                try {
                    val bitmap=BitmapFactory.decodeStream(
                        activity?.contentResolver!!.openInputStream(
                            description.iconUri!!
                        )
                    )
                    setbaackgroundlistener.setbackground(bitmap)
                    bitmap
                } catch (e: Exception) {
                    val bitmap=BitmapFactory.decodeResource(MyApplication.context.resources, R.drawable.music_icon)
                    setbaackgroundlistener.setbackground(bitmap)
                    bitmap
                }
            )
        }

        override fun onSessionEvent(event: String, extras: Bundle?) {
            super.onSessionEvent(event, extras)
            when (event.toInt()) {
                Constant.ORDER -> {
                    v.imageOrder!!.setImageResource(R.drawable.ic_loop)
                }
                Constant.UNORDER -> {
                    v.imageOrder!!.setImageResource(R.drawable.ic_random)

                }
                Constant.ONEORDER -> {
                    v.imageOrder!!.setImageResource(R.drawable.ic_one_loop)
                }
            }
        }
    }

    interface setBackgroundListener{
        fun setbackground(bitmap: Bitmap){}
    }

}