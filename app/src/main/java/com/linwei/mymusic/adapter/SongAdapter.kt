package com.linwei.mymusic.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.linwei.mymusic.R
import com.linwei.mymusic.data.Song
import kotlinx.android.synthetic.main.item_song_layout.view.*

class SongAdapter(private val songlist:List<Song>, private val onClickListener: setSongItemOnClickListener):RecyclerView.Adapter<SongAdapter.SongViewHolder>(){


    inner class SongViewHolder(view:View):RecyclerView.ViewHolder(view){


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val context=parent.context
        val view=LayoutInflater.from(context).inflate(R.layout.item_song_layout,parent,false)
        return SongViewHolder(view)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val mediasong=songlist[position]
        holder.itemView.songNameText.text=mediasong.songTitle
        holder.itemView.artistNameText.text=mediasong.songAlbum
        val bitmap=Glide.with(holder.itemView.context)
            .asBitmap()
            .load(mediasong.coverPath)
            .thumbnail(0.33f)
            .centerCrop()
            .error(R.drawable.music_icon)
            .into(holder.itemView.songCoverImage)
        holder.itemView.setOnClickListener {
            onClickListener.onClick(position)
        }

    }

    override fun getItemCount(): Int =songlist.size
}