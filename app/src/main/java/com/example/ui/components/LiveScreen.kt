package com.example.ui.components

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

// Mock Live Channel Data
data class LiveChannel(
    val id: Int,
    val creatorName: String,
    val title: String,
    val category: String,
    val initialViewers: Int,
    val avatarUrl: String,
    val videoUrl: String
)

val mockChannels = listOf(
    LiveChannel(
        id = 1,
        creatorName = "Gamer_Valkyrie",
        title = "🔴 RANKED CHILL RUNS! NEW UPDATE! 🎮 #gaming #gamer",
        category = "Gaming",
        initialViewers = 2341,
        avatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=150&q=80",
        videoUrl = "https://storage.googleapis.com/gtv-videos-bucket/sample/SubaruOutbackOnStreetAndDirt.mp4"
    ),
    LiveChannel(
        id = 2,
        creatorName = "Chef_Gordon_Clone",
        title = "🍳 COOKING SECRETS FOR PERFECT STEAK AND SOUFFLE! AMA!",
        category = "Food & Drink",
        initialViewers = 1120,
        avatarUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=150&q=80",
        videoUrl = "https://storage.googleapis.com/gtv-videos-bucket/sample/WeAreGoingOnBullrun.mp4"
    ),
    LiveChannel(
        id = 3,
        creatorName = "CyberTech_Reviewer",
        title = "📱 UNBOXING THE NEWEST FOLDABLE HOLOGRAPHIC SMARTPHONE!",
        category = "Science & Tech",
        initialViewers = 3912,
        avatarUrl = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=150&q=80",
        videoUrl = "https://storage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4"
    )
)

// Simulated live chat message
data class LiveChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val senderName: String,
    val message: String,
    val color: Color = Color.White,
    val isGift: Boolean = false,
    val giftName: String? = null
)

// Flying Hearts
data class FlyingHeart(
    val id: String = java.util.UUID.randomUUID().toString(),
    val xOffset: Float,
    val color: Color,
    val scale: Float
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveScreen(
    currentUsername: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var activeMode by remember { mutableStateOf("watch") } // "watch" or "golive"

    // Go Live state variables
    var showCountdown by remember { mutableStateOf(false) }
    var countdownValue by remember { mutableStateOf(3) }
    var isBroadcasting by remember { mutableStateOf(false) }
    var broadcastTitle by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Just Chatting") }
    var myBroadcastViewers by remember { mutableStateOf(5) }
    var myBroadcastHearts by remember { mutableStateOf(0) }
    var myBroadcastDuration by remember { mutableStateOf(0) }

    // Watch mode state variables
    var selectedChannelIndex by remember { mutableStateOf(0) }
    val currentChannel = mockChannels[selectedChannelIndex]
    var viewerCount by remember { mutableStateOf(currentChannel.initialViewers) }
    var isFollowing by remember { mutableStateOf(false) }

    // Floating/Flying Hearts State
    val flyingHearts = remember { mutableStateListOf<FlyingHeart>() }

    // Chat Message List
    val chatMessages = remember { mutableStateListOf<LiveChatMessage>() }
    var customChatMessage by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    // Reset watch states on channel change
    LaunchedEffect(selectedChannelIndex) {
        viewerCount = currentChannel.initialViewers
        isFollowing = false
        chatMessages.clear()
        // Add initial mock chats
        chatMessages.add(LiveChatMessage(senderName = "System", message = "Welcome to the Live Room! Keep the conversation polite and friendly.", color = Color(0xFF00FFCC)))
        chatMessages.add(LiveChatMessage(senderName = "TechFanatic", message = "Omg finally live!", color = Color(0xFFFFC107)))
        chatMessages.add(LiveChatMessage(senderName = "Spammer_Max", message = "GREETINGS FROM BERLIN! 👋👋", color = Color(0xFFE91E63)))
    }

    // Fluctuating viewers & automatic mock chats in Watch Mode
    LaunchedEffect(selectedChannelIndex, activeMode) {
        if (activeMode == "watch") {
            while (true) {
                delay(Random.nextLong(1500, 3000))
                // Viewers fluctuation
                val delta = Random.nextInt(-15, 20)
                viewerCount = (viewerCount + delta).coerceAtLeast(10)

                // Add random fan chat
                val fanNames = listOf("Emma_Reels", "SuperCoder", "NachoChef", "JohnD", "Lara_Croft", "PixelArtist", "TockFan_99", "MemeLord")
                val fanColors = listOf(Color(0xFF00FFCC), Color(0xFFFFE082), Color(0xFF81D4FA), Color(0xFFF48FB1), Color(0xFFA5D6A7), Color(0xFFCE93D8))
                val comments = listOf(
                    "This is absolutely amazing! 🔥",
                    "Can you say hello to my friend Leo?",
                    "Wow the quality is so good!",
                    "Love this livestream so much!",
                    "Who else is watching this in 2026? 😂",
                    "Wait, what is happening? hahah",
                    "Huge support from Canada! 🇨🇦",
                    "Absolute masterpiece! 😍"
                )
                chatMessages.add(
                    LiveChatMessage(
                        senderName = fanNames.random(),
                        message = comments.random(),
                        color = fanColors.random()
                    )
                )

                // Limit chat history to 50 items to keep performance high
                if (chatMessages.size > 50) {
                    chatMessages.removeAt(0)
                }

                // Autoscroll to bottom
                scope.launch {
                    if (chatMessages.isNotEmpty()) {
                        listState.animateScrollToItem(chatMessages.size - 1)
                    }
                }
            }
        }
    }

    // Fluctuating viewers & automated fan chats in Broadcaster Mode
    LaunchedEffect(isBroadcasting) {
        if (isBroadcasting) {
            myBroadcastViewers = 12
            myBroadcastHearts = 0
            myBroadcastDuration = 0
            chatMessages.clear()
            chatMessages.add(LiveChatMessage(senderName = "System", message = "You are now broadcasting live! Feel free to say hello to your viewers.", color = Color(0xFF00FFCC)))

            // Live stream duration tick
            launch {
                while (isBroadcasting) {
                    delay(1000)
                    myBroadcastDuration++
                }
            }

            // Viewers & chat simulation for MY stream
            launch {
                while (isBroadcasting) {
                    delay(Random.nextLong(1200, 2400))
                    // Viewer count grows over time
                    val gain = Random.nextInt(2, 6)
                    myBroadcastViewers += gain

                    // Generate automatic heart click from fan
                    if (Random.nextBoolean()) {
                        myBroadcastHearts += 1
                        flyingHearts.add(
                            FlyingHeart(
                                xOffset = Random.nextFloat() * 100 - 50,
                                color = listOf(Color(0xFFE91E63), Color(0xFF00FFCC), Color(0xFFFFEB3B), Color(0xFFE040FB)).random(),
                                scale = Random.nextFloat() * 0.4f + 0.8f
                            )
                        )
                    }

                    // Simulated fan comments on my stream
                    val viewerComments = listOf(
                        "You are doing great!",
                        "Subscribed! Love your reels!",
                        "Hey creator! Big fan of MyTock2!",
                        "Can you show us your workspace?",
                        "Collab soon please!",
                        "Cool live category choice!",
                        "Let's goooo! 🔥🔥🔥",
                        "Sharing the stream!"
                    )
                    val fanNames = listOf("AlphaReeler", "NeonGlow", "TockUser20", "SwiftGamer", "SkyHigh")
                    val fanColors = listOf(Color(0xFF00FFCC), Color(0xFFFFC107), Color(0xFF03A9F4), Color(0xFFE040FB))

                    chatMessages.add(
                        LiveChatMessage(
                            senderName = fanNames.random(),
                            message = viewerComments.random(),
                            color = fanColors.random()
                        )
                    )

                    if (chatMessages.size > 50) {
                        chatMessages.removeAt(0)
                    }

                    scope.launch {
                        if (chatMessages.isNotEmpty()) {
                            listState.animateScrollToItem(chatMessages.size - 1)
                        }
                    }
                }
            }
        }
    }

    // Cleanup flying hearts over time
    LaunchedEffect(flyingHearts.size) {
        if (flyingHearts.isNotEmpty()) {
            delay(1200)
            if (flyingHearts.isNotEmpty()) {
                flyingHearts.removeAt(0)
            }
        }
    }

    // Go Live Countdown animation
    LaunchedEffect(showCountdown) {
        if (showCountdown) {
            countdownValue = 3
            while (countdownValue > 0) {
                delay(1000)
                countdownValue--
            }
            showCountdown = false
            isBroadcasting = true
            activeMode = "golive_active"
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        if (activeMode == "watch") {
            // Watch Live stream layer
            Box(modifier = Modifier.fillMaxSize()) {
                // High-fidelity background Video Player
                VideoPlayer(
                    videoUrl = currentChannel.videoUrl,
                    isActive = true,
                    modifier = Modifier.fillMaxSize()
                )

                // Immersive Overlay Gradient
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.5f),
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.75f)
                                )
                            )
                        )
                )

                // TOP ROW OVERLAY: Creator & Viewers
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(top = 16.dp, start = 16.dp, end = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Creator Card
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(30.dp))
                            .background(Color.Black.copy(alpha = 0.6f))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = currentChannel.avatarUrl,
                            contentDescription = "Avatar",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .border(1.dp, Color(0xFFE91E63), CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = currentChannel.creatorName,
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = currentChannel.category,
                                color = Color(0xFF00FFCC),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isFollowing) Color.Gray.copy(alpha = 0.5f) else Color(0xFFE91E63))
                                .clickable {
                                    isFollowing = !isFollowing
                                    Toast
                                        .makeText(
                                            context,
                                            if (isFollowing) "Followed ${currentChannel.creatorName}!" else "Unfollowed",
                                            Toast.LENGTH_SHORT
                                        )
                                        .show()
                                }
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (isFollowing) "Following" else "Follow",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Live Status + Viewer Count
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Flashing Live Badge
                        val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                        val pulseAlpha by infiniteTransition.animateFloat(
                            initialValue = 0.4f,
                            targetValue = 1.0f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(1000, easing = LinearEasing),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "live_pulse"
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFFFF0055).copy(alpha = pulseAlpha))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "LIVE",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black
                            )
                        }

                        // Viewer Count
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(30.dp))
                                .background(Color.Black.copy(alpha = 0.6f))
                                .padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Viewers",
                                tint = Color.LightGray,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${viewerCount}",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // CHANNEL SWITCHER (Floating Overlay at Top Right)
                Column(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 90.dp, end = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "Next Channel",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    IconButton(
                        onClick = {
                            selectedChannelIndex = (selectedChannelIndex + 1) % mockChannels.size
                        },
                        modifier = Modifier
                            .size(46.dp)
                            .background(Color.Black.copy(alpha = 0.7f), CircleShape)
                            .border(1.dp, Color(0xFF00FFCC).copy(alpha = 0.5f), CircleShape)
                            .testTag("next_live_channel")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Next Channel",
                            tint = Color(0xFF00FFCC),
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // Mode Switcher: Watch to Go Live
                    Button(
                        onClick = { activeMode = "setup" },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE91E63)),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .height(34.dp)
                            .testTag("go_live_setup_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Videocam,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Go Live", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // BOTTOM DECORATIVE / INFO LAYER
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .padding(bottom = 16.dp, start = 16.dp, end = 16.dp)
                ) {
                    // Channel Title
                    Text(
                        text = currentChannel.title,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .fillMaxWidth(0.75f)
                            .padding(bottom = 12.dp)
                    )

                    // LIVE CHAT DISPLAY
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.75f)
                            .height(200.dp)
                            .padding(bottom = 12.dp)
                    ) {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(chatMessages) { chat ->
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            if (chat.isGift) Color(0xFFE91E63).copy(alpha = 0.2f)
                                            else Color.Black.copy(alpha = 0.4f)
                                        )
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "${chat.senderName}: ",
                                        color = chat.color,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                    Text(
                                        text = chat.message,
                                        color = Color.White,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }

                    // INPUT ACTION BAR: Chat input, Gifts, Heart/Likes
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Chat Input Bar
                        OutlinedTextField(
                            value = customChatMessage,
                            onValueChange = { customChatMessage = it },
                            placeholder = { Text("Say something...", color = Color.Gray, fontSize = 13.sp) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedContainerColor = Color.Black.copy(alpha = 0.6f),
                                unfocusedContainerColor = Color.Black.copy(alpha = 0.6f),
                                focusedBorderColor = Color(0xFFE91E63),
                                unfocusedBorderColor = Color.White.copy(alpha = 0.15f)
                            ),
                            shape = RoundedCornerShape(30.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("live_chat_input"),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                            keyboardActions = KeyboardActions(onSend = {
                                if (customChatMessage.trim().isNotEmpty()) {
                                    chatMessages.add(
                                        LiveChatMessage(
                                            senderName = currentUsername.ifEmpty { "Me" },
                                            message = customChatMessage.trim(),
                                            color = Color(0xFF00FFCC)
                                        )
                                    )
                                    customChatMessage = ""
                                    scope.launch {
                                        delay(100)
                                        listState.animateScrollToItem(chatMessages.size - 1)
                                    }
                                }
                            })
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        // Gift Box Button (Simulates Gift sending)
                        IconButton(
                            onClick = {
                                val gifts = listOf("🌹 Rose", "👑 Crown", "🚀 Space Rocket", "💎 Diamond")
                                val chosenGift = gifts.random()
                                chatMessages.add(
                                    LiveChatMessage(
                                        senderName = currentUsername.ifEmpty { "Me" },
                                        message = "sent a $chosenGift!",
                                        color = Color(0xFFFF4081),
                                        isGift = true,
                                        giftName = chosenGift
                                    )
                                )
                                Toast.makeText(context, "Gift sent: $chosenGift! 🎁", Toast.LENGTH_SHORT).show()
                                scope.launch {
                                    delay(100)
                                    listState.animateScrollToItem(chatMessages.size - 1)
                                }
                            },
                            modifier = Modifier
                                .size(46.dp)
                                .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                .testTag("live_gift_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.CardGiftcard,
                                contentDescription = "Send Gift",
                                tint = Color(0xFFFFEB3B),
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Heart Button (Triggers flying hearts)
                        IconButton(
                            onClick = {
                                flyingHearts.add(
                                    FlyingHeart(
                                        xOffset = Random.nextFloat() * 100 - 50,
                                        color = listOf(Color(0xFFE91E63), Color(0xFF00FFCC), Color(0xFFFFEB3B), Color(0xFFE040FB), Color.Red).random(),
                                        scale = Random.nextFloat() * 0.4f + 0.8f
                                    )
                                )
                            },
                            modifier = Modifier
                                .size(46.dp)
                                .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                .testTag("live_heart_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = "Like Live",
                                tint = Color(0xFFE91E63),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }

                // Hearts animation render layer
                HeartsRenderLayer(hearts = flyingHearts)
            }
        } else if (activeMode == "setup") {
            // GO LIVE SETUP SCREEN
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF0F0F0F))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Videocam,
                        contentDescription = "Videocam",
                        tint = Color(0xFFE91E63),
                        modifier = Modifier.size(72.dp)
                    )

                    Text(
                        text = "Prepare Your Broadcast",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "Create real-time connections, host interactively, and build your follower base instantly on MyTock2.",
                        color = Color.Gray,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Title Input Box
                    OutlinedTextField(
                        value = broadcastTitle,
                        onValueChange = { broadcastTitle = it },
                        label = { Text("Stream Title") },
                        placeholder = { Text("e.g. Chatting with fans!") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF00FFCC),
                            unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                            focusedLabelColor = Color(0xFF00FFCC),
                            unfocusedLabelColor = Color.Gray
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("broadcast_title_input"),
                        singleLine = true
                    )

                    // Categories Selector
                    val categories = listOf("Just Chatting", "Gaming", "Music", "Tech", "ASMR")
                    Text(
                        text = "Select Stream Category",
                        color = Color.LightGray,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.align(Alignment.Start)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        categories.forEach { cat ->
                            val isSelected = cat == selectedCategory
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(if (isSelected) Color(0xFF00FFCC) else Color(0xFF262626))
                                    .clickable { selectedCategory = cat }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = cat,
                                    color = if (isSelected) Color.Black else Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Cancel
                        OutlinedButton(
                            onClick = { activeMode = "watch" },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                        ) {
                            Text("Cancel", fontWeight = FontWeight.Bold)
                        }

                        // Go Live Now
                        Button(
                            onClick = {
                                if (broadcastTitle.trim().isEmpty()) {
                                    Toast.makeText(context, "Please enter a stream title first!", Toast.LENGTH_SHORT).show()
                                } else {
                                    showCountdown = true
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFE91E63),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .testTag("start_broadcast_button")
                        ) {
                            Text("Start Stream", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else if (isBroadcasting || activeMode == "golive_active") {
            // THE LIVE BROADCASTER SCREEN
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF161616)) // Dark background representing broadcaster interface
            ) {
                // Background visualizer to represent active camera stream or animated audio pulses!
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.sweepGradient(
                                colors = listOf(Color(0xFF0F0014), Color(0xFF1E0026), Color(0xFF001E1A), Color(0xFF0F0014))
                            )
                        )
                )

                // Beautiful moving sine waves indicating audio levels
                CameraFeedSimulator()

                // Top Panel info overlay
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(top = 16.dp, start = 16.dp, end = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color.Red)
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "LIVE",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black
                            )
                        }

                        // Stream Duration Timer
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(30.dp))
                                .background(Color.Black.copy(alpha = 0.6f))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = formatDuration(myBroadcastDuration),
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Live statistics overlay (Viewer & Heart meters)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Viewer icon + count
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(30.dp))
                                .background(Color.Black.copy(alpha = 0.6f))
                                .padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = Color(0xFF00FFCC),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "$myBroadcastViewers",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Stop Stream button
                        IconButton(
                            onClick = {
                                isBroadcasting = false
                                activeMode = "summary"
                            },
                            modifier = Modifier
                                .size(34.dp)
                                .background(Color(0xFFFF3B30), CircleShape)
                                .testTag("stop_broadcast_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Stop Broadcast",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                // BOTTOM CHAT CONTAINER
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .padding(bottom = 16.dp, start = 16.dp, end = 16.dp)
                ) {
                    Text(
                        text = "Broadcasting: $broadcastTitle",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Text(
                        text = "Category: $selectedCategory",
                        color = Color(0xFF00FFCC),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // Stream Chats
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.75f)
                            .height(200.dp)
                            .padding(bottom = 12.dp)
                    ) {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(chatMessages) { chat ->
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color.Black.copy(alpha = 0.4f))
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "${chat.senderName}: ",
                                        color = chat.color,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                    Text(
                                        text = chat.message,
                                        color = Color.White,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }

                    // Direct response action button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        OutlinedTextField(
                            value = customChatMessage,
                            onValueChange = { customChatMessage = it },
                            placeholder = { Text("Speak to viewers...", color = Color.Gray, fontSize = 13.sp) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedContainerColor = Color.Black.copy(alpha = 0.6f),
                                unfocusedContainerColor = Color.Black.copy(alpha = 0.6f),
                                focusedBorderColor = Color(0xFF00FFCC),
                                unfocusedBorderColor = Color.White.copy(alpha = 0.15f)
                            ),
                            shape = RoundedCornerShape(30.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                            keyboardActions = KeyboardActions(onSend = {
                                if (customChatMessage.trim().isNotEmpty()) {
                                    chatMessages.add(
                                        LiveChatMessage(
                                            senderName = currentUsername.ifEmpty { "Host" } + " 👑",
                                            message = customChatMessage.trim(),
                                            color = Color(0xFFE91E63)
                                        )
                                    )
                                    customChatMessage = ""
                                    scope.launch {
                                        delay(100)
                                        listState.animateScrollToItem(chatMessages.size - 1)
                                    }
                                }
                            })
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        // Heart click simulator for host
                        IconButton(
                            onClick = {
                                myBroadcastHearts += 1
                                flyingHearts.add(
                                    FlyingHeart(
                                        xOffset = Random.nextFloat() * 100 - 50,
                                        color = listOf(Color(0xFFE91E63), Color(0xFF00FFCC), Color(0xFFFFEB3B), Color(0xFFE040FB)).random(),
                                        scale = Random.nextFloat() * 0.4f + 0.8f
                                    )
                                )
                            },
                            modifier = Modifier
                                .size(46.dp)
                                .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = null,
                                tint = Color(0xFFE91E63),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }

                // Render flying hearts for broadcaster
                HeartsRenderLayer(hearts = flyingHearts)
            }
        } else if (activeMode == "summary") {
            // POST-LIVE SUMMARY REPORT
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF0A0A0A))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0xFF161616))
                        .padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Text(
                        text = "Broadcast Summary",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "Amazing stream! Here is how your audience responded on MyTock2:",
                        color = Color.Gray,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )

                    HorizontalDivider(color = Color.White.copy(alpha = 0.08f))

                    // Grid stats
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                            Text("Duration", color = Color.Gray, fontSize = 11.sp)
                            Text(formatDuration(myBroadcastDuration), color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                            Text("Peak Viewers", color = Color.Gray, fontSize = 11.sp)
                            Text("$myBroadcastViewers", color = Color(0xFF00FFCC), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                            Text("New Followers", color = Color.Gray, fontSize = 11.sp)
                            Text("+${Random.nextInt(5, 18)}", color = Color(0xFFFFEB3B), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                            Text("Total Hearts", color = Color.Gray, fontSize = 11.sp)
                            Text("$myBroadcastHearts ❤️", color = Color(0xFFE91E63), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    HorizontalDivider(color = Color.White.copy(alpha = 0.08f))

                    Button(
                        onClick = {
                            activeMode = "watch"
                            broadcastTitle = ""
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE91E63)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("summary_done_button")
                    ) {
                        Text("Done", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // COUNTDOWN OVERLAY DIALOG (Watch Mode to Go Live countdown)
        AnimatedVisibility(
            visible = showCountdown,
            enter = fadeIn() + scaleIn(initialScale = 0.8f),
            exit = fadeOut() + scaleOut(targetScale = 0.8f)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.9f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "GET READY!",
                        color = Color.LightGray,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "$countdownValue",
                        color = Color(0xFFE91E63),
                        fontSize = 80.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}

@Composable
fun HeartsRenderLayer(
    hearts: List<FlyingHeart>
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {} // Keep clicks unblocking
    ) {
        hearts.forEach { heart ->
            var targetState by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) {
                targetState = true
            }

            val yOffset by animateFloatAsState(
                targetValue = if (targetState) -350f else 0f,
                animationSpec = tween(1200, easing = LinearEasing),
                label = "yOffset"
            )
            val alpha by animateFloatAsState(
                targetValue = if (targetState) 0f else 1.0f,
                animationSpec = tween(1200, easing = LinearEasing),
                label = "alpha"
            )

            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = null,
                tint = heart.color.copy(alpha = alpha),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 90.dp, end = 24.dp)
                    .offset(x = heart.xOffset.dp, y = yOffset.dp)
                    .scale(heart.scale)
                    .size(24.dp)
            )
        }
    }
}

@Composable
fun CameraFeedSimulator() {
    val infiniteTransition = rememberInfiniteTransition(label = "cameraPulse")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_value"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        // Draw a decorative sci-fi networking connection circle to represent stream broadcasting
        drawCircle(
            color = Color(0xFF00FFCC).copy(alpha = 0.05f * pulse),
            radius = width * 0.4f * pulse,
            center = Offset(width / 2, height / 2.5f)
        )

        drawCircle(
            color = Color(0xFFE91E63).copy(alpha = 0.03f * (1.5f - pulse)),
            radius = width * 0.6f * (1.5f - pulse),
            center = Offset(width / 2, height / 2.5f)
        )

        // Draw decorative targeting brackets to look like a pro camera HUD
        val lineLen = 40f
        val padding = 60f
        val strokeWidth = 4f
        val strokeColor = Color.White.copy(alpha = 0.2f)

        // Top-Left corner bracket
        drawLine(strokeColor, Offset(padding, padding), Offset(padding + lineLen, padding), strokeWidth)
        drawLine(strokeColor, Offset(padding, padding), Offset(padding, padding + lineLen), strokeWidth)

        // Top-Right corner bracket
        drawLine(strokeColor, Offset(width - padding, padding), Offset(width - padding - lineLen, padding), strokeWidth)
        drawLine(strokeColor, Offset(width - padding, padding), Offset(width - padding, padding + lineLen), strokeWidth)

        // Bottom-Left corner bracket
        drawLine(strokeColor, Offset(padding, height - padding - 200f), Offset(padding + lineLen, height - padding - 200f), strokeWidth)
        drawLine(strokeColor, Offset(padding, height - padding - 200f), Offset(padding, height - padding - 200f - lineLen), strokeWidth)

        // Bottom-Right corner bracket
        drawLine(strokeColor, Offset(width - padding, height - padding - 200f), Offset(width - padding - lineLen, height - padding - 200f), strokeWidth)
        drawLine(strokeColor, Offset(width - padding, height - padding - 200f), Offset(width - padding, height - padding - 200f - lineLen), strokeWidth)
    }
}

private fun formatDuration(seconds: Int): String {
    val m = (seconds / 60) % 60
    val s = seconds % 60
    return String.format("%02d:%02d", m, s)
}
