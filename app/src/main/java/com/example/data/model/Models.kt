package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "videos")
data class LocalVideo(
    @PrimaryKey val id: Long,
    val title: String,
    val path: String,
    val duration: Long,
    val size: Long,
    val dateAdded: Long,
    val thumbnailUri: String? = null,
    val channelId: Long? = null,
    val tags: String = "" // Comma separated
)

@Serializable
@Entity(tableName = "playlists")
data class Playlist(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String = "",
    val videoIds: String = "" // Comma separated list of video IDs
)

@Serializable
@Entity(tableName = "channels")
data class Channel(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String = "",
    val bannerUri: String? = null
)
