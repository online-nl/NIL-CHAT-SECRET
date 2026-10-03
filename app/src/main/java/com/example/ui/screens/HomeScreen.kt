package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.InstallMobile
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.FirebaseManager
import com.example.model.CallSession
import com.example.model.ChatMessage
import com.example.model.UserProfile
import com.example.sound.SoundEffects
import com.example.ui.components.AvatarView
import com.example.ui.components.CyberButton
import com.example.ui.components.CyberCard
import com.example.ui.components.CyberTextField
import com.example.ui.components.TicksIndicator
import com.example.ui.components.VerifiedBadge
import com.example.ui.theme.CyberGold
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
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    currentUser: UserProfile,
    firebaseManager: FirebaseManager,
    onOpenSettings: () -> Unit,
    onOpenVerification: () -> Unit,
    onOpenPwaInfo: () -> Unit,
    onSelectChat: (UserProfile) -> Unit,
    activeChatUser: UserProfile? = null,
    onCloseActiveChat: () -> Unit = {}
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val scope = rememberCoroutineScope()

    var searchQuery by remember { mutableStateOf("") }
    var showAddContactDialog by remember { mutableStateOf(false) }

    // Known contacts list (in-memory loaded from recent sessions / searches)
    val knownContacts = remember { mutableStateListOf<UserProfile>() }

    // Observe active user profile changes (e.g. verified updates)
    val liveProfile by firebaseManager.observeUserProfile(currentUser.uid)
        .collectAsState(initial = currentUser)
    val profile = liveProfile ?: currentUser

    // Set up presence
    LaunchedEffect(profile.uid, profile.hideOnlineStatus) {
        firebaseManager.setupPresence(profile.uid, profile.hideOnlineStatus)
    }

    // Filter contacts based on search query
    val filteredContacts = remember(searchQuery, knownContacts.toList()) {
        if (searchQuery.isBlank()) {
            knownContacts.toList()
        } else {
            knownContacts.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                        it.chatId.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate950)
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("home_screen")
    ) {
        val isWideScreen = maxWidth >= 720.dp

        if (isWideScreen) {
            // Master-Detail Split Pane Layout (Telegram / WhatsApp Web style)
            Row(modifier = Modifier.fillMaxSize()) {
                // Left Panel: Contact List & Profile Card
                Box(
                    modifier = Modifier
                        .width(360.dp)
                        .fillMaxHeight()
                        .border(width = 1.dp, color = Slate800)
                ) {
                    ChatListPanel(
                        user = profile,
                        searchQuery = searchQuery,
                        onSearchChange = { searchQuery = it },
                        contacts = filteredContacts,
                        onContactClick = { contact ->
                            onSelectChat(contact)
                        },
                        onAddContactClick = { showAddContactDialog = true },
                        onOpenSettings = onOpenSettings,
                        onOpenVerification = onOpenVerification,
                        onOpenPwaInfo = onOpenPwaInfo,
                        onCopyChatId = {
                            clipboard.setText(AnnotatedString(profile.chatId))
                            Toast.makeText(context, "Chat ID copied!", Toast.LENGTH_SHORT).show()
                        }
                    )
                }

                // Right Panel: Active Chat or Empty state
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    if (activeChatUser != null) {
                        ChatScreen(
                            currentUser = profile,
                            otherUser = activeChatUser,
                            firebaseManager = firebaseManager,
                            onBack = onCloseActiveChat,
                            onStartCall = { isVideo ->
                                scope.launch {
                                    firebaseManager.initiateCall(
                                        profile.uid, activeChatUser.uid, profile.name, isVideo
                                    )
                                }
                            }
                        )
                    } else {
                        // Empty Chat Placeholder
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape)
                                    .background(Slate900)
                                    .border(1.dp, Slate800, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = null,
                                    tint = NeonEmerald,
                                    modifier = Modifier.size(40.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "NIL DARK SECURE GRID",
                                color = Slate100,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.5.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Select a conversation or tap + to discover a contact by Chat ID.",
                                color = Slate400,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        } else {
            // Mobile Responsive Layout: List View
            ChatListPanel(
                user = profile,
                searchQuery = searchQuery,
                onSearchChange = { searchQuery = it },
                contacts = filteredContacts,
                onContactClick = { contact ->
                    onSelectChat(contact)
                },
                onAddContactClick = { showAddContactDialog = true },
                onOpenSettings = onOpenSettings,
                onOpenVerification = onOpenVerification,
                onOpenPwaInfo = onOpenPwaInfo,
                onCopyChatId = {
                    clipboard.setText(AnnotatedString(profile.chatId))
                    Toast.makeText(context, "Chat ID copied: ${profile.chatId}", Toast.LENGTH_SHORT).show()
                }
            )
        }

        // Add Contact Dialog
        if (showAddContactDialog) {
            AddContactDialog(
                myUid = profile.uid,
                firebaseManager = firebaseManager,
                onDismiss = { showAddContactDialog = false },
                onStartChat = { contact ->
                    if (knownContacts.none { it.uid == contact.uid }) {
                        knownContacts.add(contact)
                    }
                    onSelectChat(contact)
                }
            )
        }
    }
}

@Composable
fun ChatListPanel(
    user: UserProfile,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    contacts: List<UserProfile>,
    onContactClick: (UserProfile) -> Unit,
    onAddContactClick: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenVerification: () -> Unit,
    onOpenPwaInfo: () -> Unit,
    onCopyChatId: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Bar with Brand & Profile Pill
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Slate900)
                    .border(1.dp, Slate800)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Logo & Name
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(NeonEmerald)
                            .border(1.dp, EmeraldGlow, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = "Logo",
                            tint = Slate950,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "NIL DARK",
                                color = Slate100,
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp,
                                letterSpacing = 1.sp
                            )
                            if (user.verified) {
                                Spacer(modifier = Modifier.width(4.dp))
                                VerifiedBadge(isVipGold = user.verifyPackage.contains("VIP"))
                            }
                        }
                        Text(
                            text = if (user.hideOnlineStatus) "Ghost Mode" else "Stealth Node",
                            color = if (user.hideOnlineStatus) Slate400 else NeonEmerald,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Header Action Buttons
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onOpenPwaInfo) {
                        Icon(
                            imageVector = Icons.Default.InstallMobile,
                            contentDescription = "PWA Info",
                            tint = Slate400,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(onClick = onOpenSettings, modifier = Modifier.testTag("btn_settings")) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = Slate400,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // User Profile Card with Copyable Chat ID (Mandatory Left Panel element)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Slate900)
                    .border(1.dp, Slate800, RoundedCornerShape(12.dp))
                    .padding(12.dp)
                    .testTag("user_profile_card")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AvatarView(
                        name = user.name,
                        photoUrl = user.photo,
                        size = 44.dp,
                        isOnline = !user.hideOnlineStatus,
                        showOnlineStatus = true
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = user.name,
                                color = Slate100,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            if (user.verified) {
                                Spacer(modifier = Modifier.width(4.dp))
                                VerifiedBadge(isVipGold = user.verifyPackage.contains("VIP"))
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { onCopyChatId() }
                        ) {
                            Text(
                                text = "CHAT ID: ",
                                color = Slate400,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = user.chatId,
                                color = NeonEmerald,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy",
                                tint = NeonEmerald,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }

                    if (!user.verified) {
                        IconButton(
                            onClick = onOpenVerification,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(CyberGold.copy(alpha = 0.15f))
                                .border(1.dp, CyberGold.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = "Redeem VIP",
                                tint = CyberGold,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // Search Bar
            CyberTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                placeholder = "Search chats or Chat ID...",
                leadingIcon = Icons.Default.Search,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp),
                tag = "input_search_chats"
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Active Conversations List
            if (contacts.isEmpty()) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(Slate900)
                            .border(1.dp, Slate800, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = Slate600,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "NO CONVERSATIONS YET",
                        color = Slate400,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Tap the + button to search and connect with any contact using their 8-character Chat ID.",
                        color = Slate600,
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .testTag("conversation_list")
                ) {
                    items(contacts, key = { it.uid }) { contact ->
                        ConversationListItem(
                            contact = contact,
                            onClick = { onContactClick(contact) }
                        )
                    }
                }
            }
        }

        // Add Contact Floating Action Button
        FloatingActionButton(
            onClick = onAddContactClick,
            containerColor = NeonEmerald,
            contentColor = Slate950,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("fab_add_contact")
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Add Contact via Chat ID")
        }
    }
}

@Composable
fun ConversationListItem(
    contact: UserProfile,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AvatarView(
            name = contact.name,
            photoUrl = contact.photo,
            size = 48.dp,
            isOnline = !contact.hideOnlineStatus,
            showOnlineStatus = !contact.hideOnlineStatus
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = contact.name,
                        color = Slate100,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (contact.verified) {
                        Spacer(modifier = Modifier.width(4.dp))
                        VerifiedBadge(isVipGold = contact.verifyPackage.contains("VIP"))
                    }
                }
                Text(
                    text = "ID: ${contact.chatId}",
                    color = NeonEmerald,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = contact.bio.ifEmpty { "Encrypted connection ready." },
                color = Slate400,
                fontSize = 12.sp,
                maxLines = 1
            )
        }
    }
}
