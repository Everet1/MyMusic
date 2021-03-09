package com.linwei.mymusic.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.linwei.mymusic.R

class SonglyricFragment:Fragment() {
    private lateinit var v:View
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        v=inflater.inflate(R.layout.lyric_activity,null)

        return v
    }






}