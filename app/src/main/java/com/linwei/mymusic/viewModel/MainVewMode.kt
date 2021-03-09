package com.linwei.mymusic.viewModel

import android.content.ContentResolver
import android.content.ContentUris
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.media.MediaMetadata
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.MediaStore
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.linwei.mymusic.MyApplication.Companion.context
import com.linwei.mymusic.R
import com.linwei.mymusic.data.Song
import kotlinx.android.synthetic.main.item_song_layout.view.*
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.File
import java.io.IOException
import java.io.InputStream
import kotlin.concurrent.thread

class MainVewMode : ViewModel() {

    val songList: LiveData<List<Song>>
        get() = _songList
    val metadataList: LiveData<List<MediaMetadata>>
        get() = _metadataList

    private val _songList = MutableLiveData<List<Song>>()
    private val _metadataList = MutableLiveData<List<MediaMetadata>>()

    fun getSongList() {
        thread {
            val cursor = context.contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                null,
                null,
                null,
                MediaStore.Audio.AudioColumns.IS_MUSIC
            )!!
            val list = ArrayList<Song>()
            if (cursor.moveToFirst()) {
                do {

                    val songid =
                        cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID))
                    val songtitle =
                        cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE))
                    val songartist =
                        cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST))
                    val songalbum =
                        cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM))
                    val songuri =
                        cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA))
                    val songalbumId =
                        cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID))
                    val coverUri = getAlbumCoverPathFromAlbumId(
                        context.contentResolver,
                        songalbumId
                    )
                    val contentUri: Uri =
                        ContentUris.withAppendedId(
                            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                            songid
                        )
                    val duration =
                        cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION))
                    val song = Song(
                        songid,
                        songtitle,
                        songartist,
                        songalbum,
                        songuri,
                        songalbumId,
                        coverUri,
                        contentUri,
                        duration
                    )
                    if (duration != 0L) list.add(song)
                } while (cursor.moveToNext())
            }
            cursor.close()
            _songList.postValue(list)
        }

    }

    private fun getAlbumCoverPathFromAlbumId(
        contentResolver: ContentResolver,
        albumId: Long
    ): String {
        val albumArtUri =
            Uri.parse("content://media/external/audio/albumart")
        val coverUri = ContentUris.withAppendedId(albumArtUri, albumId)

        return try {
            val inputStream: InputStream? = contentResolver.openInputStream(coverUri)
            inputStream?.close()
            coverUri.toString()
        } catch (e: IOException) {
            ""
        } catch (e: IllegalStateException) {
            ""
        }
    }

//    fun createMetadata() {
//        thread {
//            val metadatalist = ArrayList<MediaMetadata>()
//            songList.value?.forEach { song ->
//                val metadata = MediaMetadata.Builder().also {
//                    it.putString(MediaMetadata.METADATA_KEY_MEDIA_ID, song.songID.toString())
//                    it.putString(MediaMetadata.METADATA_KEY_ALBUM, song.songAlbum)
//                    it.putString(MediaMetadata.METADATA_KEY_ARTIST, song.songArtist)
//                    it.putString(MediaMetadata.METADATA_KEY_TITLE, song.songTitle)
//                    it.putString(MediaMetadata.METADATA_KEY_MEDIA_URI, song.contentUri.toString())
//                    it.putString(MediaMetadata.METADATA_KEY_DISPLAY_TITLE, song.songTitle)
//                    it.putString(MediaMetadata.METADATA_KEY_DISPLAY_SUBTITLE, song.songArtist)
//                    it.putString(MediaMetadata.METADATA_KEY_DISPLAY_DESCRIPTION, song.songAlbum)
//                    it.putString(MediaMetadata.METADATA_KEY_ALBUM_ART_URI, song.coverPath)
//                    it.putLong(MediaMetadata.METADATA_KEY_DURATION, song.duration)
////                it.putBitmap(
////                    MediaMetadata.METADATA_KEY_DISPLAY_ICON,
////                   null
////                )
//                }.build()
//                metadatalist.add(metadata)
//            }
//            _metadataList.postValue(metadatalist)
//        }
//
//    }

}