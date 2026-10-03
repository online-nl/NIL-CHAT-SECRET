package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VpnKey
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.FirebaseManager
import com.example.data.SessionManager
import com.example.model.UserProfile
import com.example.sound.SoundEffects
import com.example.ui.components.CyberButton
import com.example.ui.components.CyberCard
import com.example.ui.components.CyberTextField
import com.example.ui.theme.CyberBlue
import com.example.ui.theme.EmeraldGlow
import com.example.ui.theme.IndigoAccent
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

@Composable
fun AuthScreen(
    firebaseManager: FirebaseManager,
    sessionManager: SessionManager,
    onAuthSuccess: (UserProfile) -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val coroutineScope = rememberCoroutineScope()

    var isLoginMode by remember { mutableStateOf(false) }

    // Registration inputs
    var regName by remember { mutableStateOf("") }
    var regPin by remember { mutableStateOf("") }
    var regPinConfirm by remember { mutableStateOf("") }

    // Login inputs
    var loginChatId by remember { mutableStateOf("") }
    var loginPin by remember { mutableStateOf("") }

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Registration success modal
    var newRegisteredUser by remember { mutableStateOf<UserProfile?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate950)
            .testTag("auth_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Brand Header
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(NeonEmerald.copy(alpha = 0.3f), Slate950)
                        )
                    )
                    .border(1.5.dp, NeonEmerald, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = "NIL DARK Shield",
                    tint = NeonEmerald,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "NIL DARK",
                color = Slate100,
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp
            )

            Text(
                text = "Ultra-Secure Anonymous Realtime Chat",
                color = NeonEmerald,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Mode Toggle Tab
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Slate900)
                    .border(1.dp, Slate800, RoundedCornerShape(14.dp))
                    .padding(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (!isLoginMode) NeonEmerald else Slate900)
                        .clickable {
                            isLoginMode = false
                            errorMessage = null
                        }
                        .padding(vertical = 10.dp)
                        .testTag("tab_register"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "NEW REGISTRATION",
                        color = if (!isLoginMode) Slate950 else Slate400,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isLoginMode) NeonEmerald else Slate900)
                        .clickable {
                            isLoginMode = true
                            errorMessage = null
                        }
                        .padding(vertical = 10.dp)
                        .testTag("tab_login"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "LOGIN VIA CHAT ID",
                        color = if (isLoginMode) Slate950 else Slate400,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Error Message Banner
            AnimatedVisibility(
                visible = errorMessage != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                if (errorMessage != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(com.example.ui.theme.CyberRed.copy(alpha = 0.15f))
                            .border(1.dp, com.example.ui.theme.CyberRed.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                            .padding(12.dp)
                            .testTag("auth_error_message")
                    ) {
                        Text(
                            text = errorMessage ?: "",
                            color = Slate100,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // Input Form Card
            CyberCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = Slate900,
                borderColor = Slate800
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    if (!isLoginMode) {
                        // Registration Form
                        CyberTextField(
                            value = regName,
                            onValueChange = { regName = it },
                            label = "DISPLAY NAME / CODENAME",
                            placeholder = "e.g. Agent Phantom, Neo",
                            leadingIcon = Icons.Default.Person,
                            tag = "input_reg_name"
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        CyberTextField(
                            value = regPin,
                            onValueChange = { if (it.length <= 4 && it.all { char -> char.isDigit() }) regPin = it },
                            label = "4-DIGIT SECURITY PIN",
                            placeholder = "••••",
                            leadingIcon = Icons.Default.Lock,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            singleLine = true,
                            isMonospace = true,
                            tag = "input_reg_pin"
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        CyberTextField(
                            value = regPinConfirm,
                            onValueChange = { if (it.length <= 4 && it.all { char -> char.isDigit() }) regPinConfirm = it },
                            label = "CONFIRM 4-DIGIT PIN",
                            placeholder = "••••",
                            leadingIcon = Icons.Default.VpnKey,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            singleLine = true,
                            isMonospace = true,
                            tag = "input_reg_pin_confirm"
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        CyberButton(
                            text = "GENERATE CHAT ID & REGISTER",
                            onClick = {
                                if (regName.trim().isEmpty()) {
                                    errorMessage = "Please enter a display name."
                                    return@CyberButton
                                }
                                if (regPin.length != 4) {
                                    errorMessage = "PIN must be exactly 4 digits."
                                    return@CyberButton
                                }
                                if (regPin != regPinConfirm) {
                                    errorMessage = "PIN confirmation does not match."
                                    return@CyberButton
                                }

                                isLoading = true
                                errorMessage = null
                                coroutineScope.launch {
                                    val result = firebaseManager.registerUser(regName.trim(), regPin.trim())
                                    isLoading = false
                                    result.onSuccess { profile ->
                                        sessionManager.currentUid = profile.uid
                                        sessionManager.saveCachedInfo(profile.name, profile.chatId)
                                        SoundEffects.playSentChime()
                                        newRegisteredUser = profile
                                    }.onFailure { err ->
                                        errorMessage = err.message ?: "Registration failed."
                                    }
                                }
                            },
                            isLoading = isLoading,
                            modifier = Modifier.fillMaxWidth(),
                            tag = "btn_submit_register"
                        )
                    } else {
                        // Login Form
                        CyberTextField(
                            value = loginChatId,
                            onValueChange = { loginChatId = it.uppercase() },
                            label = "8-CHARACTER CHAT ID",
                            placeholder = "e.g. X9K2M4P1",
                            leadingIcon = Icons.Default.Security,
                            isMonospace = true,
                            singleLine = true,
                            tag = "input_login_chat_id"
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        CyberTextField(
                            value = loginPin,
                            onValueChange = { if (it.length <= 4 && it.all { char -> char.isDigit() }) loginPin = it },
                            label = "4-DIGIT PIN",
                            placeholder = "••••",
                            leadingIcon = Icons.Default.Lock,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            singleLine = true,
                            isMonospace = true,
                            tag = "input_login_pin"
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        CyberButton(
                            text = "AUTHENTICATE NODE",
                            onClick = {
                                if (loginChatId.trim().length != 8) {
                                    errorMessage = "Chat ID must be exactly 8 characters."
                                    return@CyberButton
                                }
                                if (loginPin.length != 4) {
                                    errorMessage = "PIN must be exactly 4 digits."
                                    return@CyberButton
                                }

                                isLoading = true
                                errorMessage = null
                                coroutineScope.launch {
                                    val result = firebaseManager.loginUser(loginChatId.trim(), loginPin.trim())
                                    isLoading = false
                                    result.onSuccess { profile ->
                                        sessionManager.currentUid = profile.uid
                                        sessionManager.saveCachedInfo(profile.name, profile.chatId)
                                        SoundEffects.playActionTick()
                                        onAuthSuccess(profile)
                                    }.onFailure { err ->
                                        errorMessage = err.message ?: "Authentication failed."
                                    }
                                }
                            },
                            isLoading = isLoading,
                            modifier = Modifier.fillMaxWidth(),
                            tag = "btn_submit_login"
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Security Badges Footer (Mandatory per model specification)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Slate900.copy(alpha = 0.5f))
                    .border(1.dp, Slate850, RoundedCornerShape(12.dp))
                    .padding(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = NeonEmerald,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "100% Anonymous & Private",
                        color = Slate300(),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = NeonEmerald,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "No Email, Phone Number, or Google Sign-In",
                        color = Slate300(),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = NeonEmerald,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "SHA-256 Vaulted PIN Authentication",
                        color = Slate300(),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Registration Success Dialog showing permanent Chat ID
        if (newRegisteredUser != null) {
            val user = newRegisteredUser!!
            AlertDialog(
                onDismissRequest = { /* Must click Proceed */ },
                containerColor = Slate900,
                titleContentColor = Slate100,
                textContentColor = Slate400,
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = NeonEmerald,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Registration Successful!", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                },
                text = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Save your unique permanent Chat ID. Contacts use this 8-character ID to find and chat with you:",
                            color = Slate300(),
                            fontSize = 13.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Slate950)
                                .border(1.5.dp, NeonEmerald, RoundedCornerShape(10.dp))
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = "YOUR CHAT ID",
                                        color = Slate600,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = user.chatId,
                                        color = NeonEmerald,
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.Black,
                                        fontFamily = FontFamily.Monospace,
                                        letterSpacing = 2.sp
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        clipboardManager.setText(AnnotatedString(user.chatId))
                                        Toast.makeText(context, "Chat ID copied to clipboard!", Toast.LENGTH_SHORT).show()
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "Copy Chat ID",
                                        tint = NeonEmerald
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Keep your 4-digit PIN safe. It cannot be recovered if lost.",
                            color = CyberBlue,
                            fontSize = 11.sp
                        )
                    }
                },
                confirmButton = {
                    CyberButton(
                        text = "ENTER DARK GRID",
                        onClick = {
                            val u = newRegisteredUser!!
                            newRegisteredUser = null
                            onAuthSuccess(u)
                        },
                        tag = "btn_enter_dark_grid"
                    )
                }
            )
        }
    }
}

@Composable
private fun Slate300() = com.example.ui.theme.Slate300
