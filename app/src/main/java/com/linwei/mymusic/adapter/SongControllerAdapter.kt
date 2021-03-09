package com.linwei.mymusic.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.linwei.mymusic.MyApplication
import com.linwei.mymusic.R
import com.linwei.mymusic.data.Song
import kotlinx.android.synthetic.main.item_song_layout2.view.*

class SongControllerAdapter(
    private val list: List<Song>,
    private val onClickListener: setSongItemOnClickListener
) : RecyclerView.Adapter<SongControllerAdapter.SongControllerHodler>() {


    inner class SongControllerHodler(view: View) : RecyclerView.ViewHolder(view) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongControllerHodler {
        return SongControllerHodler(
            LayoutInflater.from(parent.context).inflate(R.layout.item_song_layout2, parent, false)
        )
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: SongControllerHodler, position: Int) {
        val song=list[position]
        onClickListener.nowItem(position)
        holder.itemView.songName_SongArt.text="${song.songTitle} - ${song.songArtist}"
        Glide.with(MyApplication.context)
            .asBitmap()
            .load(song.coverPath)
            .error(R.drawable.music_icon)
            .into(holder.itemView.imageView)
        holder.itemView.setOnClickListener {
            onClickListener.onClick(position)
        }
    }

    override fun getItemCount(): Int = list.size


}