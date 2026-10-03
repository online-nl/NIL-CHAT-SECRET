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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Stars
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
import com.example.sound.SoundEffects
import com.example.ui.components.CyberButton
import com.example.ui.components.CyberTextField
import com.example.ui.components.VerifiedBadge
import com.example.ui.theme.CyberGold
import com.example.ui.theme.CyberRed
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate300
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate850
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950
import kotlinx.coroutines.launch

@Composable
fun VerificationDialog(
    uid: String,
    isAlreadyVerified: Boolean,
    verifyPackage: String,
    firebaseManager: FirebaseManager,
    onDismiss: () -> Unit,
    onVerifiedSuccess: () -> Unit
) {
    var codeInput by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var successMsg by remember { mutableStateOf<String?>(null) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
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
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = CyberGold,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "VERIFICATION BADGE",
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
                if (isAlreadyVerified) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(CyberGold.copy(alpha = 0.12f))
                            .border(1.dp, CyberGold.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                            .padding(14.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                VerifiedBadge(isVipGold = true, showLabel = true)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Node Verified",
                                    color = Slate100,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Package: $verifyPackage. Your identity shield is permanent and visible to all contacts.",
                                color = Slate400,
                                fontSize = 12.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }

                Text(
                    text = "Redeem custom verification badge code or secret developer pass (e.g. VIP2026):",
                    color = Slate400,
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                CyberTextField(
                    value = codeInput,
                    onValueChange = {
                        codeInput = it.uppercase()
                        errorMsg = null
                    },
                    placeholder = "Enter VIP or Voucher Code (VIP2026)",
                    leadingIcon = Icons.Default.Stars,
                    isMonospace = true,
                    singleLine = true,
                    tag = "input_verify_code"
                )

                AnimatedVisibility(visible = errorMsg != null) {
                    if (errorMsg != null) {
                        Text(
                            text = errorMsg ?: "",
                            color = CyberRed,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }

                AnimatedVisibility(visible = successMsg != null) {
                    if (successMsg != null) {
                        Text(
                            text = successMsg ?: "",
                            color = NeonEmerald,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Slate950)
                        .padding(12.dp)
                ) {
                    Text(
                        text = "VIP Gold Perks:",
                        color = CyberGold,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "• Official Gold Shield badge across chats & headers", color = Slate400, fontSize = 11.sp)
                    Text(text = "• Zero log retention guarantee", color = Slate400, fontSize = 11.sp)
                    Text(text = "• Highest priority WebRTC signaling stream", color = Slate400, fontSize = 11.sp)
                }
            }
        },
        confirmButton = {
            CyberButton(
                text = "ACTIVATE BADGE",
                onClick = {
                    if (codeInput.trim().isEmpty()) {
                        errorMsg = "Please enter a code."
                        return@CyberButton
                    }
                    isLoading = true
                    errorMsg = null
                    successMsg = null
                    scope.launch {
                        val result = firebaseManager.redeemVerificationCode(uid, codeInput.trim())
                        isLoading = false
                        result.onSuccess { msg ->
                            successMsg = msg
                            SoundEffects.playSentChime()
                            onVerifiedSuccess()
                        }.onFailure { err ->
                            errorMsg = err.message ?: "Failed to redeem code."
                        }
                    }
                },
                isLoading = isLoading,
                modifier = Modifier.fillMaxWidth(),
                tag = "btn_activate_badge"
            )
        },
        dismissButton = {}
    )
}
