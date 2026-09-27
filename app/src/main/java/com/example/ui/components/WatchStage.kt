package com.example.ui.components

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.LocalVideo
import com.example.data.model.Channel
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.GrayText
import com.example.ui.theme.PrimaryRed
import com.example.viewmodel.LocalTubeViewModel

@Composable
fun WatchStage(
    video: LocalVideo?,
    isExpanded: Boolean,
    viewModel: LocalTubeViewModel,
    onCollapse: () -> Unit,
    onExpand: () -> Unit,
    onClose: () -> Unit
) {
    if (video == null) return

    AnimatedVisibility(
        visible = true,
        enter = slideInVertically(initialOffsetY = { it }),
        exit = slideOutVertically(targetOffsetY = { it })
    ) {
        if (isExpanded) {
            ExpandedPlayer(video, viewModel, onCollapse)
        } else {
            MiniPlayer(video, onExpand, onClose)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpandedPlayer(
    video: LocalVideo,
    viewModel: LocalTubeViewModel,
    onCollapse: () -> Unit
) {
    val channels by viewModel.channels.collectAsState()
    val assignedChannel = remember(channels, video.channelId) {
        channels.find { it.id == video.channelId }
    }

    var showMenu by remember { mutableStateOf(false) }
    var showTagsDialog by remember { mutableStateOf(false) }
    var showChannelDialog by remember { mutableStateOf(false) }
    
    var tagInput by remember(video) { mutableStateOf(video.tags) }
    var newChannelName by remember { mutableStateOf("") }
    var newChannelDesc by remember { mutableStateOf("") }
    var showCreateChannelField by remember { mutableStateOf(false) }
    var isFullscreen by remember { mutableStateOf(false) }

    BackHandler(enabled = isFullscreen) {
        isFullscreen = false
    }

    // Modern Android Photo Picker for zero-permission custom thumbnail upload
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let {
            val updatedVideo = video.copy(thumbnailUri = it.toString())
            viewModel.updateVideo(updatedVideo)
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column {
            // Video Player Area
            Box(
                modifier = if (isFullscreen) {
                    Modifier
                        .fillMaxSize()
                        .background(Color.Black)
                } else {
                    Modifier
                        .fillMaxWidth()
                        .aspectRatio(16 / 9f)
                        .background(Color.Black)
                }
            ) {
                AsyncImage(
                    model = video.thumbnailUri,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
                
                IconButton(
                    onClick = {
                        if (isFullscreen) {
                            isFullscreen = false
                        } else {
                            onCollapse()
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(if (isFullscreen) 16.dp else 8.dp)
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(
                        if (isFullscreen) Icons.Default.ArrowBack else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (isFullscreen) "Exit Fullscreen" else "Collapse",
                        tint = Color.White
                    )
                }
                
                Icon(
                    Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.5f),
                    modifier = Modifier
                        .size(if (isFullscreen) 80.dp else 64.dp)
                        .align(Alignment.Center)
                )

                // Fullscreen toggle button on the bottom right (as pointed in user screenshot)
                IconButton(
                    onClick = { isFullscreen = !isFullscreen },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(if (isFullscreen) 16.dp else 12.dp)
                        .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                        .size(40.dp)
                ) {
                    Icon(
                        if (isFullscreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                        contentDescription = if (isFullscreen) "Exit Fullscreen" else "Enter Fullscreen",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            // Video Info (Hidden when in fullscreen)
            if (!isFullscreen) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(16.dp)
                ) {
                Text(
                    text = video.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${formatSize(video.size)} • Local File",
                    style = MaterialTheme.typography.bodySmall,
                    color = GrayText
                )

                // Tags Display Row (If tags are present)
                if (video.tags.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        video.tags.split(",").filter { it.isNotEmpty() }.forEach { tag ->
                            SuggestionChip(
                                onClick = {},
                                label = { Text("#$tag", fontSize = 11.sp) },
                                colors = SuggestionChipDefaults.suggestionChipColors(
                                    labelColor = PrimaryRed
                                )
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))

                // Premium Interactive Channel Info Card (Click to open menu as requested)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showMenu = true },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Channel Avatar (First letter or Default)
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(PrimaryRed.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = assignedChannel?.name?.take(1)?.uppercase() ?: "?",
                                color = PrimaryRed,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = assignedChannel?.name ?: "No Channel (Click to assign)",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (assignedChannel != null) MaterialTheme.colorScheme.onSurface else PrimaryRed
                            )
                            Text(
                                text = assignedChannel?.description ?: "Offline Local Creator • Configure settings",
                                style = MaterialTheme.typography.bodySmall,
                                color = GrayText,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Box {
                            IconButton(onClick = { showMenu = true }) {
                                Icon(Icons.Default.MoreVert, contentDescription = "Options", tint = GrayText)
                            }

                            // The Interactive Dropdown Menu containing the working buttons
                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Upload Custom Image") },
                                    leadingIcon = { Icon(Icons.Default.Image, contentDescription = null, tint = PrimaryRed) },
                                    onClick = {
                                        showMenu = false
                                        photoPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Add / Manage Channel") },
                                    leadingIcon = { Icon(Icons.Default.Subscriptions, contentDescription = null, tint = PrimaryRed) },
                                    onClick = {
                                        showMenu = false
                                        showChannelDialog = true
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Manage Tags") },
                                    leadingIcon = { Icon(Icons.Default.Label, contentDescription = null, tint = PrimaryRed) },
                                    onClick = {
                                        showMenu = false
                                        showTagsDialog = true
                                    }
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Static Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    ActionButton(Icons.Default.ThumbUp, "Like")
                    ActionButton(Icons.Default.Share, "Share")
                    ActionButton(Icons.Default.Download, "Offline")
                    ActionButton(Icons.Default.PlaylistAdd, "Save")
                }
            }
        }
    }
    }

    // Dialog: Manage Tags
    if (showTagsDialog) {
        AlertDialog(
            onDismissRequest = { showTagsDialog = false },
            title = { Text("Manage Tags") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Add tags separated by commas to categorize your video.", style = MaterialTheme.typography.bodySmall, color = GrayText)
                    OutlinedTextField(
                        value = tagInput,
                        onValueChange = { tagInput = it },
                        placeholder = { Text("e.g. nature, tutorial, music") },
                        label = { Text("Tags") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryRed,
                            focusedLabelColor = PrimaryRed
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val sanitizedTags = tagInput.split(",")
                            .map { it.trim().lowercase() }
                            .filter { it.isNotEmpty() }
                            .joinToString(",")
                        
                        viewModel.updateVideo(video.copy(tags = sanitizedTags))
                        showTagsDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryRed)
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTagsDialog = false }) {
                    Text("Cancel", color = GrayText)
                }
            }
        )
    }

    // Dialog: Add / Manage Channel
    if (showChannelDialog) {
        AlertDialog(
            onDismissRequest = { 
                showChannelDialog = false
                showCreateChannelField = false
            },
            title = { Text("Assign Video Channel") },
            text = {
                Box(modifier = Modifier.heightIn(max = 350.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Select a creator channel to assign this video to:", style = MaterialTheme.typography.bodySmall, color = GrayText)
                        
                        // Option: No Channel
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.updateVideo(video.copy(channelId = null))
                                    showChannelDialog = false
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = video.channelId == null, onClick = {
                                viewModel.updateVideo(video.copy(channelId = null))
                                showChannelDialog = false
                            })
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("None / Personal Video")
                        }

                        // Existing Channels
                        channels.forEach { channel ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.updateVideo(video.copy(channelId = channel.id))
                                        showChannelDialog = false
                                    }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(selected = video.channelId == channel.id, onClick = {
                                    viewModel.updateVideo(video.copy(channelId = channel.id))
                                    showChannelDialog = false
                                })
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(channel.name, fontWeight = FontWeight.Medium)
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

                        if (!showCreateChannelField) {
                            TextButton(
                                onClick = { showCreateChannelField = true },
                                colors = ButtonDefaults.textButtonColors(contentColor = PrimaryRed)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Create New Channel")
                            }
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = newChannelName,
                                    onValueChange = { newChannelName = it },
                                    label = { Text("Channel Name") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                OutlinedTextField(
                                    value = newChannelDesc,
                                    onValueChange = { newChannelDesc = it },
                                    label = { Text("Channel Description") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Button(
                                    onClick = {
                                        if (newChannelName.isNotBlank()) {
                                            viewModel.createChannel(newChannelName, newChannelDesc)
                                            newChannelName = ""
                                            newChannelDesc = ""
                                            showCreateChannelField = false
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryRed),
                                    modifier = Modifier.align(Alignment.End)
                                ) {
                                    Text("Add")
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { 
                    showChannelDialog = false
                    showCreateChannelField = false
                }) {
                    Text("Done", color = PrimaryRed)
                }
            }
        )
    }
}

@Composable
fun MiniPlayer(
    video: LocalVideo,
    onExpand: () -> Unit,
    onClose: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .clickable { onExpand() },
        color = DarkSurface,
        tonalElevation = 8.dp
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            AsyncImage(
                model = video.thumbnailUri,
                contentDescription = null,
                modifier = Modifier
                    .width(100.dp)
                    .fillMaxHeight()
                    .padding(vertical = 4.dp),
                contentScale = ContentScale.Crop
            )
            
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            ) {
                Text(
                    text = video.title,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Local Storage",
                    style = MaterialTheme.typography.labelSmall,
                    color = GrayText
                )
            }
            
            IconButton(onClick = { /* play/pause */ }) {
                Icon(Icons.Default.PlayArrow, contentDescription = "Play")
            }
            
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Close")
            }
        }
    }
}

@Composable
fun ActionButton(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = label)
        Text(text = label, style = MaterialTheme.typography.labelSmall)
    }
}
