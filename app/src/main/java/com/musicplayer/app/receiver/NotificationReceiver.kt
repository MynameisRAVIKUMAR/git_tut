package com.musicplayer.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.musicplayer.app.service.MusicService

class NotificationReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context?, intent: Intent?) {
        val action = intent?.action ?: return
        
        // Create intent to communicate with MusicService
        val serviceIntent = Intent(context, MusicService::class.java).apply {
            this.action = action
        }
        
        when (action) {
            MusicService.ACTION_PLAY,
            MusicService.ACTION_PAUSE,
            MusicService.ACTION_NEXT,
            MusicService.ACTION_PREVIOUS,
            MusicService.ACTION_STOP -> {
                context?.startService(serviceIntent)
            }
        }
    }
}