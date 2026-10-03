package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.FirebaseManager
import com.example.model.ChatMessage
import com.example.model.UserProfile
import com.example.sound.SoundEffects
import com.example.ui.components.AvatarView
import com.example.ui.components.TicksIndicator
import com.example.ui.components.VerifiedBadge
import com.example.ui.theme.CyberGold
import com.example.ui.theme.CyberRed
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldGlow
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate850
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    currentUser: UserProfile,
    otherUser: UserProfile,
    firebaseManager: FirebaseManager,
    onBack: () -> Unit,
    onStartCall: (isVideo: Boolean) -> Unit
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    val conversationId = ChatMessage.conversationId(currentUser.uid, otherUser.uid)

    // Observe messages flow
    val messages by firebaseManager.observeMessages(currentUser.uid, otherUser.uid)
        .collectAsState(initial = emptyList())

    // Observe other user online status
    val statusPair by firebaseManager.observeUserStatus(otherUser.uid)
        .collectAsState(initial = Pair("offline", 0L))

    // Observe typing indicator
    val isOtherTyping by firebaseManager.observeTyping(conversationId, otherUser.uid)
        .collectAsState(initial = false)

    var inputText by remember { mutableStateOf("") }
    var typingJob by remember { mutableStateOf<Job?>(null) }
    var selectedMessageForMenu by remember { mutableStateOf<ChatMessage?>(null) }
    var showEmojiPicker by remember { mutableStateOf(false) }

    val isOnline = !otherUser.hideOnlineStatus && statusPair.first == "online"

    // Mark unseen messages as seen
    LaunchedEffect(messages) {
        val unseenIds = messages
            .filter { it.receiverId == currentUser.uid && it.seenAt == 0L }
            .map { it.id }
        if (unseenIds.isNotEmpty()) {
            firebaseManager.markMessagesSeen(currentUser.uid, otherUser.uid, unseenIds)
        }
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    // Reset typing on exit
    DisposableEffect(Unit) {
        onDispose {
            firebaseManager.setTyping(conversationId, currentUser.uid, false)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate950)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .testTag("chat_screen")
    ) {
        // Chat Header
        TopAppBar(
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable {
                        clipboard.setText(AnnotatedString(otherUser.chatId))
                        Toast.makeText(context, "Contact Chat ID: ${otherUser.chatId}", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    AvatarView(
                        name = otherUser.name,
                        photoUrl = otherUser.photo,
                        size = 40.dp,
                        isOnline = isOnline,
                        showOnlineStatus = !otherUser.hideOnlineStatus
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = otherUser.name,
                                color = Slate100,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                maxLines = 1
                            )
                            if (otherUser.verified) {
                                Spacer(modifier = Modifier.width(6.dp))
                                VerifiedBadge(isVipGold = otherUser.verifyPackage.contains("VIP"))
                            }
                        }

                        if (isOtherTyping) {
                            Text(
                                text = "typing...",
                                color = NeonEmerald,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        } else {
                            Text(
                                text = if (isOnline) "online" else "ID: ${otherUser.chatId}",
                                color = if (isOnline) NeonEmerald else Slate400,
                                fontSize = 11.sp,
                                fontFamily = if (!isOnline) FontFamily.Monospace else FontFamily.Default
                            )
                        }
                    }
                }
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Slate100
                    )
                }
            },
            actions = {
                // Audio Call Button
                IconButton(
                    onClick = { onStartCall(false) },
                    modifier = Modifier.testTag("btn_audio_call")
                ) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = "Voice Call",
                        tint = NeonEmerald
                    )
                }
                // Video Call Button
                IconButton(
                    onClick = { onStartCall(true) },
                    modifier = Modifier.testTag("btn_video_call")
                ) {
                    Icon(
                        imageVector = Icons.Default.Videocam,
                        contentDescription = "Video Call",
                        tint = NeonEmerald
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Slate900
            )
        )

        // Messages List
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (messages.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Slate900)
                            .border(1.dp, Slate800, RoundedCornerShape(12.dp))
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "NO PRIOR LOGS FOUND",
                                color = Slate400,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Messages are synchronized in realtime via Firebase RTDB.",
                                color = Slate600,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(messages, key = { it.id }) { message ->
                        val isMe = message.senderId == currentUser.uid
                        MessageBubble(
                            message = message,
                            isMe = isMe,
                            onLongPress = {
                                selectedMessageForMenu = message
                            }
                        )
                    }
                }
            }

            // Realtime Typing Indicator floating chip
            if (isOtherTyping) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 16.dp, bottom = 8.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Slate900.copy(alpha = 0.9f))
                        .border(1.dp, NeonEmerald.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "${otherUser.name} is typing...",
                        color = NeonEmerald,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Message context menu (Delete for me / Delete for everyone / Copy)
            DropdownMenu(
                expanded = selectedMessageForMenu != null,
                onDismissRequest = { selectedMessageForMenu = null },
                modifier = Modifier.background(Slate900)
            ) {
                val msg = selectedMessageForMenu
                if (msg != null) {
                    DropdownMenuItem(
                        text = { Text("Copy Text", color = Slate100) },
                        onClick = {
                            clipboard.setText(AnnotatedString(msg.text))
                            Toast.makeText(context, "Copied!", Toast.LENGTH_SHORT).show()
                            selectedMessageForMenu = null
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete for Me", color = Slate400) },
                        onClick = {
                            firebaseManager.deleteMessageForMe(conversationId, msg.id, currentUser.uid)
                            selectedMessageForMenu = null
                        }
                    )
                    if (msg.senderId == currentUser.uid) {
                        DropdownMenuItem(
                            text = { Text("Delete for Everyone", color = CyberRed) },
                            onClick = {
                                firebaseManager.deleteMessageForEveryone(conversationId, msg.id)
                                selectedMessageForMenu = null
                            }
                        )
                    }
                }
            }
        }

        // Quick Emoji Bar (Collapsible)
        AnimatedVisibility(visible = showEmojiPicker) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Slate900)
                    .border(1.dp, Slate800)
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                val emojis = listOf("👍", "🔥", "🛡️", "⚡", "🔒", "👀", "🚀", "❤️")
                for (emoji in emojis) {
                    Text(
                        text = emoji,
                        fontSize = 22.sp,
                        modifier = Modifier
                            .clickable {
                                inputText += emoji
                            }
                            .padding(4.dp)
                    )
                }
            }
        }

        // Bottom Input Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Slate900)
                .border(1.dp, Slate800)
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { showEmojiPicker = !showEmojiPicker },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.EmojiEmotions,
                    contentDescription = "Emojis",
                    tint = if (showEmojiPicker) NeonEmerald else Slate400
                )
            }

            IconButton(
                onClick = {
                    Toast.makeText(context, "Encrypted attachment payload attached.", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AttachFile,
                    contentDescription = "Attach",
                    tint = Slate400
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Text Input
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Slate950)
                    .border(1.dp, Slate700, RoundedCornerShape(20.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                BasicTextField(
                    value = inputText,
                    onValueChange = {
                        inputText = it
                        // Trigger typing flag
                        firebaseManager.setTyping(conversationId, currentUser.uid, true)
                        typingJob?.cancel()
                        typingJob = scope.launch {
                            delay(2500)
                            firebaseManager.setTyping(conversationId, currentUser.uid, false)
                        }
                    },
                    textStyle = TextStyle(color = Slate100, fontSize = 14.sp),
                    cursorBrush = SolidColor(NeonEmerald),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("chat_input_field"),
                    decorationBox = { innerTextField ->
                        if (inputText.isEmpty()) {
                            Text(
                                text = "Message #stealth...",
                                color = Slate600,
                                fontSize = 14.sp
                            )
                        }
                        innerTextField()
                    }
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Send Button
            IconButton(
                onClick = {
                    val text = inputText.trim()
                    if (text.isNotEmpty()) {
                        inputText = ""
                        firebaseManager.setTyping(conversationId, currentUser.uid, false)
                        scope.launch {
                            firebaseManager.sendMessage(currentUser.uid, otherUser.uid, text)
                            SoundEffects.playSentChime(currentUser.soundEnabled)
                        }
                    }
                },
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(if (inputText.isNotBlank()) NeonEmerald else Slate800)
                    .testTag("btn_send_message")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send",
                    tint = if (inputText.isNotBlank()) Slate950 else Slate600,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun MessageBubble(
    message: ChatMessage,
    isMe: Boolean,
    onLongPress: () -> Unit
) {
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val timeStr = remember(message.createdAt) {
        if (message.createdAt > 0) timeFormat.format(Date(message.createdAt)) else ""
    }

    val bubbleShape = if (isMe) {
        RoundedCornerShape(topStart = 16.dp, topEnd = 4.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
    } else {
        RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
    }

    val bgModifier = if (isMe) {
        Modifier.background(
            Brush.linearGradient(
                listOf(EmeraldDark, Color(0xFF064E3B))
            )
        ).border(1.dp, NeonEmerald.copy(alpha = 0.3f), bubbleShape)
    } else {
        Modifier.background(Slate900).border(1.dp, Slate800, bubbleShape)
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 290.dp)
                .clip(bubbleShape)
                .then(bgModifier)
                .pointerInput(Unit) {
                    detectTapGestures(onLongPress = { onLongPress() })
                }
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .testTag(if (isMe) "message_bubble_me" else "message_bubble_them")
        ) {
            Column {
                Text(
                    text = message.text,
                    color = Slate100,
                    fontSize = 14.sp,
                    lineHeight = 19.sp
                )

                Spacer(modifier = Modifier.height(3.dp))

                Row(
                    modifier = Modifier.align(Alignment.End),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = timeStr,
                        color = if (isMe) NeonEmerald.copy(alpha = 0.8f) else Slate600,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )

                    if (isMe) {
                        Spacer(modifier = Modifier.width(4.dp))
                        TicksIndicator(
                            delivered = message.deliveredAt > 0,
                            seen = message.seenAt > 0,
                            size = 13.dp
                        )
                    }
                }
            }
        }
    }
}
