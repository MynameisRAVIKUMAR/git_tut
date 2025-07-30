package com.musicplayer.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.musicplayer.app.data.Song
import com.musicplayer.app.data.Playlist
import com.musicplayer.app.repository.MusicRepository
import kotlinx.coroutines.launch

class MusicViewModel(private val repository: MusicRepository) : ViewModel() {
    
    // LiveData for UI
    val allSongs: LiveData<List<Song>> = repository.getAllSongs()
    val allPlaylists: LiveData<List<Playlist>> = repository.getAllPlaylists()
    val allArtists: LiveData<List<String>> = repository.getAllArtists()
    val allAlbums: LiveData<List<String>> = repository.getAllAlbums()
    val favoriteSongs: LiveData<List<Song>> = repository.getFavoriteSongs()
    val recentlyPlayedSongs: LiveData<List<Song>> = repository.getRecentlyPlayedSongs()
    val topPlayedSongs: LiveData<List<Song>> = repository.getTopPlayedSongs()
    
    // Current playback state
    private val _currentSong = MutableLiveData<Song?>()
    val currentSong: LiveData<Song?> = _currentSong
    
    private val _isPlaying = MutableLiveData<Boolean>()
    val isPlaying: LiveData<Boolean> = _isPlaying
    
    private val _currentPosition = MutableLiveData<Long>()
    val currentPosition: LiveData<Long> = _currentPosition
    
    private val _duration = MutableLiveData<Long>()
    val duration: LiveData<Long> = _duration
    
    private val _shuffleMode = MutableLiveData<Boolean>()
    val shuffleMode: LiveData<Boolean> = _shuffleMode
    
    private val _repeatMode = MutableLiveData<Int>() // 0: None, 1: One, 2: All
    val repeatMode: LiveData<Int> = _repeatMode
    
    // Current playlist
    private val _currentPlaylist = MutableLiveData<List<Song>>()
    val currentPlaylist: LiveData<List<Song>> = _currentPlaylist
    
    private val _currentIndex = MutableLiveData<Int>()
    val currentIndex: LiveData<Int> = _currentIndex
    
    // Search results
    private val _searchResults = MutableLiveData<List<Song>>()
    val searchResults: LiveData<List<Song>> = _searchResults
    
    // Loading state
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    init {
        _isPlaying.value = false
        _currentPosition.value = 0L
        _duration.value = 0L
        _shuffleMode.value = false
        _repeatMode.value = 0
        _currentIndex.value = 0
        _isLoading.value = false
    }
    
    fun refreshMusicLibrary() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.refreshMusicLibrary()
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun playSong(song: Song, playlist: List<Song> = listOf(song)) {
        _currentSong.value = song
        _currentPlaylist.value = playlist
        _currentIndex.value = playlist.indexOf(song)
        _isPlaying.value = true
        
        // Update play count
        viewModelScope.launch {
            repository.incrementPlayCount(song)
        }
    }
    
    fun playPlaylist(songs: List<Song>, startIndex: Int = 0) {
        if (songs.isNotEmpty() && startIndex in songs.indices) {
            _currentPlaylist.value = songs
            _currentIndex.value = startIndex
            _currentSong.value = songs[startIndex]
            _isPlaying.value = true
            
            // Update play count
            viewModelScope.launch {
                repository.incrementPlayCount(songs[startIndex])
            }
        }
    }
    
    fun togglePlayPause() {
        _isPlaying.value = !(_isPlaying.value ?: false)
    }
    
    fun play() {
        _isPlaying.value = true
    }
    
    fun pause() {
        _isPlaying.value = false
    }
    
    fun playNext() {
        val playlist = _currentPlaylist.value ?: return
        val currentIdx = _currentIndex.value ?: 0
        
        val nextIndex = if (_shuffleMode.value == true) {
            playlist.indices.random()
        } else {
            (currentIdx + 1) % playlist.size
        }
        
        if (nextIndex < playlist.size) {
            _currentIndex.value = nextIndex
            _currentSong.value = playlist[nextIndex]
            
            viewModelScope.launch {
                repository.incrementPlayCount(playlist[nextIndex])
            }
        }
    }
    
    fun playPrevious() {
        val playlist = _currentPlaylist.value ?: return
        val currentIdx = _currentIndex.value ?: 0
        
        val previousIndex = if (currentIdx > 0) currentIdx - 1 else playlist.size - 1
        
        if (previousIndex >= 0) {
            _currentIndex.value = previousIndex
            _currentSong.value = playlist[previousIndex]
            
            viewModelScope.launch {
                repository.incrementPlayCount(playlist[previousIndex])
            }
        }
    }
    
    fun seekTo(position: Long) {
        _currentPosition.value = position
    }
    
    fun updatePosition(position: Long) {
        _currentPosition.value = position
    }
    
    fun updateDuration(duration: Long) {
        _duration.value = duration
    }
    
    fun toggleShuffle() {
        _shuffleMode.value = !(_shuffleMode.value ?: false)
    }
    
    fun toggleRepeat() {
        val currentMode = _repeatMode.value ?: 0
        _repeatMode.value = (currentMode + 1) % 3
    }
    
    fun toggleFavorite(song: Song) {
        viewModelScope.launch {
            repository.toggleFavorite(song)
        }
    }
    
    fun searchSongs(query: String) {
        if (query.isBlank()) {
            _searchResults.value = emptyList()
            return
        }
        
        viewModelScope.launch {
            repository.searchSongs(query).observeForever { songs ->
                _searchResults.value = songs
            }
        }
    }
    
    fun getSongsByArtist(artist: String): LiveData<List<Song>> {
        return repository.getSongsByArtist(artist)
    }
    
    fun getSongsByAlbum(album: String): LiveData<List<Song>> {
        return repository.getSongsByAlbum(album)
    }
    
    fun createPlaylist(name: String, description: String = "") {
        viewModelScope.launch {
            repository.createPlaylist(name, description)
        }
    }
    
    fun addSongToPlaylist(playlistId: Long, songId: Long) {
        viewModelScope.launch {
            repository.addSongToPlaylist(playlistId, songId)
        }
    }
    
    fun removeSongFromPlaylist(playlistId: Long, songId: Long) {
        viewModelScope.launch {
            repository.removeSongFromPlaylist(playlistId, songId)
        }
    }
    
    fun getPlaylistSongs(playlistId: Long): LiveData<List<Song>> {
        return repository.getPlaylistSongs(playlistId)
    }
    
    fun deletePlaylist(playlist: Playlist) {
        viewModelScope.launch {
            repository.deletePlaylist(playlist)
        }
    }
}

class MusicViewModelFactory(private val repository: MusicRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MusicViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MusicViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}