package com.musicplayer.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "songs")
data class Song(
    @PrimaryKey val id: Long = 0L,
    val title: String = "",
    val artist: String = "",
    val album: String = "",
    val duration: Long = 0L,
    val path: String = "",
    val albumId: Long = 0L,
    val artistId: Long = 0L,
    val track: Int = 0,
    val year: Int = 0,
    val dateModified: Long = 0L,
    val albumArt: String? = null,
    val size: Long = 0L,
    var isFavorite: Boolean = false,
    var playCount: Int = 0,
    var lastPlayed: Long = 0L
) : Parcelable {
    
    fun getFormattedDuration(): String {
        val minutes = (duration / 1000) / 60
        val seconds = (duration / 1000) % 60
        return String.format("%d:%02d", minutes, seconds)
    }
    
    fun getDisplayArtist(): String {
        return if (artist.isNotEmpty() && artist != "<unknown>") artist else "Unknown Artist"
    }
    
    fun getDisplayAlbum(): String {
        return if (album.isNotEmpty() && album != "<unknown>") album else "Unknown Album"
    }
}