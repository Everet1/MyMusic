package com.linwei.mymusic.data

import android.graphics.Bitmap
import android.net.Uri


data class Song(
    val songID: Long,
    val songTitle: String,
    val songArtist: String,
    val songAlbum: String,
    val SongURI: String,
    val songAlbumID: Long,
    val coverPath:String,
    val contentUri:Uri,
    val duration: Long
)
