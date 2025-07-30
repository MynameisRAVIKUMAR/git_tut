package com.musicplayer.app.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.musicplayer.app.R
import com.musicplayer.app.data.Song
import com.musicplayer.app.databinding.ItemSongBinding

class SongAdapter(
    private val onSongClick: (Song) -> Unit
) : ListAdapter<Song, SongAdapter.SongViewHolder>(SongDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val binding = ItemSongBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SongViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    inner class SongViewHolder(
        private val binding: ItemSongBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(song: Song) {
            binding.apply {
                titleText.text = song.title
                artistText.text = song.getDisplayArtist()
                durationText.text = song.getFormattedDuration()
                
                // Load album art
                Glide.with(albumArt.context)
                    .load(song.albumArt)
                    .placeholder(R.drawable.ic_music_note_24)
                    .error(R.drawable.ic_music_note_24)
                    .into(albumArt)
                
                root.setOnClickListener {
                    onSongClick(song)
                }
                
                // Add favorite indicator
                favoriteIndicator.visibility = if (song.isFavorite) {
                    android.view.View.VISIBLE
                } else {
                    android.view.View.GONE
                }
            }
        }
    }
    
    private class SongDiffCallback : DiffUtil.ItemCallback<Song>() {
        override fun areItemsTheSame(oldItem: Song, newItem: Song): Boolean {
            return oldItem.id == newItem.id
        }
        
        override fun areContentsTheSame(oldItem: Song, newItem: Song): Boolean {
            return oldItem == newItem
        }
    }
}