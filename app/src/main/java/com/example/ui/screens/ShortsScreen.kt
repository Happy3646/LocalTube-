package com.example.ui.screens

import android.content.res.Configuration
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.LocalVideo
import com.example.ui.components.formatSize
import com.example.ui.theme.GrayText
import com.example.ui.theme.PrimaryRed
import com.example.viewmodel.LocalTubeViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ShortsScreen(
    viewModel: LocalTubeViewModel,
    modifier: Modifier = Modifier
) {
    val videos by viewModel.videos.collectAsState()
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val shortsList = remember(videos) {
        videos
    }

    if (shortsList.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = PrimaryRed)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Loading Shorts...", color = Color.White)
            }
        }
        return
    }

    val pagerState = rememberPagerState(pageCount = { shortsList.size })

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        VerticalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            val video = shortsList[page]
            ShortPageItem(video = video, isLandscape = isLandscape) {
                viewModel.selectVideo(video)
            }
        }

        // Shorts Header Badge
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(
                    WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top)
                )
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.PlayArrow,
                contentDescription = null,
                tint = PrimaryRed,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Shorts",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun ShortPageItem(
    video: LocalVideo,
    isLandscape: Boolean = false,
    onPlayFull: () -> Unit
) {
    var isLiked by remember { mutableStateOf(false) }
    var showHeartAnimation by remember { mutableStateOf(false) }
    var isPlaying by remember { mutableStateOf(true) }

    // Audio bar animation
    val infiniteTransition = rememberInfiniteTransition(label = "audioBar")
    val barHeight1 by infiniteTransition.animateFloat(
        initialValue = 10f,
        targetValue = 28f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "b1"
    )
    val barHeight2 by infiniteTransition.animateFloat(
        initialValue = 28f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "b2"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        isLiked = true
                        showHeartAnimation = true
                    },
                    onTap = {
                        isPlaying = !isPlaying
                    }
                )
            }
    ) {
        // Video Fullscreen Thumbnail/Cover
        AsyncImage(
            model = video.thumbnailUri,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = if (isLandscape) ContentScale.Fit else ContentScale.Crop
        )

        // Semi-transparent dark gradient overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.3f),
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.7f)
                        )
                    )
                )
        )

        // Play/Pause Overlay indicator
        if (!isPlaying) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.5f))
                    .align(Alignment.Center)
            ) {
                Icon(
                    Icons.Default.PlayArrow,
                    contentDescription = "Paused",
                    tint = Color.White,
                    modifier = Modifier
                        .size(40.dp)
                        .align(Alignment.Center)
                )
            }
        }

        // Double-tap Floating Heart Animation
        if (showHeartAnimation) {
            LaunchedEffect(Unit) {
                delay(600)
                showHeartAnimation = false
            }
            Box(
                modifier = Modifier.align(Alignment.Center)
            ) {
                Icon(
                    Icons.Default.Favorite,
                    contentDescription = null,
                    tint = PrimaryRed,
                    modifier = Modifier.size(100.dp)
                )
            }
        }

        // Left Info Content Overlay (Title, Channel Name, Description)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomStart)
                .windowInsetsPadding(
                    WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom)
                )
                .padding(
                    bottom = if (isLandscape) 12.dp else 24.dp,
                    start = 16.dp,
                    end = if (isLandscape) 90.dp else 80.dp
                )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(PrimaryRed),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = video.title.take(1).uppercase(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "@LocalCreator",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = { /* Sub */ },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryRed),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.height(26.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp)
                ) {
                    Text("Subscribe", fontSize = 11.sp, color = Color.White)
                }
            }

            Text(
                text = video.title,
                color = Color.White,
                fontSize = if (isLandscape) 13.sp else 14.sp,
                maxLines = if (isLandscape) 1 else 2,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 6.dp)
            )

            // Audio track label & Animated bars
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Original Local Audio • ${formatSize(video.size)}",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Right Interactive Action Strip Overlay
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .windowInsetsPadding(
                    WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom)
                )
                .padding(bottom = if (isLandscape) 12.dp else 24.dp, end = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(if (isLandscape) 8.dp else 16.dp)
        ) {
            // Like
            ShortActionColumn(
                icon = Icons.Default.Favorite,
                tint = if (isLiked) PrimaryRed else Color.White,
                label = if (isLiked) "1.2K" else "1.1K"
            ) {
                isLiked = !isLiked
            }

            // Dislike
            ShortActionColumn(
                icon = Icons.Default.ThumbDown,
                label = "Dislike"
            ) {}

            // Share
            ShortActionColumn(
                icon = Icons.Default.Share,
                label = "Share"
            ) {}

            // Full Screen Play Mode button
            ShortActionColumn(
                icon = Icons.Default.Fullscreen,
                label = "Play Full"
            ) {
                onPlayFull()
            }

            // Audio Disk rotating placeholder
            Box(
                modifier = Modifier
                    .size(if (isLandscape) 32.dp else 40.dp)
                    .clip(CircleShape)
                    .background(Color.DarkGray)
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalAlignment = Alignment.Bottom,
                    modifier = Modifier.fillMaxHeight().padding(bottom = 4.dp)
                ) {
                    Box(modifier = Modifier.width(3.dp).height(barHeight1.dp).background(Color.White))
                    Box(modifier = Modifier.width(3.dp).height(barHeight2.dp).background(Color.White))
                    Box(modifier = Modifier.width(3.dp).height(barHeight1.dp).background(Color.White))
                }
            }
        }
    }
}

@Composable
fun ShortActionColumn(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color = Color.White,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.4f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = label,
                tint = tint,
                modifier = Modifier.size(26.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
