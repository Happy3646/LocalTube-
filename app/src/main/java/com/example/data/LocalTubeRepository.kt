package com.example.data

import com.example.data.model.Channel
import com.example.data.model.LocalVideo
import com.example.data.model.Playlist
import com.example.data.room.LocalTubeDao
import kotlinx.coroutines.flow.Flow

class LocalTubeRepository(private val dao: LocalTubeDao) {
    val allVideos: Flow<List<LocalVideo>> = dao.getAllVideos()
    val allPlaylists: Flow<List<Playlist>> = dao.getAllPlaylists()
    val allChannels: Flow<List<Channel>> = dao.getAllChannels()

    suspend fun insertVideos(videos: List<LocalVideo>) = dao.insertVideos(videos)
    suspend fun insertVideo(video: LocalVideo) = dao.insertVideo(video)
    suspend fun insertPlaylist(playlist: Playlist) = dao.insertPlaylist(playlist)
    suspend fun insertChannel(channel: Channel) = dao.insertChannel(channel)
}
