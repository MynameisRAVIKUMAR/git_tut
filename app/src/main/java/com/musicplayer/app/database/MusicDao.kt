package com.musicplayer.app.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.musicplayer.app.data.Song
import com.musicplayer.app.data.Playlist
import com.musicplayer.app.data.PlaylistSong

@Dao
interface MusicDao {
    
    // Song operations
    @Query("SELECT * FROM songs ORDER BY title ASC")
    fun getAllSongs(): LiveData<List<Song>>
    
    @Query("SELECT * FROM songs WHERE id = :id")
    suspend fun getSongById(id: Long): Song?
    
    @Query("SELECT * FROM songs WHERE title LIKE '%' || :query || '%' OR artist LIKE '%' || :query || '%' OR album LIKE '%' || :query || '%'")
    fun searchSongs(query: String): LiveData<List<Song>>
    
    @Query("SELECT * FROM songs WHERE artist = :artist ORDER BY album, track")
    fun getSongsByArtist(artist: String): LiveData<List<Song>>
    
    @Query("SELECT * FROM songs WHERE album = :album ORDER BY track")
    fun getSongsByAlbum(album: String): LiveData<List<Song>>
    
    @Query("SELECT * FROM songs WHERE isFavorite = 1 ORDER BY lastPlayed DESC")
    fun getFavoriteSongs(): LiveData<List<Song>>
    
    @Query("SELECT * FROM songs ORDER BY playCount DESC LIMIT :limit")
    fun getTopPlayedSongs(limit: Int): LiveData<List<Song>>
    
    @Query("SELECT * FROM songs ORDER BY lastPlayed DESC LIMIT :limit")
    fun getRecentlyPlayedSongs(limit: Int): LiveData<List<Song>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSongs(songs: List<Song>)
    
    @Update
    suspend fun updateSong(song: Song)
    
    @Delete
    suspend fun deleteSong(song: Song)
    
    @Query("DELETE FROM songs")
    suspend fun deleteAllSongs()
    
    // Playlist operations
    @Query("SELECT * FROM playlists ORDER BY name ASC")
    fun getAllPlaylists(): LiveData<List<Playlist>>
    
    @Query("SELECT * FROM playlists WHERE id = :id")
    suspend fun getPlaylistById(id: Long): Playlist?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: Playlist): Long
    
    @Update
    suspend fun updatePlaylist(playlist: Playlist)
    
    @Delete
    suspend fun deletePlaylist(playlist: Playlist)
    
    // Playlist-Song operations
    @Query("SELECT songs.* FROM songs INNER JOIN playlist_songs ON songs.id = playlist_songs.songId WHERE playlist_songs.playlistId = :playlistId ORDER BY playlist_songs.position")
    fun getPlaylistSongs(playlistId: Long): LiveData<List<Song>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylistSong(playlistSong: PlaylistSong)
    
    @Query("DELETE FROM playlist_songs WHERE playlistId = :playlistId AND songId = :songId")
    suspend fun removePlaylistSong(playlistId: Long, songId: Long)
    
    @Query("DELETE FROM playlist_songs WHERE playlistId = :playlistId")
    suspend fun clearPlaylist(playlistId: Long)
    
    // Statistics
    @Query("SELECT DISTINCT artist FROM songs WHERE artist != '' ORDER BY artist")
    fun getAllArtists(): LiveData<List<String>>
    
    @Query("SELECT DISTINCT album FROM songs WHERE album != '' ORDER BY album")
    fun getAllAlbums(): LiveData<List<String>>
    
    @Query("SELECT COUNT(*) FROM songs")
    fun getSongCount(): LiveData<Int>
    
    @Query("SELECT SUM(duration) FROM songs")
    fun getTotalDuration(): LiveData<Long>
}