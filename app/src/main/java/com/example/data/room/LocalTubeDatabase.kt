package com.example.data.room

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RoomDatabase
import com.example.data.model.Channel
import com.example.data.model.LocalVideo
import com.example.data.model.Playlist
import kotlinx.coroutines.flow.Flow

@Dao
interface LocalTubeDao {
    @Query("SELECT * FROM videos ORDER BY dateAdded DESC")
    fun getAllVideos(): Flow<List<LocalVideo>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVideos(videos: List<LocalVideo>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVideo(video: LocalVideo)

    @Query("SELECT * FROM playlists")
    fun getAllPlaylists(): Flow<List<Playlist>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: Playlist)

    @Query("SELECT * FROM channels")
    fun getAllChannels(): Flow<List<Channel>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChannel(channel: Channel)
}

@Database(entities = [LocalVideo::class, Playlist::class, Channel::class], version = 1, exportSchema = false)
abstract class LocalTubeDatabase : RoomDatabase() {
    abstract fun dao(): LocalTubeDao
}
