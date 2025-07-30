package com.musicplayer.app.repository

import android.content.Context
import androidx.lifecycle.LiveData
import com.musicplayer.app.data.Song
import com.musicplayer.app.data.Playlist
import com.musicplayer.app.data.PlaylistSong
import com.musicplayer.app.database.MusicDatabase
import com.musicplayer.app.database.MusicDao
import com.musicplayer.app.utils.MusicScanner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MusicRepository(context: Context) {
    
    private val musicDao: MusicDao = MusicDatabase.getDatabase(context).musicDao()
    private val musicScanner = MusicScanner(context)
    
    // Song operations
    fun getAllSongs(): LiveData<List<Song>> = musicDao.getAllSongs()
    
    suspend fun refreshMusicLibrary() {
        withContext(Dispatchers.IO) {
            val scannedSongs = musicScanner.scanForMusic()
            musicDao.deleteAllSongs()
            musicDao.insertSongs(scannedSongs)
        }
    }
    
    suspend fun getSongById(id: Long): Song? = musicDao.getSongById(id)
    
    fun searchSongs(query: String): LiveData<List<Song>> = musicDao.searchSongs(query)
    
    fun getSongsByArtist(artist: String): LiveData<List<Song>> = musicDao.getSongsByArtist(artist)
    
    fun getSongsByAlbum(album: String): LiveData<List<Song>> = musicDao.getSongsByAlbum(album)
    
    fun getFavoriteSongs(): LiveData<List<Song>> = musicDao.getFavoriteSongs()
    
    fun getTopPlayedSongs(limit: Int = 20): LiveData<List<Song>> = musicDao.getTopPlayedSongs(limit)
    
    fun getRecentlyPlayedSongs(limit: Int = 20): LiveData<List<Song>> = musicDao.getRecentlyPlayedSongs(limit)
    
    suspend fun updateSong(song: Song) = musicDao.updateSong(song)
    
    suspend fun toggleFavorite(song: Song) {
        val updatedSong = song.copy(isFavorite = !song.isFavorite)
        musicDao.updateSong(updatedSong)
    }
    
    suspend fun incrementPlayCount(song: Song) {
        val updatedSong = song.copy(
            playCount = song.playCount + 1,
            lastPlayed = System.currentTimeMillis()
        )
        musicDao.updateSong(updatedSong)
    }
    
    // Playlist operations
    fun getAllPlaylists(): LiveData<List<Playlist>> = musicDao.getAllPlaylists()
    
    suspend fun getPlaylistById(id: Long): Playlist? = musicDao.getPlaylistById(id)
    
    suspend fun createPlaylist(name: String, description: String = ""): Long {
        val playlist = Playlist(
            name = name,
            description = description
        )
        return musicDao.insertPlaylist(playlist)
    }
    
    suspend fun updatePlaylist(playlist: Playlist) = musicDao.updatePlaylist(playlist)
    
    suspend fun deletePlaylist(playlist: Playlist) = musicDao.deletePlaylist(playlist)
    
    // Playlist-Song operations
    fun getPlaylistSongs(playlistId: Long): LiveData<List<Song>> = musicDao.getPlaylistSongs(playlistId)
    
    suspend fun addSongToPlaylist(playlistId: Long, songId: Long) {
        val playlistSong = PlaylistSong(
            playlistId = playlistId,
            songId = songId
        )
        musicDao.insertPlaylistSong(playlistSong)
    }
    
    suspend fun removeSongFromPlaylist(playlistId: Long, songId: Long) {
        musicDao.removePlaylistSong(playlistId, songId)
    }
    
    suspend fun clearPlaylist(playlistId: Long) = musicDao.clearPlaylist(playlistId)
    
    // Statistics
    fun getAllArtists(): LiveData<List<String>> = musicDao.getAllArtists()
    
    fun getAllAlbums(): LiveData<List<String>> = musicDao.getAllAlbums()
    
    fun getSongCount(): LiveData<Int> = musicDao.getSongCount()
    
    fun getTotalDuration(): LiveData<Long> = musicDao.getTotalDuration()
    
    // Album art
    suspend fun getAlbumArt(song: Song) = musicScanner.getAlbumArt(song)
}