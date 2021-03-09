package com.linwei.mymusic.activity

import android.Manifest
import android.content.ComponentName
import android.content.Intent
import android.media.MediaMetadata
import android.media.browse.MediaBrowser
import android.media.session.MediaController
import android.media.session.PlaybackState

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.KeyEvent
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import com.linwei.mymusic.CircleProgressView
import com.linwei.mymusic.R
import com.linwei.mymusic.adapter.RecyclerViewPageChangeListenerHelper
import com.linwei.mymusic.adapter.SongAdapter
import com.linwei.mymusic.adapter.SongControllerAdapter
import com.linwei.mymusic.adapter.setSongItemOnClickListener
import com.linwei.mymusic.constant.Constant
import com.linwei.mymusic.fragment.PlayFragmentDialog
import com.linwei.mymusic.mediaservice.BrowserService
import com.linwei.mymusic.viewModel.MainVewMode
import com.yanzhenjie.permission.AndPermission
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private lateinit var mainViewModel: MainVewMode
    private lateinit var mMediaBrowser: MediaBrowser
    private var mMediaController: MediaController?=null
    val snapHelper=PagerSnapHelper()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        AndPermission.with(this).permission(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
            .onGranted {
                initView()
                mainViewModel.getSongList()
            }
            .onDenied {
                title = "权限没给足"
                Log.e("main",it.toString())
            }
            .start()


    }

    override fun onResume() {
        super.onResume()

    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(runable)
    }

    private fun initView() {
        val layoutManager = LinearLayoutManager(this)
        songRecyclerView.layoutManager = layoutManager
        //ContorllerRecy
        val layoutManagerController=LinearLayoutManager(this)
        layoutManagerController.orientation=LinearLayoutManager.HORIZONTAL
        ControllerRecycler.layoutManager=layoutManagerController

        snapHelper.attachToRecyclerView(ControllerRecycler)
        ControllerRecycler.addOnScrollListener(scollisenter)
        mMediaBrowser =
            MediaBrowser(this, ComponentName(this, BrowserService::class.java), callbacnk, null)
        mainViewModel = ViewModelProvider.AndroidViewModelFactory(application)
            .create(MainVewMode::class.java)
        mainViewModel.songList.observe(this, Observer{
            Log.e("main","")
            it.forEach { itt->
                println(itt)
            }
            Constant.songList = it
            val adapter = SongAdapter(it, object : setSongItemOnClickListener {
                override fun onClick(postion: Int) {
                    super.onClick(postion)
                    val bundle = Bundle()
                    bundle.putInt("postion", postion)
                    mMediaController?.transportControls?.sendCustomAction("postion", bundle)
                    ControllerRecycler.scrollToPosition(postion)
                }
            })
            songRecyclerView.adapter = adapter
            val adapter2 = SongControllerAdapter(it, object : setSongItemOnClickListener {
                override fun onClick(postion: Int) {
                    super.onClick(postion)
                    val playFragmentDialog=PlayFragmentDialog()
                    playFragmentDialog.show(supportFragmentManager,"dialog")
                }
            })
            ControllerRecycler.adapter=adapter2
            mMediaBrowser.connect()

        })
    }

    private val scollisenter=RecyclerViewPageChangeListenerHelper(snapHelper,object:RecyclerViewPageChangeListenerHelper.OnPageChangeListener{
        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)
            val bundle = Bundle()
            bundle.putInt("postion", position)
            mMediaController?.transportControls?.sendCustomAction("postion", bundle)
        }
    })


    private val callbacnk = object : MediaBrowser.ConnectionCallback() {

        override fun onConnected() {
            super.onConnected()
            //连接Service成功回调到这里
            Log.e("Main","Connect")
            mMediaBrowser.unsubscribe(Constant.Root_ID)
            mMediaBrowser.subscribe(Constant.Root_ID, subscribeCallback)
            mMediaController = MediaController(this@MainActivity, mMediaBrowser.sessionToken)
            Constant.mMediaController=mMediaController
            mMediaController!!.registerCallback(MeidaControllerCallback)
            playButton.onStatePlayListener=object:CircleProgressView.OnStatePlayListener{
                override fun toPlay() {
                    super.toPlay()
                    Log.e("main","开始播放")
                    if(mMediaController!=null){
                        mMediaController!!.transportControls.play()
                    }else{
                        playButton.setPlaying(false)
                    }
                }

                override fun toPause() {
                    super.toPause()
                    Log.e("main","暂停")
                    if(mMediaController!=null){
                        mMediaController!!.transportControls.pause()
                    }else{
                        playButton.setPlaying(false)
                    }
                }
            }
            handler.post(runable)
        }

        override fun onConnectionFailed() {
            super.onConnectionFailed()
            Toast.makeText(this@MainActivity, "Connection_Failed", Toast.LENGTH_LONG).show()
        }

    }

    private val subscribeCallback = object : MediaBrowser.SubscriptionCallback() {
        override fun onChildrenLoaded(
            parentId: String,
            children: MutableList<MediaBrowser.MediaItem>
        ) {
            super.onChildrenLoaded(parentId, children)
            //这里获取播放列表

        }
    }

    //控制器回调
    private val MeidaControllerCallback = object : MediaController.Callback() {

        override fun onPlaybackStateChanged(state: PlaybackState?) {
            super.onPlaybackStateChanged(state)
            playButton.setProgress(state!!.position.toFloat())

            when (state.state) {
                PlaybackState.STATE_PLAYING -> {
                    playButton.setPlaying(true)
                }
                PlaybackState.STATE_PAUSED -> {
                    playButton.setPlaying(false)
                }
            }

        }

        override fun onMetadataChanged(metadata: MediaMetadata?) {
            super.onMetadataChanged(metadata)
            val description=metadata!!.description
            playButton.setProgress(0f)
            playButton.setMaxProgress(Constant.mMediaPlayer.duration.toFloat())
//            titleText.text="${description.title}-${description.subtitle}"
            ControllerRecycler.scrollToPosition(description.mediaId!!.toInt())
            //imageView.setImageBitmap(description.iconBitmap)
        }

    }
    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            val intent = Intent(Intent.ACTION_MAIN)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            intent.addCategory(Intent.CATEGORY_HOME)
            startActivity(intent)
            return true
        }

        return super.onKeyDown(keyCode, event)
    }


    val handler=Handler()
    val runable=object:Runnable{
        override fun run() {
            if (Constant.mMediaPlayer.isPlaying){
                playButton.setProgress(Constant.mMediaPlayer.currentPosition.toFloat())
                //handler.postDelayed(this,100L)
            }
            handler.postDelayed(this,100L)

        }
    }

}