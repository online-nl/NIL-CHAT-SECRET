package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.model.UserProfile
import com.example.sound.SoundEffects
import com.example.ui.components.AvatarView
import com.example.ui.components.CyberButton
import com.example.ui.components.CyberCard
import com.example.ui.components.CyberTextField
import com.example.ui.components.VerifiedBadge
import com.example.ui.theme.CyberGold
import com.example.ui.theme.CyberRed
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate300
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate850
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    currentUser: UserProfile,
    firebaseManager: FirebaseManager,
    onBack: () -> Unit,
    onLogout: () -> Unit,
    onOpenVerification: () -> Unit,
    onOpenPwaInfo: () -> Unit
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val scope = rememberCoroutineScope()

    var name by remember { mutableStateOf(currentUser.name) }
    var bio by remember { mutableStateOf(currentUser.bio) }
    var photo by remember { mutableStateOf(currentUser.photo) }
    var ghostMode by remember { mutableStateOf(currentUser.hideOnlineStatus) }
    var soundEnabled by remember { mutableStateOf(currentUser.soundEnabled) }
    var isSaving by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "SETTINGS & PROFILE",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate100
                    )
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Slate950
                )
            )
        },
        containerColor = Slate950
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .testTag("settings_screen")
        ) {
            // User Header Card with Copyable Chat ID
            CyberCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = Slate900,
                borderColor = Slate800
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AvatarView(
                        name = name,
                        photoUrl = photo,
                        size = 72.dp,
                        showOnlineStatus = !ghostMode,
                        isOnline = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = name,
                            color = Slate100,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        if (currentUser.verified) {
                            Spacer(modifier = Modifier.width(6.dp))
                            VerifiedBadge(isVipGold = currentUser.verifyPackage.contains("VIP"))
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Copyable Chat ID pill
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Slate950)
                            .border(1.dp, NeonEmerald.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .clickable {
                                clipboard.setText(AnnotatedString(currentUser.chatId))
                                Toast.makeText(context, "Chat ID copied!", Toast.LENGTH_SHORT).show()
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "CHAT ID: ",
                            color = Slate400,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = currentUser.chatId,
                            color = NeonEmerald,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy",
                            tint = NeonEmerald,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Profile Editor
            Text(
                text = "IDENTITY & PROFILE",
                color = Slate400,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            CyberCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = Slate900,
                borderColor = Slate800
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    CyberTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = "CODENAME / DISPLAY NAME",
                        placeholder = "Your name",
                        leadingIcon = Icons.Default.Person,
                        tag = "input_edit_name"
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    CyberTextField(
                        value = bio,
                        onValueChange = { bio = it },
                        label = "STATUS / BIO",
                        placeholder = "Status note...",
                        leadingIcon = Icons.Default.Edit,
                        singleLine = false,
                        tag = "input_edit_bio"
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    CyberTextField(
                        value = photo,
                        onValueChange = { photo = it },
                        label = "AVATAR URL (OPTIONAL)",
                        placeholder = "https://... or leave empty for cyber monogram",
                        tag = "input_edit_photo"
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    CyberButton(
                        text = "SAVE PROFILE CHANGES",
                        onClick = {
                            isSaving = true
                            scope.launch {
                                firebaseManager.updateProfile(
                                    uid = currentUser.uid,
                                    name = name,
                                    bio = bio,
                                    photo = photo,
                                    hideOnlineStatus = ghostMode,
                                    soundEnabled = soundEnabled
                                )
                                isSaving = false
                                SoundEffects.playSentChime()
                                Toast.makeText(context, "Profile saved!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        isLoading = isSaving,
                        modifier = Modifier.fillMaxWidth(),
                        tag = "btn_save_profile"
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Privacy & Controls
            Text(
                text = "STEALTH & PRIVACY CONTROLS",
                color = Slate400,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            CyberCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = Slate900,
                borderColor = Slate800
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Ghost Mode
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.VisibilityOff,
                                contentDescription = null,
                                tint = NeonEmerald,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Ghost Mode (Hide Online Status)",
                                    color = Slate100,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "Appear completely offline to all contacts",
                                    color = Slate400,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Switch(
                            checked = ghostMode,
                            onCheckedChange = {
                                ghostMode = it
                                scope.launch {
                                    firebaseManager.setupPresence(currentUser.uid, it)
                                    firebaseManager.updateProfile(
                                        currentUser.uid, name, bio, photo, it, soundEnabled
                                    )
                                }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Slate950,
                                checkedTrackColor = NeonEmerald,
                                uncheckedTrackColor = Slate800
                            ),
                            modifier = Modifier.testTag("switch_ghost_mode")
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Sound alert toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = null,
                                tint = NeonEmerald,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Synthesizer Chimes & Sounds",
                                    color = Slate100,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "Notification beeps and incoming ringtones",
                                    color = Slate400,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Switch(
                            checked = soundEnabled,
                            onCheckedChange = {
                                soundEnabled = it
                                scope.launch {
                                    firebaseManager.updateProfile(
                                        currentUser.uid, name, bio, photo, ghostMode, it
                                    )
                                }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Slate950,
                                checkedTrackColor = NeonEmerald,
                                uncheckedTrackColor = Slate800
                            ),
                            modifier = Modifier.testTag("switch_sound")
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Badges and PWA shortcuts
            CyberCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenVerification() },
                backgroundColor = Slate900,
                borderColor = CyberGold.copy(alpha = 0.4f)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = CyberGold,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Verification Badge System",
                            color = Slate100,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = if (currentUser.verified) "Active: ${currentUser.verifyPackage}" else "Redeem developer VIP2026 pass",
                            color = CyberGold,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            CyberCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenPwaInfo() },
                backgroundColor = Slate900,
                borderColor = Slate800
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = NeonEmerald,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "WebAPK & PWA Architecture",
                            color = Slate100,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Anonymous zero-tracker security model & offline caching",
                            color = Slate400,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            CyberButton(
                text = "LOGOUT OF THIS NODE",
                onClick = onLogout,
                icon = Icons.AutoMirrored.Filled.Logout,
                isDanger = true,
                modifier = Modifier.fillMaxWidth(),
                tag = "btn_logout"
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
