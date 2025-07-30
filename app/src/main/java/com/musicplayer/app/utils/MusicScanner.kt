package com.musicplayer.app.utils

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.MediaStore
import android.util.Size
import com.musicplayer.app.data.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class MusicScanner(private val context: Context) {
    
    suspend fun scanForMusic(): List<Song> = withContext(Dispatchers.IO) {
        val songs = mutableListOf<Song>()
        val contentResolver = context.contentResolver
        
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.ARTIST_ID,
            MediaStore.Audio.Media.TRACK,
            MediaStore.Audio.Media.YEAR,
            MediaStore.Audio.Media.DATE_MODIFIED,
            MediaStore.Audio.Media.SIZE
        )
        
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} = 1 AND ${MediaStore.Audio.Media.DURATION} > 30000"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"
        
        val cursor: Cursor? = contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            null,
            sortOrder
        )
        
        cursor?.use { c ->
            val idColumn = c.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleColumn = c.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistColumn = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumColumn = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val durationColumn = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val dataColumn = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            val albumIdColumn = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
            val artistIdColumn = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST_ID)
            val trackColumn = c.getColumnIndexOrThrow(MediaStore.Audio.Media.TRACK)
            val yearColumn = c.getColumnIndexOrThrow(MediaStore.Audio.Media.YEAR)
            val dateModifiedColumn = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_MODIFIED)
            val sizeColumn = c.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
            
            while (c.moveToNext()) {
                try {
                    val id = c.getLong(idColumn)
                    val title = c.getString(titleColumn) ?: "Unknown Title"
                    val artist = c.getString(artistColumn) ?: "Unknown Artist"
                    val album = c.getString(albumColumn) ?: "Unknown Album"
                    val duration = c.getLong(durationColumn)
                    val path = c.getString(dataColumn) ?: continue
                    val albumId = c.getLong(albumIdColumn)
                    val artistId = c.getLong(artistIdColumn)
                    val track = c.getInt(trackColumn)
                    val year = c.getInt(yearColumn)
                    val dateModified = c.getLong(dateModifiedColumn)
                    val size = c.getLong(sizeColumn)
                    
                    // Verify file exists
                    if (File(path).exists()) {
                        val song = Song(
                            id = id,
                            title = title,
                            artist = artist,
                            album = album,
                            duration = duration,
                            path = path,
                            albumId = albumId,
                            artistId = artistId,
                            track = track,
                            year = year,
                            dateModified = dateModified,
                            size = size,
                            albumArt = getAlbumArtPath(albumId)
                        )
                        songs.add(song)
                    }
                } catch (e: Exception) {
                    // Skip corrupted entries
                    continue
                }
            }
        }
        
        songs
    }
    
    private fun getAlbumArtPath(albumId: Long): String? {
        return try {
            val uri = Uri.parse("content://media/external/audio/albumart")
            Uri.withAppendedPath(uri, albumId.toString()).toString()
        } catch (e: Exception) {
            null
        }
    }
    
    suspend fun getAlbumArt(song: Song, size: Size = Size(300, 300)): Bitmap? = withContext(Dispatchers.IO) {
        try {
            // Try to get album art from MediaStore first
            song.albumArt?.let { artPath ->
                try {
                    val uri = Uri.parse(artPath)
                    context.contentResolver.loadThumbnail(uri, size, null)
                } catch (e: Exception) {
                    null
                }
            } ?: run {
                // Fallback to extracting from file metadata
                getEmbeddedAlbumArt(song.path)
            }
        } catch (e: Exception) {
            null
        }
    }
    
    private fun getEmbeddedAlbumArt(filePath: String): Bitmap? {
        return try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(filePath)
            val art = retriever.embeddedPicture
            retriever.release()
            
            art?.let { BitmapFactory.decodeByteArray(it, 0, it.size) }
        } catch (e: Exception) {
            null
        }
    }
    
    suspend fun getAudioMetadata(filePath: String): Map<String, String?> = withContext(Dispatchers.IO) {
        val metadata = mutableMapOf<String, String?>()
        
        try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(filePath)
            
            metadata["title"] = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
            metadata["artist"] = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
            metadata["album"] = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)
            metadata["duration"] = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            metadata["genre"] = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE)
            metadata["year"] = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_YEAR)
            metadata["track"] = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CD_TRACK_NUMBER)
            
            retriever.release()
        } catch (e: Exception) {
            // Return empty metadata on error
        }
        
        metadata
    }
}