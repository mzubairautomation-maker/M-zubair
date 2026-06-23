package com.example.ui.components

import android.widget.Toast
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.Reel

@Composable
fun FeedScreen(
    reels: List<Reel>,
    onLikeToggle: (Reel) -> Unit,
    onCommentClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    if (reels.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0xFF121212)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(24.dp)
            ) {
                CircularProgressIndicator(color = Color(0xFFE91E63))
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Loading reels...",
                    color = Color.LightGray,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        return
    }

    val pagerState = rememberPagerState(pageCount = { reels.size })

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Vertical Pager
        VerticalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            val reel = reels[page]
            // We consider the page active if it matches the current focused page of the pager
            val isPageActive = page == pagerState.currentPage

            Box(modifier = Modifier.fillMaxSize()) {
                // High performance Video Player
                VideoPlayer(
                    videoUrl = reel.videoUrl,
                    isActive = isPageActive,
                    modifier = Modifier.fillMaxSize()
                )

                // Immersive full-screen dark gradient covers to increase legibility of text details
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.4f),
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.65f)
                                )
                            )
                        )
                )

                // Interactions Overlay (Right side buttons)
                ReelInteractionsOverlay(
                    reel = reel,
                    onLikeClick = { onLikeToggle(reel) },
                    onCommentClick = { onCommentClick(reel.id) },
                    onShareClick = {
                        Toast.makeText(context, "Link copied to clipboard!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = 80.dp, end = 12.dp)
                )

                // Info Overlay (Creator details, sound disc, description at bottom-left)
                ReelInfoOverlay(
                    reel = reel,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth(0.82f)
                        .padding(bottom = 24.dp, start = 16.dp)
                )
            }
        }

        // Top Overlay Headers (Simple For You / Following Switch lookalike)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 54.dp, start = 16.dp, end = 16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Following",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable {
                    Toast.makeText(context, "Following feed coming soon!", Toast.LENGTH_SHORT).show()
                }
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "•",
                color = Color.White.copy(alpha = 0.4f),
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "For You",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .size(width = 24.dp, height = 2.dp)
                        .background(Color(0xFFE91E63))
                )
            }
        }
    }
}

@Composable
fun ReelInteractionsOverlay(
    reel: Reel,
    onLikeClick: () -> Unit,
    onCommentClick: () -> Unit,
    onShareClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Creator Avatar with pink plus button
        Box(
            modifier = Modifier
                .size(48.dp)
                .clickable { /* Profile interaction */ },
            contentAlignment = Alignment.BottomCenter
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .border(1.5.dp, Color.White, CircleShape)
                    .background(Color.Gray)
            ) {
                if (reel.creatorAvatar.isNotEmpty() && reel.creatorAvatar.startsWith("http")) {
                    AsyncImage(
                        model = reel.creatorAvatar,
                        contentDescription = "Avatar",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFFE91E63).copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = reel.creatorName.take(1).uppercase(),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }

            // Small Plus icon
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .background(Color(0xFFE91E63), CircleShape)
                    .border(1.dp, Color.Black, CircleShape)
                    .align(Alignment.BottomCenter),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "+",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 1.dp)
                )
            }
        }

        // Like Button
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            IconButton(
                onClick = onLikeClick,
                modifier = Modifier
                    .size(44.dp)
                    .testTag("like_button_${reel.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = "Like",
                    tint = if (reel.isLiked) Color(0xFFE91E63) else Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
            Text(
                text = formatCount(reel.likesCount),
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }

        // Comments Button
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            IconButton(
                onClick = onCommentClick,
                modifier = Modifier
                    .size(44.dp)
                    .testTag("comments_button_${reel.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.Comment,
                    contentDescription = "Comments",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
            Text(
                text = "...", // We load on-demand in the drawer or placeholder
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }

        // Share Button
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            IconButton(
                onClick = onShareClick,
                modifier = Modifier.size(44.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Share",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
            Text(
                text = formatCount(reel.sharesCount),
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun ReelInfoOverlay(
    reel: Reel,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            // Creator Name
            Text(
                text = "@${reel.creatorName.replace(" ", "_")}",
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Reel Title
            Text(
                text = reel.title,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Description
            Text(
                text = reel.description,
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 13.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Music label
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = "Music",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Original Audio - ${reel.creatorName}",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Beautiful Rotating Vinyl Disc Animation on the bottom right
        RotatingVinylDisc(
            modifier = Modifier.padding(start = 16.dp)
        )
    }
}

@Composable
fun RotatingVinylDisc(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "Vinyl rotation")
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = LinearEasing)
        ),
        label = "rotation"
    )

    Box(
        modifier = modifier
            .size(40.dp)
            .rotate(rotationAngle)
            .clip(CircleShape)
            .background(Color(0xFF1E1E1E))
            .border(2.dp, Color.White.copy(alpha = 0.15f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        // Draw vinyl lines using Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = Color.Black,
                radius = size.minDimension / 2f
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.12f),
                radius = size.minDimension * 0.4f,
                style = Stroke(width = 1.dp.toPx())
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.12f),
                radius = size.minDimension * 0.3f,
                style = Stroke(width = 1.dp.toPx())
            )
            // Central label
            drawCircle(
                color = Color(0xFFE91E63),
                radius = size.minDimension * 0.18f
            )
        }
        Icon(
            imageVector = Icons.Default.MusicNote,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(12.dp)
        )
    }
}

fun formatCount(count: Int): String {
    return when {
        count >= 1000000 -> "%.1fM".format(count / 1000000f)
        count >= 1000 -> "%.1fK".format(count / 1000f)
        else -> count.toString()
    }
}
