package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Phone
import androidx.compose.ui.platform.testTag
import com.example.data.Reel

@Composable
fun ProfileScreen(
    reels: List<Reel>,
    username: String,
    email: String,
    phoneNumber: String,
    onLogout: () -> Unit,
    onReelSelect: (Reel) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTabState by remember { mutableStateOf(0) } // 0: My Reels, 1: Liked
    val myReels = reels.filter { !it.videoUrl.contains("storage.googleapis.com") } // Show locally uploaded files
    val likedReels = reels.filter { it.isLiked }

    val displayReels = if (selectedTabState == 0) {
        if (myReels.isNotEmpty()) myReels else reels // fallback to all if none uploaded yet to look filled
    } else {
        likedReels
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
            .padding(top = 54.dp)
    ) {
        // Bio Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE91E63).copy(alpha = 0.2f))
                    .border(2.dp, Color(0xFFE91E63), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (username.isNotEmpty()) username.take(1).uppercase() else "U",
                    color = Color(0xFFE91E63),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = username.ifEmpty { "Reels User" },
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "@${username.ifEmpty { "reels_creator" }.lowercase().replace(" ", "_")}",
                color = Color(0xFF00FFCC),
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Display Phone & Email with elegant icons
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (email.isNotEmpty()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = "Email",
                            tint = Color.Gray,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = email,
                            color = Color.LightGray,
                            fontSize = 13.sp
                        )
                    }
                }

                if (phoneNumber.isNotEmpty()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = "Phone Number",
                            tint = Color.Gray,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = phoneNumber,
                            color = Color.LightGray,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Stats
            Row(
                modifier = Modifier.fillMaxWidth(0.85f),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ProfileStatColumn(count = reels.size.toString(), label = "Reels")
                ProfileStatColumn(count = "2.4K", label = "Followers")
                ProfileStatColumn(count = reels.sumOf { it.likesCount }.toString(), label = "Likes")
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = onLogout,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF262626)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(38.dp)
                    .testTag("logout_button")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ExitToApp,
                        contentDescription = "Logout",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Log Out", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Tabs
        TabRow(
            selectedTabIndex = selectedTabState,
            containerColor = Color.Transparent,
            contentColor = Color.White,
            indicator = { tabPositions ->
                TabRowDefaults.PrimaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabState]),
                    color = Color(0xFFE91E63)
                )
            }
        ) {
            Tab(
                selected = selectedTabState == 0,
                onClick = { selectedTabState = 0 },
                icon = { Icon(imageVector = Icons.Default.GridOn, contentDescription = "My Reels", tint = if (selectedTabState == 0) Color(0xFFE91E63) else Color.Gray) },
                text = { Text("My Reels", color = if (selectedTabState == 0) Color.White else Color.Gray, fontSize = 13.sp) }
            )
            Tab(
                selected = selectedTabState == 1,
                onClick = { selectedTabState = 1 },
                icon = { Icon(imageVector = Icons.Default.Favorite, contentDescription = "Liked", tint = if (selectedTabState == 1) Color(0xFFE91E63) else Color.Gray) },
                text = { Text("Liked", color = if (selectedTabState == 1) Color.White else Color.Gray, fontSize = 13.sp) }
            )
        }

        // Grid Content
        if (displayReels.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (selectedTabState == 0) "No uploads yet" else "No liked reels yet",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(2.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                items(displayReels, key = { it.id }) { reel ->
                    Box(
                        modifier = Modifier
                            .aspectRatio(0.75f)
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(Color(0xFF2C1E32), Color(0xFF1E2835))
                                )
                            )
                            .clickable { onReelSelect(reel) }
                    ) {
                        // Play Icon Overlay
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Watch",
                            tint = Color.White.copy(alpha = 0.5f),
                            modifier = Modifier
                                .size(32.dp)
                                .align(Alignment.Center)
                        )

                        // Reels description overlay on grid cells
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
                                    )
                                )
                                .padding(6.dp)
                                .align(Alignment.BottomStart)
                        ) {
                            Text(
                                text = reel.title,
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileStatColumn(
    count: String,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = count,
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            color = Color.Gray,
            fontSize = 12.sp
        )
    }
}
