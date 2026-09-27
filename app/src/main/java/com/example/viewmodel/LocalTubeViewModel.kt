package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.example.data.LocalTubeRepository
import com.example.data.MediaScanner
import com.example.data.model.LocalVideo
import com.example.data.room.LocalTubeDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

import com.example.data.model.Playlist
import com.example.data.model.Channel

class LocalTubeViewModel(application: Application) : AndroidViewModel(application) {
    private val db = Room.databaseBuilder(
        application,
        LocalTubeDatabase::class.java,
        "localtube_db"
    ).build()
    
    private val repository = LocalTubeRepository(db.dao())
    private val mediaScanner = MediaScanner(application)

    val videos: StateFlow<List<LocalVideo>> = repository.allVideos
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val playlists = repository.allPlaylists
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val channels = repository.allChannels
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedVideo = MutableStateFlow<LocalVideo?>(null)
    val selectedVideo: StateFlow<LocalVideo?> = _selectedVideo.asStateFlow()

    private val _isPlayerExpanded = MutableStateFlow(false)
    val isPlayerExpanded: StateFlow<Boolean> = _isPlayerExpanded.asStateFlow()

    fun selectVideo(video: LocalVideo?) {
        _selectedVideo.value = video
        if (video != null) {
            _isPlayerExpanded.value = true
        }
    }

    fun setPlayerExpanded(expanded: Boolean) {
        _isPlayerExpanded.value = expanded
    }

    fun createPlaylist(name: String, description: String = "") {
        viewModelScope.launch {
            repository.insertPlaylist(Playlist(name = name, description = description, videoIds = ""))
        }
    }

    fun createChannel(name: String, description: String = "") {
        viewModelScope.launch {
            repository.insertChannel(Channel(name = name, description = description))
        }
    }

    fun updateVideo(video: LocalVideo) {
        viewModelScope.launch {
            repository.insertVideo(video)
            if (_selectedVideo.value?.id == video.id) {
                _selectedVideo.value = video
            }
        }
    }

    fun scanMedia() {
        viewModelScope.launch {
            val scannedVideos = mediaScanner.scanVideos()
            if (scannedVideos.isNotEmpty()) {
                repository.insertVideos(scannedVideos)
            } else if (videos.value.isEmpty()) {
                // Seed some mock data if nothing found (for demo)
                seedMockData()
            }
        }
    }

    private suspend fun seedMockData() {
        // Create Mock Channels
        val mockChannels = listOf(
            Channel(1, "Nature Explorer", "Stunning landscapes & raw wildlife videos", null),
            Channel(2, "Urban Beats", "Exploring city life, tech and street cultures", null),
            Channel(3, "Cook & Taste", "Quick delicious offline recipes and kitchen hacks", null)
        )
        for (channel in mockChannels) {
            repository.insertChannel(channel)
        }

        // Create Mock Videos with matched channelId and folder-like paths
        val mockVideos = listOf(
            LocalVideo(1, "Nature Wonders: Scenic Rivers", "/storage/emulated/0/Movies/nature_scenic.mp4", 120000, 5200000, System.currentTimeMillis(), channelId = 1, tags = "nature,rivers"),
            LocalVideo(2, "Urban Jungle: Tokyo Nightwalk", "/storage/emulated/0/Movies/tokyo_walk.mp4", 180000, 8400000, System.currentTimeMillis() - 86400000, channelId = 2, tags = "city,tokyo"),
            LocalVideo(3, "Cooking Masterclass: Italian Pasta", "/storage/emulated/0/Downloads/pasta_recipe.mp4", 600000, 25600000, System.currentTimeMillis() - 172800000, channelId = 3, tags = "cooking,pasta"),
            LocalVideo(4, "Tech Review: Dynamic Future 2026", "/storage/emulated/0/Downloads/tech_review.mp4", 450000, 15800000, System.currentTimeMillis() - 259200000, channelId = 2, tags = "tech,future")
        )
        repository.insertVideos(mockVideos)

        // Create Mock Playlists referencing the videos
        val mockPlaylists = listOf(
            Playlist(1, "Favorites", "My absolute favorite offline captures", "1,3"),
            Playlist(2, "Watch Later", "Saved videos to analyze later", "2,4")
        )
        for (playlist in mockPlaylists) {
            repository.insertPlaylist(playlist)
        }
    }
}
