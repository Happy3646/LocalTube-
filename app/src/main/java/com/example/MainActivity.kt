package com.example

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.components.WatchStage
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ShortsScreen
import com.example.ui.screens.ChannelsScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.theme.LocalTubeTheme
import com.example.viewmodel.LocalTubeViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: LocalTubeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LocalTubeTheme {
                MainContent(viewModel)
            }
        }
    }
}

data class NavigationItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)

@Composable
fun MainContent(viewModel: LocalTubeViewModel) {
    val navController = rememberNavController()
    val selectedVideo by viewModel.selectedVideo.collectAsState()
    val isPlayerExpanded by viewModel.isPlayerExpanded.collectAsState()
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    
    var currentRoute by remember { mutableStateOf("home") }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal),
            topBar = {
                // Show custom sketch header on Home, Channels, and Profile screens
                if (currentRoute != "shorts") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .windowInsetsPadding(
                                WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top)
                            )
                            .padding(
                                horizontal = 16.dp,
                                vertical = if (isLandscape) 4.dp else 6.dp
                            ),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Sketch-style Red Folder Logo (more compact)
                            Image(
                                painter = painterResource(id = R.drawable.img_localtube_logo_v2_1790515007810),
                                contentDescription = "Logo",
                                modifier = Modifier
                                    .size(if (isLandscape) 24.dp else 28.dp)
                                    .clip(RoundedCornerShape(6.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "LocalTube",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    fontSize = if (isLandscape) 16.sp else 18.sp,
                                    letterSpacing = (-0.5).sp
                                ),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                        
                        // Compact "Mix" Shuffle Pill Button on top right
                        Button(
                            onClick = {
                                val allVids = viewModel.videos.value
                                if (allVids.isNotEmpty()) {
                                    viewModel.selectVideo(allVids.random())
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            shape = RoundedCornerShape(16.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                            modifier = Modifier.height(if (isLandscape) 26.dp else 28.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Shuffle,
                                    contentDescription = "Mix Shuffle",
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Mix",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }
                }
            },
            bottomBar = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .windowInsetsPadding(
                            WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom)
                        )
                ) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f), thickness = 1.dp)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(if (isLandscape) 52.dp else 70.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val screens = listOf(
                            NavigationItem("home", "Videos", Icons.Default.Home),
                            NavigationItem("shorts", "Shorts", Icons.Default.PlayArrow),
                            NavigationItem("channels", "Channels", Icons.Default.Subscriptions),
                            NavigationItem("profile", "Profile", Icons.Default.Person)
                        )
                        
                        screens.forEachIndexed { index, screen ->
                            val isSelected = currentRoute == screen.route
                            
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clickable {
                                        currentRoute = screen.route
                                        navController.navigate(screen.route) {
                                            popUpTo(navController.graph.startDestinationId) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center,
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    // LABEL ON TOP
                                    Text(
                                        text = screen.label,
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        ),
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                        fontSize = if (isLandscape) 10.sp else 12.sp,
                                        maxLines = 1,
                                        softWrap = false,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    
                                    Spacer(modifier = Modifier.height(if (isLandscape) 2.dp else 4.dp))
                                    
                                    // ICON ON BOTTOM
                                    if (screen.route == "shorts") {
                                        Box(
                                            modifier = Modifier
                                                .size(if (isLandscape) 18.dp else 22.dp)
                                                .clip(CircleShape)
                                                .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                Icons.Default.PlayArrow,
                                                contentDescription = "Shorts",
                                                tint = MaterialTheme.colorScheme.surface,
                                                modifier = Modifier.size(if (isLandscape) 11.dp else 14.dp)
                                            )
                                        }
                                    } else {
                                        Icon(
                                            imageVector = screen.icon,
                                            contentDescription = screen.label,
                                            tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                            modifier = Modifier.size(if (isLandscape) 18.dp else 22.dp)
                                        )
                                    }
                                }
                            }
                            
                            // Divider line between options exactly as sketched
                            if (index < screens.lastIndex) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight(0.4f)
                                        .width(1.dp)
                                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                                )
                            }
                        }
                    }
                }
            }
        ) { padding ->
            NavHost(
                navController = navController,
                startDestination = "home",
                modifier = Modifier.padding(padding)
            ) {
                composable("home") {
                    HomeScreen(viewModel)
                }
                composable("shorts") {
                    ShortsScreen(viewModel)
                }
                composable("channels") {
                    ChannelsScreen(viewModel)
                }
                composable("profile") {
                    ProfileScreen(viewModel)
                }
            }
        }

        // Watch Stage Overlay
        if (selectedVideo != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .align(Alignment.BottomCenter)
            ) {
                WatchStage(
                    video = selectedVideo,
                    isExpanded = isPlayerExpanded,
                    viewModel = viewModel,
                    onCollapse = { viewModel.setPlayerExpanded(false) },
                    onExpand = { viewModel.setPlayerExpanded(true) },
                    onClose = { viewModel.selectVideo(null) }
                )
            }
        }
    }
}
