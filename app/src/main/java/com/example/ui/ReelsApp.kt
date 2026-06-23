package com.example.ui

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddBox
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.CommentBottomSheet
import com.example.ui.components.FeedScreen
import com.example.ui.components.ProfileScreen
import com.example.ui.components.UploadScreen
import com.example.ui.components.LiveScreen
import kotlinx.coroutines.launch

import com.example.ui.components.SignupScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReelsApp(
    modifier: Modifier = Modifier,
    viewModel: ReelViewModel = viewModel()
) {
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val username by viewModel.username.collectAsState()
    val email by viewModel.email.collectAsState()
    val phoneNumber by viewModel.phoneNumber.collectAsState()

    val reels by viewModel.reels.collectAsState()
    val currentTab by viewModel.currentTab.collectAsState()
    val uploadStatus by viewModel.uploadStatus.collectAsState()
    val selectedReelCommentsId by viewModel.selectedReelIdForComments.collectAsState()
    val comments by viewModel.activeComments.collectAsState()

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    if (!isLoggedIn) {
        SignupScreen(
            onSignupSuccess = { user, mail, phone ->
                viewModel.signup(user, mail, phone)
            }
        )
    } else {
        Scaffold(
            bottomBar = {
                NavigationBar(
                    containerColor = Color(0xFF0F0F0F), // Immersive deep dark bottom navigation
                    contentColor = Color.White
                ) {
                    NavigationBarItem(
                        selected = currentTab == AppTab.FEED,
                        onClick = { viewModel.selectTab(AppTab.FEED) },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Home,
                                contentDescription = "Feed"
                            )
                        },
                        label = { Text("Feed", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFFE91E63),
                            selectedTextColor = Color(0xFFE91E63),
                            unselectedIconColor = Color.Gray,
                            unselectedTextColor = Color.Gray,
                            indicatorColor = Color.Transparent
                        ),
                        modifier = Modifier.testTag("tab_feed")
                    )

                    NavigationBarItem(
                        selected = currentTab == AppTab.LIVE,
                        onClick = { viewModel.selectTab(AppTab.LIVE) },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Videocam,
                                contentDescription = "Live"
                            )
                        },
                        label = { Text("Live", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFFFF0055), // Vibrant red/pink for LIVE stream
                            selectedTextColor = Color(0xFFFF0055),
                            unselectedIconColor = Color.Gray,
                            unselectedTextColor = Color.Gray,
                            indicatorColor = Color.Transparent
                        ),
                        modifier = Modifier.testTag("tab_live")
                    )

                    NavigationBarItem(
                        selected = currentTab == AppTab.UPLOAD,
                        onClick = { viewModel.selectTab(AppTab.UPLOAD) },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.AddBox,
                                contentDescription = "Upload"
                            )
                        },
                        label = { Text("Upload", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF00FFCC), // Mint green highlight for upload!
                            selectedTextColor = Color(0xFF00FFCC),
                            unselectedIconColor = Color.Gray,
                            unselectedTextColor = Color.Gray,
                            indicatorColor = Color.Transparent
                        ),
                        modifier = Modifier.testTag("tab_upload")
                    )

                    NavigationBarItem(
                        selected = currentTab == AppTab.PROFILE,
                        onClick = { viewModel.selectTab(AppTab.PROFILE) },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Profile"
                            )
                        },
                        label = { Text("Profile", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFFE91E63),
                            selectedTextColor = Color(0xFFE91E63),
                            unselectedIconColor = Color.Gray,
                            unselectedTextColor = Color.Gray,
                            indicatorColor = Color.Transparent
                        ),
                        modifier = Modifier.testTag("tab_profile")
                    )
                }
            },
            modifier = modifier.fillMaxSize()
        ) { innerPadding ->
            // Main Screen Router with smooth fade transition
            Crossfade(
                targetState = currentTab,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                label = "tabTransition"
            ) { tab ->
                when (tab) {
                    AppTab.FEED -> {
                        FeedScreen(
                            reels = reels,
                            onLikeToggle = { reel -> viewModel.toggleLike(reel) },
                            onCommentClick = { reelId -> viewModel.showCommentsForReel(reelId) }
                        )
                    }
                    AppTab.LIVE -> {
                        LiveScreen(
                            currentUsername = username
                        )
                    }
                    AppTab.UPLOAD -> {
                        UploadScreen(
                            uploadStatus = uploadStatus,
                            onUploadClick = { uri, title, description, creator ->
                                viewModel.uploadReel(uri, title, description, creator)
                            },
                            onResetStatus = { viewModel.resetUploadStatus() }
                        )
                    }
                    AppTab.PROFILE -> {
                        ProfileScreen(
                            reels = reels,
                            username = username,
                            email = email,
                            phoneNumber = phoneNumber,
                            onLogout = { viewModel.logout() },
                            onReelSelect = { reel ->
                                viewModel.selectTab(AppTab.FEED)
                                // Ideally, focus/scroll pager to this specific reel, which our VerticalPager will handle
                            }
                        )
                    }
                }
            }

            // Overlay Comments Sheet
            if (selectedReelCommentsId != null) {
                CommentBottomSheet(
                    comments = comments,
                    sheetState = sheetState,
                    onDismissRequest = { viewModel.showCommentsForReel(null) },
                    onAddComment = { text ->
                        viewModel.addComment(selectedReelCommentsId!!, username.ifEmpty { "Me" }, text)
                    },
                    onDeleteComment = { commentId ->
                        viewModel.deleteComment(commentId)
                    }
                )
            }
        }
    }
}
