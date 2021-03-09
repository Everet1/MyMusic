package com.linwei.mymusic.broadcast

import android.content.Context
import android.content.Intent
import android.view.KeyEvent
import androidx.media.session.MediaButtonReceiver
import com.linwei.mymusic.constant.Constant.mMediaController

class MyMediaButtonReceiver : MediaButtonReceiver() {


    override fun onReceive(context: Context?, intent: Intent?) {
        val action = intent?.action ?: return
        when (action) {
            Intent.ACTION_MEDIA_BUTTON -> {
                val keyEvent =intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT) as? KeyEvent ?: return
                when (keyEvent.action) {
                    KeyEvent.ACTION_DOWN -> {//按键按下
                        when (keyEvent.keyCode) {
                            KeyEvent.KEYCODE_MEDIA_PLAY->{//播放按钮
                                //处理你的逻辑
                                mMediaController?.transportControls?.play()
                            }
                            KeyEvent.KEYCODE_MEDIA_PAUSE->{
                                mMediaController?.transportControls?.pause()

                            }
                            KeyEvent.KEYCODE_MEDIA_NEXT->{//下一首
                                //处理你的逻辑
                                mMediaController?.transportControls?.skipToNext()
                            }
                            KeyEvent.KEYCODE_MEDIA_PREVIOUS->{//上一首
                                //处理你的逻辑
                                mMediaController?.transportControls?.skipToPrevious()
                            }
                            KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE->{
                                //是否顺序播放
                                mMediaController?.transportControls?.sendCustomAction("PlayMode",null)
                            }
                        }
                    }
                }

            }
        }


    }
}
