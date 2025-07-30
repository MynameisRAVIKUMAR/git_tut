package com.musicplayer.app.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.musicplayer.app.databinding.FragmentSongsBinding
import com.musicplayer.app.viewmodel.MusicViewModel
import com.musicplayer.app.ui.adapters.SongAdapter

class SongsFragment : Fragment() {
    
    private var _binding: FragmentSongsBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var musicViewModel: MusicViewModel
    private lateinit var songAdapter: SongAdapter
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSongsBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Get ViewModel from activity
        musicViewModel = ViewModelProvider(requireActivity())[MusicViewModel::class.java]
        
        setupRecyclerView()
        observeData()
    }
    
    private fun setupRecyclerView() {
        songAdapter = SongAdapter { song ->
            // Play song when clicked
            musicViewModel.playSong(song, musicViewModel.allSongs.value ?: emptyList())
        }
        
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = songAdapter
            setHasFixedSize(true)
        }
    }
    
    private fun observeData() {
        musicViewModel.allSongs.observe(viewLifecycleOwner) { songs ->
            songAdapter.submitList(songs)
            
            // Show/hide empty state
            if (songs.isEmpty()) {
                binding.emptyState.visibility = View.VISIBLE
                binding.recyclerView.visibility = View.GONE
            } else {
                binding.emptyState.visibility = View.GONE
                binding.recyclerView.visibility = View.VISIBLE
            }
        }
        
        musicViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}