package com.musicplayer.app.ui

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.musicplayer.app.R
import com.musicplayer.app.databinding.ActivityMainBinding
import com.musicplayer.app.service.MusicService
import com.musicplayer.app.viewmodel.MusicViewModel
import com.musicplayer.app.viewmodel.MusicViewModelFactory
import com.musicplayer.app.repository.MusicRepository
import com.bumptech.glide.Glide
import android.Manifest
import android.os.Build

class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private lateinit var musicViewModel: MusicViewModel
    private var musicService: MusicService? = null
    private var isBound = false
    
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as MusicService.MusicBinder
            musicService = binder.getService()
            isBound = true
        }
        
        override fun onServiceDisconnected(name: ComponentName?) {
            musicService = null
            isBound = false
        }
    }
    
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.all { it.value }) {
            initializeMusic()
        } else {
            // Handle permission denied
            finish()
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Apply edge-to-edge display
        setupEdgeToEdge()
        
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Initialize ViewModel
        initializeViewModel()
        
        // Setup navigation
        setupNavigation()
        
        // Request permissions
        requestPermissions()
        
        // Setup mini player
        setupMiniPlayer()
        
        // Setup search
        setupSearch()
    }
    
    private fun setupEdgeToEdge() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
    
    private fun initializeViewModel() {
        val repository = MusicRepository(this)
        val factory = MusicViewModelFactory(repository)
        musicViewModel = ViewModelProvider(this, factory)[MusicViewModel::class.java]
    }
    
    private fun setupNavigation() {
        val navController = findNavController(R.id.nav_host_fragment)
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_songs,
                R.id.navigation_albums,
                R.id.navigation_artists,
                R.id.navigation_playlists
            )
        )
        
        setSupportActionBar(binding.toolbar)
        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.bottomNavigation.setupWithNavController(navController)
    }
    
    private fun requestPermissions() {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.READ_MEDIA_AUDIO,
                Manifest.permission.POST_NOTIFICATIONS
            )
        } else {
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        }
        
        Dexter.withContext(this)
            .withPermissions(*permissions)
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                    if (report?.areAllPermissionsGranted() == true) {
                        initializeMusic()
                    } else {
                        // Show permission explanation dialog
                        finish()
                    }
                }
                
                override fun onPermissionRationaleShouldBeShown(
                    permissions: MutableList<PermissionRequest>?,
                    token: PermissionToken?
                ) {
                    token?.continuePermissionRequest()
                }
            })
            .check()
    }
    
    private fun initializeMusic() {
        // Start music service
        val serviceIntent = Intent(this, MusicService::class.java)
        startService(serviceIntent)
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE)
        
        // Refresh music library
        musicViewModel.refreshMusicLibrary()
    }
    
    private fun setupMiniPlayer() {
        musicViewModel.currentSong.observe(this) { song ->
            if (song != null) {
                binding.miniPlayer.visibility = View.VISIBLE
                binding.miniPlayerTitle.text = song.title
                binding.miniPlayerArtist.text = song.getDisplayArtist()
                
                // Load album art
                Glide.with(this)
                    .load(song.albumArt)
                    .placeholder(R.drawable.ic_music_note_24)
                    .error(R.drawable.ic_music_note_24)
                    .into(binding.miniPlayerAlbumArt)
            } else {
                binding.miniPlayer.visibility = View.GONE
            }
        }
        
        musicViewModel.isPlaying.observe(this) { isPlaying ->
            val iconRes = if (isPlaying) R.drawable.ic_pause_24 else R.drawable.ic_play_arrow_24
            binding.miniPlayerPlayPause.setIconResource(iconRes)
        }
        
        binding.miniPlayerPlayPause.setOnClickListener {
            musicViewModel.togglePlayPause()
        }
        
        binding.miniPlayer.setOnClickListener {
            // Navigate to full player
            findNavController(R.id.nav_host_fragment).navigate(R.id.action_to_player)
        }
    }
    
    private fun setupSearch() {
        binding.searchBar.setOnClickListener {
            // Navigate to search fragment
            findNavController(R.id.nav_host_fragment).navigate(R.id.action_to_search)
        }
    }
    
    override fun onStart() {
        super.onStart()
        if (!isBound) {
            val serviceIntent = Intent(this, MusicService::class.java)
            bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE)
        }
    }
    
    override fun onStop() {
        super.onStop()
        if (isBound) {
            unbindService(serviceConnection)
            isBound = false
        }
    }
    
    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}