package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.FirebaseManager
import com.example.model.UserProfile
import com.example.ui.components.AvatarView
import com.example.ui.components.CyberButton
import com.example.ui.components.CyberTextField
import com.example.ui.components.VerifiedBadge
import com.example.ui.theme.CyberRed
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate850
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950
import kotlinx.coroutines.launch

@Composable
fun AddContactDialog(
    myUid: String,
    firebaseManager: FirebaseManager,
    onDismiss: () -> Unit,
    onStartChat: (UserProfile) -> Unit
) {
    var searchId by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }
    var foundUser by remember { mutableStateOf<UserProfile?>(null) }
    var searchError by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Slate900,
        titleContentColor = Slate100,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "DISCOVER CONTACT",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate100,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Slate400)
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Enter an exact 8-character Chat ID to establish a secure peer-to-peer dark grid connection:",
                    color = Slate400,
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                CyberTextField(
                    value = searchId,
                    onValueChange = {
                        searchId = it.uppercase()
                        foundUser = null
                        searchError = null
                    },
                    placeholder = "e.g. X9K2M4P1",
                    leadingIcon = Icons.Default.Search,
                    isMonospace = true,
                    singleLine = true,
                    tag = "input_search_chat_id"
                )

                Spacer(modifier = Modifier.height(12.dp))

                CyberButton(
                    text = "SEARCH CHAT ID",
                    onClick = {
                        if (searchId.trim().length != 8) {
                            searchError = "Chat ID must be exactly 8 alphanumeric characters."
                            return@CyberButton
                        }
                        isSearching = true
                        searchError = null
                        foundUser = null
                        scope.launch {
                            val user = firebaseManager.searchUserByChatId(searchId.trim())
                            isSearching = false
                            if (user == null) {
                                searchError = "No user found with Chat ID: $searchId"
                            } else if (user.uid == myUid) {
                                searchError = "This is your own Chat ID."
                            } else {
                                foundUser = user
                            }
                        }
                    },
                    isLoading = isSearching,
                    isSecondary = true,
                    modifier = Modifier.fillMaxWidth(),
                    tag = "btn_search_chat_id"
                )

                AnimatedVisibility(visible = searchError != null) {
                    if (searchError != null) {
                        Text(
                            text = searchError ?: "",
                            color = CyberRed,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }

                // Found User Card
                AnimatedVisibility(visible = foundUser != null) {
                    if (foundUser != null) {
                        val user = foundUser!!
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Slate950)
                                .border(1.dp, NeonEmerald.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    AvatarView(name = user.name, photoUrl = user.photo, size = 44.dp)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = user.name,
                                                color = Slate100,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp
                                            )
                                            if (user.verified) {
                                                Spacer(modifier = Modifier.width(6.dp))
                                                VerifiedBadge(isVipGold = user.verifyPackage.contains("VIP"))
                                            }
                                        }
                                        Text(
                                            text = "ID: ${user.chatId}",
                                            color = NeonEmerald,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }

                                if (user.bio.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = user.bio,
                                        color = Slate400,
                                        fontSize = 12.sp,
                                        maxLines = 2
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (foundUser != null) {
                CyberButton(
                    text = "START CHAT",
                    onClick = {
                        val user = foundUser!!
                        onDismiss()
                        onStartChat(user)
                    },
                    tag = "btn_start_found_chat"
                )
            }
        },
        dismissButton = {}
    )
}
