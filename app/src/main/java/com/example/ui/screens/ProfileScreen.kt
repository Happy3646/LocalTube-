package com.example.ui.screens

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.Channel
import com.example.data.model.LocalVideo
import com.example.data.model.Playlist
import com.example.ui.components.formatDuration
import com.example.ui.components.formatSize
import com.example.ui.theme.GrayText
import com.example.ui.theme.PrimaryRed
import com.example.viewmodel.LocalTubeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: LocalTubeViewModel,
    modifier: Modifier = Modifier
) {
    val videos by viewModel.videos.collectAsState()
    val playlists by viewModel.playlists.collectAsState()
    val channels by viewModel.channels.collectAsState()
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    var activeTab by remember { mutableStateOf("folders") } // folders, playlists, channels, settings

    // Extract folders from video paths
    val folders = remember(videos) {
        videos.groupBy { video ->
            val parts = video.path.split("/")
            if (parts.size > 1) parts[parts.size - 2] else "Root"
        }.map { (folderName, folderVideos) ->
            FolderItem(name = folderName, videos = folderVideos)
        }
    }

    var selectedFolderVideos by remember { mutableStateOf<List<LocalVideo>?>(null) }
    var selectedPlaylistVideos by remember { mutableStateOf<List<LocalVideo>?>(null) }
    var selectedPlaylistName by remember { mutableStateOf("") }
    var showCreatePlaylistDialog by remember { mutableStateOf(false) }
    var newPlaylistName by remember { mutableStateOf("") }
    var newPlaylistDesc by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // User Profile Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            PrimaryRed.copy(alpha = 0.3f),
                            Color.Transparent
                        )
                    )
                )
                .padding(horizontal = 16.dp, vertical = if (isLandscape) 8.dp else 18.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Avatar
                Box(
                    modifier = Modifier
                        .size(if (isLandscape) 42.dp else 64.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(PrimaryRed, Color(0xFFFF5252))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = "Profile",
                        tint = Color.White,
                        modifier = Modifier.size(if (isLandscape) 24.dp else 32.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "LocalTube Pro User",
                        style = if (isLandscape) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Manage & watch your offline collection",
                        style = MaterialTheme.typography.bodySmall,
                        color = GrayText
                    )
                }
            }
        }

        // Navigation Grid (Single row of 4 in Landscape, 2x2 in Portrait)
        if (isLandscape) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ProfileNavButton(
                    title = "Folders",
                    icon = Icons.Default.Folder,
                    isSelected = activeTab == "folders",
                    count = folders.size,
                    onClick = { activeTab = "folders" },
                    modifier = Modifier.weight(1f),
                    isLandscape = true
                )
                ProfileNavButton(
                    title = "Playlists",
                    icon = Icons.Default.PlaylistPlay,
                    isSelected = activeTab == "playlists",
                    count = playlists.size,
                    onClick = { activeTab = "playlists" },
                    modifier = Modifier.weight(1f),
                    isLandscape = true
                )
                ProfileNavButton(
                    title = "Channels",
                    icon = Icons.Default.Subscriptions,
                    isSelected = activeTab == "channels",
                    count = channels.size,
                    onClick = { activeTab = "channels" },
                    modifier = Modifier.weight(1f),
                    isLandscape = true
                )
                ProfileNavButton(
                    title = "Settings",
                    icon = Icons.Default.Settings,
                    isSelected = activeTab == "settings",
                    count = null,
                    onClick = { activeTab = "settings" },
                    modifier = Modifier.weight(1f),
                    isLandscape = true
                )
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ProfileNavButton(
                    title = "Folders",
                    icon = Icons.Default.Folder,
                    isSelected = activeTab == "folders",
                    count = folders.size,
                    onClick = { activeTab = "folders" },
                    modifier = Modifier.weight(1f),
                    isLandscape = false
                )
                ProfileNavButton(
                    title = "Playlists",
                    icon = Icons.Default.PlaylistPlay,
                    isSelected = activeTab == "playlists",
                    count = playlists.size,
                    onClick = { activeTab = "playlists" },
                    modifier = Modifier.weight(1f),
                    isLandscape = false
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ProfileNavButton(
                    title = "Channels",
                    icon = Icons.Default.Subscriptions,
                    isSelected = activeTab == "channels",
                    count = channels.size,
                    onClick = { activeTab = "channels" },
                    modifier = Modifier.weight(1f),
                    isLandscape = false
                )
                ProfileNavButton(
                    title = "Settings",
                    icon = Icons.Default.Settings,
                    isSelected = activeTab == "settings",
                    count = null,
                    onClick = { activeTab = "settings" },
                    modifier = Modifier.weight(1f),
                    isLandscape = false
                )
            }
        }

        Spacer(modifier = Modifier.height(if (isLandscape) 4.dp else 12.dp))

        // Dynamic Section Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = when (activeTab) {
                    "folders" -> "Local Video Folders"
                    "playlists" -> "My Playlists"
                    "channels" -> "Manage Subscriptions"
                    else -> "App Settings"
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            if (activeTab == "playlists") {
                Button(
                    onClick = { showCreatePlaylistDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryRed),
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "New Playlist",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }

        // Sub-content viewer based on active selection
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp)
        ) {
            when (activeTab) {
                "folders" -> {
                    if (folders.isEmpty()) {
                        EmptyStateView(
                            icon = Icons.Default.FolderOpen,
                            title = "No Folders Detected",
                            desc = "Folders containing video media will automatically appear here."
                        )
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(folders) { folder ->
                                FolderCard(folder = folder) {
                                    selectedFolderVideos = folder.videos
                                }
                            }
                        }
                    }
                }
                "playlists" -> {
                    if (playlists.isEmpty()) {
                        EmptyStateView(
                            icon = Icons.Default.PlaylistAdd,
                            title = "No Playlists Yet",
                            desc = "Create customized playlists to group your favorite local videos together."
                        )
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(playlists) { playlist ->
                                PlaylistCard(playlist = playlist, videos = videos) {
                                    val videoIdList = playlist.videoIds.split(",").filter { it.isNotEmpty() }.mapNotNull { it.toLongOrNull() }
                                    selectedPlaylistVideos = videos.filter { videoIdList.contains(it.id) }
                                    selectedPlaylistName = playlist.name
                                }
                            }
                        }
                    }
                }
                "channels" -> {
                    if (channels.isEmpty()) {
                        EmptyStateView(
                            icon = Icons.Default.Subscriptions,
                            title = "No Channels Created",
                            desc = "Group videos by local creators, topics, or folders into custom Channels."
                        )
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(channels) { channel ->
                                ChannelCard(channel = channel, videoCount = videos.count { it.channelId == channel.id })
                            }
                        }
                    }
                }
                "settings" -> {
                    SettingsSection(viewModel)
                }
            }
        }
    }

    // Dialog: Folder Videos list
    selectedFolderVideos?.let { folderVideos ->
        AlertDialog(
            onDismissRequest = { selectedFolderVideos = null },
            title = { Text("Videos in Folder") },
            text = {
                Box(modifier = Modifier.heightIn(max = if (isLandscape) 180.dp else 360.dp)) {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(folderVideos) { video ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.selectVideo(video)
                                        selectedFolderVideos = null
                                    }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = PrimaryRed)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(video.title, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.Medium)
                                    Text(formatDuration(video.duration), style = MaterialTheme.typography.bodySmall, color = GrayText)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedFolderVideos = null }) {
                    Text("Close", color = PrimaryRed)
                }
            }
        )
    }

    // Dialog: Playlist Videos list
    selectedPlaylistVideos?.let { playlistVideos ->
        AlertDialog(
            onDismissRequest = { selectedPlaylistVideos = null },
            title = { Text(selectedPlaylistName) },
            text = {
                Box(modifier = Modifier.heightIn(max = if (isLandscape) 180.dp else 360.dp)) {
                    if (playlistVideos.isEmpty()) {
                        Text("This playlist is currently empty.")
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(playlistVideos) { video ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            viewModel.selectVideo(video)
                                            selectedPlaylistVideos = null
                                        }
                                        .padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = PrimaryRed)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(video.title, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.Medium)
                                        Text(formatDuration(video.duration), style = MaterialTheme.typography.bodySmall, color = GrayText)
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedPlaylistVideos = null }) {
                    Text("Close", color = PrimaryRed)
                }
            }
        )
    }

    // Dialog: Create Playlist
    if (showCreatePlaylistDialog) {
        AlertDialog(
            onDismissRequest = { showCreatePlaylistDialog = false },
            title = { Text("Create Playlist") },
            text = {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .heightIn(max = if (isLandscape) 180.dp else 360.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = newPlaylistName,
                        onValueChange = { newPlaylistName = it },
                        label = { Text("Playlist Name") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryRed, focusedLabelColor = PrimaryRed)
                    )
                    OutlinedTextField(
                        value = newPlaylistDesc,
                        onValueChange = { newPlaylistDesc = it },
                        label = { Text("Description") },
                        singleLine = false,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryRed, focusedLabelColor = PrimaryRed)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPlaylistName.isNotBlank()) {
                            viewModel.createPlaylist(newPlaylistName, newPlaylistDesc)
                            newPlaylistName = ""
                            newPlaylistDesc = ""
                            showCreatePlaylistDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryRed)
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreatePlaylistDialog = false }) {
                    Text("Cancel", color = GrayText)
                }
            }
        )
    }
}

@Composable
fun ProfileNavButton(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    count: Int?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLandscape: Boolean = false
) {
    Card(
        modifier = modifier
            .height(if (isLandscape) 48.dp else 68.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) PrimaryRed.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            contentColor = if (isSelected) PrimaryRed else MaterialTheme.colorScheme.onSurface
        ),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, PrimaryRed) else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = if (isLandscape) 8.dp else 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(if (isLandscape) 30.dp else 38.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSelected) PrimaryRed.copy(alpha = 0.2f) else MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = title,
                    modifier = Modifier.size(if (isLandscape) 18.dp else 22.dp)
                )
            }

            Spacer(modifier = Modifier.width(if (isLandscape) 6.dp else 10.dp))

            Column {
                Text(
                    text = title,
                    style = if (isLandscape) MaterialTheme.typography.labelMedium else MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis
                )
                if (count != null) {
                    Text(
                        text = "$count items",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSelected) PrimaryRed else GrayText,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }
    }
}

@Composable
fun FolderCard(folder: FolderItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Folder,
                contentDescription = "Folder",
                tint = PrimaryRed,
                modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(folder.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                Text("${folder.videos.size} videos", style = MaterialTheme.typography.bodySmall, color = GrayText)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = "View", tint = GrayText)
        }
    }
}

@Composable
fun PlaylistCard(playlist: Playlist, videos: List<LocalVideo>, onClick: () -> Unit) {
    val count = remember(playlist, videos) {
        playlist.videoIds.split(",").filter { it.isNotEmpty() }.size
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.PlaylistPlay,
                contentDescription = "Playlist",
                tint = PrimaryRed,
                modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(playlist.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                Text(if (playlist.description.isNotEmpty()) playlist.description else "Custom Video Playlist", style = MaterialTheme.typography.bodySmall, color = GrayText)
                Text("$count videos", style = MaterialTheme.typography.labelSmall, color = PrimaryRed, fontWeight = FontWeight.SemiBold)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = "View", tint = GrayText)
        }
    }
}

@Composable
fun ChannelCard(channel: Channel, videoCount: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(PrimaryRed.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(channel.name.take(1).uppercase(), color = PrimaryRed, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(channel.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                Text(channel.description, style = MaterialTheme.typography.bodySmall, color = GrayText)
                Text("$videoCount uploaded videos", style = MaterialTheme.typography.labelSmall, color = PrimaryRed)
            }
            Button(
                onClick = { /* already subbed */ },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.height(32.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
            ) {
                Text("Subscribed", style = MaterialTheme.typography.labelMedium, color = GrayText)
            }
        }
    }
}

@Composable
fun SettingsSection(viewModel: LocalTubeViewModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Video Feed Rows", fontWeight = FontWeight.Bold)
                    Text("Locked to 1 video per row (single-column) to prevent layout distortion", style = MaterialTheme.typography.bodySmall, color = GrayText)
                }
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = PrimaryRed.copy(alpha = 0.15f),
                    modifier = Modifier.padding(start = 12.dp)
                ) {
                    Text(
                        "1 Row",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = PrimaryRed
                    )
                }
            }

            Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Auto-Play Next Video", fontWeight = FontWeight.Bold)
                    Text("Automatically play upcoming video in directory", style = MaterialTheme.typography.bodySmall, color = GrayText)
                }
                Switch(checked = true, onCheckedChange = {}, colors = SwitchDefaults.colors(checkedThumbColor = PrimaryRed, checkedTrackColor = PrimaryRed.copy(alpha = 0.5f)))
            }

            Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.scanMedia() },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Scan", tint = PrimaryRed)
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text("Force Media Library Rescan", fontWeight = FontWeight.Bold)
                    Text("Manually look for new movies/videos on device", style = MaterialTheme.typography.bodySmall, color = GrayText)
                }
            }

            Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Info, contentDescription = "About", tint = GrayText)
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text("About LocalTube", fontWeight = FontWeight.Bold)
                    Text("v1.2.0 Pro Player Core • Offline First", style = MaterialTheme.typography.bodySmall, color = GrayText)
                }
            }
        }
    }
}

@Composable
fun EmptyStateView(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    desc: String
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = PrimaryRed.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        Text(desc, style = MaterialTheme.typography.bodySmall, color = GrayText, textAlign = TextAlign.Center)
    }
}

data class FolderItem(
    val name: String,
    val videos: List<LocalVideo>
)
