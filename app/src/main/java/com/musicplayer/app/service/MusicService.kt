package com.musicplayer.app.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.media.AudioManager
import android.os.*
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.NotificationCompat
import androidx.media.MediaBrowserServiceCompat
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.musicplayer.app.R
import com.musicplayer.app.data.Song
import com.musicplayer.app.receiver.NotificationReceiver
import com.musicplayer.app.ui.MainActivity
import kotlinx.coroutines.*

class MusicService : MediaBrowserServiceCompat() {
    
    companion object {
        const val CHANNEL_ID = "music_playback_channel"
        const val NOTIFICATION_ID = 1
        
        const val ACTION_PLAY = "action_play"
        const val ACTION_PAUSE = "action_pause"
        const val ACTION_NEXT = "action_next"
        const val ACTION_PREVIOUS = "action_previous"
        const val ACTION_STOP = "action_stop"
    }
    
    private lateinit var exoPlayer: SimpleExoPlayer
    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var mediaSessionConnector: MediaSessionConnector
    private lateinit var notificationManager: NotificationManager
    
    private var currentSong: Song? = null
    private var playlist: MutableList<Song> = mutableListOf()
    private var currentIndex = 0
    private var isShuffleEnabled = false
    private var repeatMode = RepeatMode.NONE
    
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    enum class RepeatMode {
        NONE, ONE, ALL
    }
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize ExoPlayer
        initializePlayer()
        
        // Initialize MediaSession
        initializeMediaSession()
        
        // Create notification channel
        createNotificationChannel()
    }
    
    private fun initializePlayer() {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.CONTENT_TYPE_MUSIC)
            .build()
            
        exoPlayer = SimpleExoPlayer.Builder(this)
            .setAudioAttributes(audioAttributes, true)
            .setHandleAudioBecomingNoisy(true)
            .build()
            
        exoPlayer.addListener(object : Player.EventListener {
            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                when (playbackState) {
                    Player.STATE_ENDED -> {
                        when (repeatMode) {
                            RepeatMode.ONE -> exoPlayer.seekTo(0)
                            RepeatMode.ALL -> playNext()
                            RepeatMode.NONE -> {
                                if (currentIndex < playlist.size - 1) {
                                    playNext()
                                }
                            }
                        }
                    }
                }
                updateNotification()
            }
            
            override fun onPlayerError(error: ExoPlaybackException) {
                // Handle playback errors
                playNext()
            }
        })
    }
    
    private fun initializeMediaSession() {
        mediaSession = MediaSessionCompat(this, "MusicService")
        mediaSession.setFlags(
            MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or
                    MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS
        )
        
        mediaSession.setCallback(object : MediaSessionCompat.Callback() {
            override fun onPlay() {
                play()
            }
            
            override fun onPause() {
                pause()
            }
            
            override fun onSkipToNext() {
                playNext()
            }
            
            override fun onSkipToPrevious() {
                playPrevious()
            }
            
            override fun onStop() {
                stopSelf()
            }
            
            override fun onSeekTo(pos: Long) {
                exoPlayer.seekTo(pos)
            }
        })
        
        sessionToken = mediaSession.sessionToken
        
        mediaSessionConnector = MediaSessionConnector(mediaSession)
        mediaSessionConnector.setPlayer(exoPlayer)
    }
    
    private fun createNotificationChannel() {
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Music Playback",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Music playback controls"
                setShowBadge(false)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    fun playPlaylist(songs: List<Song>, startIndex: Int = 0) {
        playlist.clear()
        playlist.addAll(songs)
        currentIndex = startIndex.coerceIn(0, playlist.size - 1)
        
        if (playlist.isNotEmpty()) {
            playSong(playlist[currentIndex])
        }
    }
    
    fun playSong(song: Song) {
        currentSong = song
        
        val dataSourceFactory = DefaultDataSourceFactory(this, "MusicPlayer")
        val mediaSource: MediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
            .createMediaSource(MediaItem.fromUri(song.path))
            
        exoPlayer.setMediaSource(mediaSource)
        exoPlayer.prepare()
        exoPlayer.playWhenReady = true
        
        updateMediaMetadata(song)
        updateNotification()
        
        startForeground(NOTIFICATION_ID, createNotification())
    }
    
    fun play() {
        exoPlayer.playWhenReady = true
        updateNotification()
    }
    
    fun pause() {
        exoPlayer.playWhenReady = false
        updateNotification()
    }
    
    fun playNext() {
        if (playlist.isNotEmpty()) {
            currentIndex = if (isShuffleEnabled) {
                playlist.indices.random()
            } else {
                (currentIndex + 1) % playlist.size
            }
            playSong(playlist[currentIndex])
        }
    }
    
    fun playPrevious() {
        if (playlist.isNotEmpty()) {
            currentIndex = if (currentIndex > 0) currentIndex - 1 else playlist.size - 1
            playSong(playlist[currentIndex])
        }
    }
    
    fun seekTo(position: Long) {
        exoPlayer.seekTo(position)
    }
    
    fun setShuffleMode(enabled: Boolean) {
        isShuffleEnabled = enabled
    }
    
    fun setRepeatMode(mode: RepeatMode) {
        repeatMode = mode
    }
    
    private fun updateMediaMetadata(song: Song) {
        val metadata = MediaMetadataCompat.Builder()
            .putString(MediaMetadataCompat.METADATA_KEY_TITLE, song.title)
            .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, song.getDisplayArtist())
            .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, song.getDisplayAlbum())
            .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, song.duration)
            .build()
            
        mediaSession.setMetadata(metadata)
    }
    
    private fun createNotification(): Notification {
        val song = currentSong ?: return createEmptyNotification()
        
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val playPauseAction = if (exoPlayer.isPlaying) {
            NotificationCompat.Action(
                R.drawable.ic_pause_24,
                "Pause",
                getActionPendingIntent(ACTION_PAUSE)
            )
        } else {
            NotificationCompat.Action(
                R.drawable.ic_play_arrow_24,
                "Play",
                getActionPendingIntent(ACTION_PLAY)
            )
        }
        
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(song.title)
            .setContentText(song.getDisplayArtist())
            .setSubText(song.getDisplayAlbum())
            .setSmallIcon(R.drawable.ic_music_note_24)
            .setContentIntent(pendingIntent)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOnlyAlertOnce(true)
            .addAction(
                R.drawable.ic_skip_previous_24,
                "Previous",
                getActionPendingIntent(ACTION_PREVIOUS)
            )
            .addAction(playPauseAction)
            .addAction(
                R.drawable.ic_skip_next_24,
                "Next",
                getActionPendingIntent(ACTION_NEXT)
            )
            .setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setMediaSession(mediaSession.sessionToken)
                    .setShowActionsInCompactView(0, 1, 2)
            )
            .build()
    }
    
    private fun createEmptyNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Music Player")
            .setContentText("No song playing")
            .setSmallIcon(R.drawable.ic_music_note_24)
            .build()
    }
    
    private fun getActionPendingIntent(action: String): PendingIntent {
        val intent = Intent(this, NotificationReceiver::class.java).apply {
            this.action = action
        }
        return PendingIntent.getBroadcast(
            this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
    
    private fun updateNotification() {
        if (::notificationManager.isInitialized) {
            notificationManager.notify(NOTIFICATION_ID, createNotification())
        }
    }
    
    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot? {
        return BrowserRoot("root", null)
    }
    
    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {
        result.sendResult(mutableListOf())
    }
    
    override fun onDestroy() {
        serviceScope.cancel()
        exoPlayer.release()
        mediaSession.release()
        super.onDestroy()
    }
    
    inner class MusicBinder : Binder() {
        fun getService(): MusicService = this@MusicService
    }
    
    override fun onBind(intent: Intent?): IBinder {
        return MusicBinder()
    }
}