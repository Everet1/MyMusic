package com.linwei.mymusic.mediaservice

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.MediaMetadata
import android.media.MediaPlayer
import android.media.browse.MediaBrowser
import android.media.session.MediaSession
import android.media.session.PlaybackState
import android.net.Uri
import android.os.Bundle
import android.os.SystemClock
import android.service.media.MediaBrowserService
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import android.view.KeyEvent
import androidx.media.session.MediaButtonReceiver
import com.linwei.mymusic.MyApplication
import com.linwei.mymusic.R
import com.linwei.mymusic.activity.MainActivity
import com.linwei.mymusic.constant.Constant
import com.linwei.mymusic.constant.Constant.isOrder
import com.linwei.mymusic.constant.Constant.mMediaPlayer
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList
import kotlin.random.Random

class BrowserService : MediaBrowserService() {
    private lateinit var mMediaSession: MediaSession
    private lateinit var mPlaybackState: PlaybackState
    private lateinit var mNotificationManager: NotificationManager
    private lateinit var builder: Notification.Builder
    private lateinit var notification: Notification
    private var clickCounts = 0
    private var nowSong = 0
    private val TAG = "Service"
    private val metadatalist = ArrayList<MediaMetadata>()
    private lateinit var metadata: MediaMetadata
    private val timer = Timer()
    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot {

        return BrowserRoot(Constant.Root_ID, null)
    }

    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowser.MediaItem>>
    ) {
        //这里获取到元数据的列表 ，然后返回给客户端
        result.detach()
        mMediaSession.controller.transportControls.prepare()
    }

    override fun onCreate() {
        super.onCreate()
        //createMetadata()
        mPlaybackState = PlaybackState.Builder()
            .setState(PlaybackState.STATE_NONE, 0, 1f, SystemClock.elapsedRealtime())
            .build()
        mMediaSession = MediaSession(this, "my_Session")
        Constant.sessionToken = mMediaSession.sessionToken
        sessionToken = Constant.sessionToken //这一步必须要有
        mMediaSession.setCallback(sessionCallback)
//        mMediaSession.setFlags(MediaSession.FLAG_HANDLES_MEDIA_BUTTONS or MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS)
        mMediaSession.setPlaybackState(mPlaybackState)
        mMediaPlayer = MediaPlayer()
        buildNotification()
        mMediaPlayer.setOnPreparedListener {
            createMetadata()
            if (Constant.isFirstRun) {
                Constant.isFirstRun = false
            } else {
                it.start()
                mPlaybackState = PlaybackState.Builder()
                    .setState(
                        PlaybackState.STATE_PLAYING,
                        mMediaPlayer.currentPosition.toLong(),
                        1f
                    )
                    .setActions(PlaybackState.ACTION_SEEK_TO)
                    .build()
                mMediaSession.setPlaybackState(mPlaybackState)
            }
            buildNotification()
        }
        //播放完毕的回调
        mMediaPlayer.setOnCompletionListener {
            //自动播放下一曲
            playMode()
            mMediaPlayer.reset()
            mMediaSession.controller.transportControls.prepare()
        }
    }

    private fun playMode() {
        when (isOrder) {
            Constant.ORDER -> {
                nowSong = (nowSong + 1) % Constant.songList.size
            }
            Constant.UNORDER -> {
                nowSong = Random.nextInt(Constant.songList.size)
            }
            Constant.ONEORDER -> {

            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mMediaPlayer.release()
    }

    private val sessionCallback = object : MediaSession.Callback() {
        override fun onPrepare() {
            super.onPrepare()
            Log.e("musicsever",Constant.songList[nowSong].SongURI)
//            mMediaPlayer.setDataSource(
//                Constant.songList[nowSong].SongURI
//            )
            mMediaPlayer.setDataSource(this@BrowserService,Constant.songList[nowSong].contentUri)
            mMediaPlayer.prepare()
            buildNotification()
        }

        override fun onPlay() {
            super.onPlay()
            mMediaPlayer.start()
            mPlaybackState = PlaybackState.Builder()
                .setState(
                    PlaybackState.STATE_PLAYING,
                    mMediaPlayer.currentPosition.toLong(),
                    1f
                )
                .setActions(PlaybackState.ACTION_SEEK_TO)
                .build()
            mMediaSession.setPlaybackState(mPlaybackState)
            buildNotification()

        }

        override fun onPause() {
            super.onPause()
            mMediaPlayer.pause()
            mPlaybackState = PlaybackState.Builder()
                .setState(
                    PlaybackState.STATE_PAUSED,
                    mMediaPlayer.currentPosition.toLong(),
                    0f
                )
                .build()
            mMediaSession.setPlaybackState(mPlaybackState)
            buildNotification()

        }

        override fun onSkipToNext() {
            super.onSkipToNext()
            if (isOrder == Constant.UNORDER) {
                nowSong = Random.nextInt(Constant.songList.size)
            } else {
                nowSong = (nowSong + 1) % Constant.songList.size
            }
            mMediaPlayer.reset()
            onPrepare()
            buildNotification()

        }

        override fun onSkipToPrevious() {
            super.onSkipToPrevious()
            if (isOrder == Constant.UNORDER) {
                nowSong = Random.nextInt(Constant.songList.size)
            } else {
                if (nowSong == 0) nowSong = Constant.songList.size - 1
                else nowSong -= 1
            }
            mMediaPlayer.reset()
            onPrepare()
            buildNotification()
        }

        override fun onMediaButtonEvent(mediaButtonIntent: Intent): Boolean {
            Log.e(TAG, "ddd")
            val keyevent = mediaButtonIntent.getParcelableExtra<KeyEvent>(Intent.EXTRA_KEY_EVENT)
            if (keyevent?.action == KeyEvent.ACTION_UP) {
                clickCounts += 1
                if (clickCounts == 1) {
                    val timetask = MyTimerTask()
                    timer.schedule(timetask, 1000L)
                }
            }
            return true
        }

        override fun onSeekTo(pos: Long) {
            super.onSeekTo(pos)
            if (mPlaybackState.state == PlaybackState.STATE_PLAYING) {
                mMediaPlayer.seekTo(pos.toInt())
                mPlaybackState = PlaybackState.Builder()
                    .setState(
                        PlaybackState.STATE_PLAYING,
                        mMediaPlayer.currentPosition.toLong(),
                        1f
                    )
                    .setActions(PlaybackState.ACTION_SEEK_TO)
                    .build()
                mMediaSession.setPlaybackState(mPlaybackState)
                buildNotification()
            }
        }

        override fun onCustomAction(action: String, extras: Bundle?) {
            super.onCustomAction(action, extras)
            if (action == "PlayMode") {
                when (isOrder) {
                    Constant.ORDER -> {
                        isOrder = Constant.UNORDER
                    }
                    Constant.UNORDER -> {
                        isOrder = Constant.ONEORDER
                    }
                    Constant.ONEORDER -> {
                        isOrder = Constant.ORDER
                    }
                }
                mMediaSession.sendSessionEvent("$isOrder",null)
                buildNotification()
            } else {
                Constant.isFirstRun = false
                nowSong = extras!!.getInt(action, 0)
                mMediaPlayer.reset()
                onPrepare()
            }

        }
    }

    inner class MyTimerTask : TimerTask() {
        override fun run() {

            if (clickCounts == 1) {
                if (mPlaybackState.state == PlaybackState.STATE_PLAYING) {
                    mMediaSession.controller.transportControls.pause()

                } else {
                    mMediaSession.controller.transportControls.play()
                }
            }
            if (clickCounts == 2) {
                mMediaSession.controller.transportControls.skipToNext()
            }
            if (clickCounts == 3) {
                mMediaSession.controller.transportControls.skipToPrevious()
            }
            clickCounts = 0
        }
    }

    fun buildNotification() {
        mNotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel =
            NotificationChannel(Constant.notinChannel, "音乐控制", NotificationManager.IMPORTANCE_LOW)
        mNotificationManager.createNotificationChannel(channel)
        builder = Notification.Builder(this, Constant.notinChannel)
        builder.setVisibility(Notification.VISIBILITY_PUBLIC)
        builder.setSmallIcon(R.drawable.icon_music)
        builder.setLargeIcon(
            try {
                BitmapFactory.decodeStream(contentResolver.openInputStream(Uri.parse(Constant.songList[nowSong].coverPath)))
            } catch (e: Exception) {
                Log.e(TAG, e.toString())
                BitmapFactory.decodeResource(resources, R.drawable.music_icon)
            }

        )
        builder.setOngoing(mPlaybackState.state == PlaybackState.STATE_PLAYING)
        builder.setOngoing(true)
        builder.setSubText("")
        builder.setContentTitle(Constant.songList[nowSong].songTitle)
        builder.setContentText(Constant.songList[nowSong].songArtist)
        builder.setContentIntent(
            PendingIntent.getActivity(
                this,
                0,
                Intent(this, MainActivity::class.java),
                0
            )
        )
        builder.style = Notification.MediaStyle().setMediaSession(Constant.sessionToken)
            .setShowActionsInCompactView(1, 2)

        builder.addAction(
            createAction(
                android.R.drawable.ic_media_previous,
                "Previous",
                MediaButtonReceiver.buildMediaButtonPendingIntent(
                    MyApplication.context,
                    PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
                )
            )
        )
        if (mPlaybackState.state == PlaybackState.STATE_PAUSED || mPlaybackState.state == PlaybackState.STATE_NONE) {
            builder.addAction(
                createAction(
                    android.R.drawable.ic_media_play, "Play",
                    MediaButtonReceiver.buildMediaButtonPendingIntent(
                        MyApplication.context,
                        PlaybackStateCompat.ACTION_PLAY
                    )
                )

            )
        } else if (mPlaybackState.state == PlaybackState.STATE_PLAYING) {
            builder.addAction(
                createAction(
                    android.R.drawable.ic_media_pause, "Pause",
                    MediaButtonReceiver.buildMediaButtonPendingIntent(
                        MyApplication.context,
                        PlaybackStateCompat.ACTION_PAUSE
                    )
                )

            )
        }
        builder.addAction(
            createAction(
                android.R.drawable.ic_media_next, "Next",
                MediaButtonReceiver.buildMediaButtonPendingIntent(
                    MyApplication.context,
                    PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                )
            )

        )
        when (isOrder) {
            Constant.ORDER -> {
                builder.addAction(
                    createAction(
                        R.drawable.ic_order, "Order",
                        MediaButtonReceiver.buildMediaButtonPendingIntent(
                            MyApplication.context,
                            PlaybackStateCompat.ACTION_PLAY_PAUSE
                        )
                    )

                )
            }
            Constant.UNORDER -> {
                builder.addAction(
                    createAction(
                        R.drawable.ic_inorder, "unOrder",
                        MediaButtonReceiver.buildMediaButtonPendingIntent(
                            MyApplication.context,
                            PlaybackStateCompat.ACTION_PLAY_PAUSE
                    )

                    )
                )
            }
            Constant.ONEORDER -> {
                builder.addAction(
                    createAction(R.drawable.ic_oneorder, "oneOrder",
                        MediaButtonReceiver.buildMediaButtonPendingIntent(
                            MyApplication.context,
                            PlaybackStateCompat.ACTION_PLAY_PAUSE)
                    )
                )
            }
        }
        notification = builder.build()
        mNotificationManager.notify(1111, notification)
    }

    private fun createAction(icon:Int,title:String,intent: PendingIntent):Notification.Action{
        return Notification.Action.Builder(icon,title,intent).build()
    }


    private fun createMetadata() {

        Constant.songList[nowSong].let { song ->
            metadata = MediaMetadata.Builder().also {
                it.putString(MediaMetadata.METADATA_KEY_ARTIST, song.songArtist)//歌手
                it.putString(MediaMetadata.METADATA_KEY_TITLE, song.songTitle)//歌名
                it.putLong(MediaMetadata.METADATA_KEY_DURATION, song.duration)//总时间
                it.putString(MediaMetadata.METADATA_KEY_MEDIA_ID,nowSong.toString())//这里借用改变把当前歌曲下标传一传
                it.putString(MediaMetadata.METADATA_KEY_DISPLAY_ICON_URI,song.coverPath)//专辑图uri
            }.build()
            mMediaSession.setMetadata(metadata)
            metadatalist.add(metadata)
        }
    }
}
