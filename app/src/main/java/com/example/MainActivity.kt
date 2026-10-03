package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.data.FirebaseManager
import com.example.data.SessionManager
import com.example.model.CallSession
import com.example.model.UserProfile
import com.example.sound.SoundEffects
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.CallingScreen
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.IncomingCallModal
import com.example.ui.screens.PwaInfoModal
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.VerificationDialog
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.Slate950
import kotlinx.coroutines.launch

enum class AppDestination {
    AUTH,
    HOME,
    CHAT,
    SETTINGS,
    CALLING
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val firebaseManager = FirebaseManager.getInstance(applicationContext)
        val sessionManager = SessionManager(applicationContext)

        setContent {
            MyApplicationTheme {
                NilDarkApp(
                    firebaseManager = firebaseManager,
                    sessionManager = sessionManager
                )
            }
        }
    }
}

@Composable
fun NilDarkApp(
    firebaseManager: FirebaseManager,
    sessionManager: SessionManager
) {
    val scope = rememberCoroutineScope()

    var destination by remember { mutableStateOf(AppDestination.AUTH) }
    var currentUser by remember { mutableStateOf<UserProfile?>(null) }
    var activeChatUser by remember { mutableStateOf<UserProfile?>(null) }
    var activeCallSession by remember { mutableStateOf<CallSession?>(null) }
    var isCallVideo by remember { mutableStateOf(false) }

    var isSessionRestoring by remember { mutableStateOf(true) }
    var showVerificationDialog by remember { mutableStateOf(false) }
    var showPwaInfoModal by remember { mutableStateOf(false) }

    // Check stored session
    LaunchedEffect(Unit) {
        val storedUid = sessionManager.currentUid
        if (!storedUid.isNullOrEmpty()) {
            val user = firebaseManager.getUserProfile(storedUid)
            if (user != null) {
                currentUser = user
                destination = AppDestination.HOME
            } else {
                sessionManager.clearSession()
                destination = AppDestination.AUTH
            }
        } else {
            destination = AppDestination.AUTH
        }
        isSessionRestoring = false
    }

    // Global incoming call observer
    val currentUid = currentUser?.uid
    val incomingCall by remember(currentUid) {
        if (!currentUid.isNullOrEmpty()) {
            firebaseManager.observeCallSession(currentUid)
        } else {
            kotlinx.coroutines.flow.flowOf(null)
        }
    }.collectAsState(initial = null)

    // Handle incoming call dialog
    val isIncomingActive = incomingCall != null &&
            incomingCall?.status == "calling" &&
            incomingCall?.caller != currentUid

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate950)
    ) {
        if (isSessionRestoring) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = NeonEmerald, strokeWidth = 2.dp)
            }
        } else {
            AnimatedContent(
                targetState = destination,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "nav_transition"
            ) { target ->
                when (target) {
                    AppDestination.AUTH -> {
                        AuthScreen(
                            firebaseManager = firebaseManager,
                            sessionManager = sessionManager,
                            onAuthSuccess = { user ->
                                currentUser = user
                                destination = AppDestination.HOME
                            }
                        )
                    }

                    AppDestination.HOME -> {
                        if (currentUser != null) {
                            HomeScreen(
                                currentUser = currentUser!!,
                                firebaseManager = firebaseManager,
                                onOpenSettings = { destination = AppDestination.SETTINGS },
                                onOpenVerification = { showVerificationDialog = true },
                                onOpenPwaInfo = { showPwaInfoModal = true },
                                onSelectChat = { selectedContact ->
                                    activeChatUser = selectedContact
                                    destination = AppDestination.CHAT
                                },
                                activeChatUser = activeChatUser,
                                onCloseActiveChat = { activeChatUser = null }
                            )
                        }
                    }

                    AppDestination.CHAT -> {
                        BackHandler {
                            activeChatUser = null
                            destination = AppDestination.HOME
                        }
                        if (currentUser != null && activeChatUser != null) {
                            ChatScreen(
                                currentUser = currentUser!!,
                                otherUser = activeChatUser!!,
                                firebaseManager = firebaseManager,
                                onBack = {
                                    activeChatUser = null
                                    destination = AppDestination.HOME
                                },
                                onStartCall = { video ->
                                    isCallVideo = video
                                    scope.launch {
                                        firebaseManager.initiateCall(
                                            currentUser!!.uid,
                                            activeChatUser!!.uid,
                                            currentUser!!.name,
                                            video
                                        )
                                    }
                                    destination = AppDestination.CALLING
                                }
                            )
                        }
                    }

                    AppDestination.SETTINGS -> {
                        BackHandler {
                            destination = AppDestination.HOME
                        }
                        if (currentUser != null) {
                            SettingsScreen(
                                currentUser = currentUser!!,
                                firebaseManager = firebaseManager,
                                onBack = { destination = AppDestination.HOME },
                                onLogout = {
                                    sessionManager.clearSession()
                                    currentUser = null
                                    destination = AppDestination.AUTH
                                },
                                onOpenVerification = { showVerificationDialog = true },
                                onOpenPwaInfo = { showPwaInfoModal = true }
                            )
                        }
                    }

                    AppDestination.CALLING -> {
                        BackHandler {
                            if (activeChatUser != null) {
                                firebaseManager.endCall(activeChatUser!!.uid)
                            }
                            destination = AppDestination.CHAT
                        }
                        if (activeChatUser != null) {
                            CallingScreen(
                                contact = activeChatUser!!,
                                isVideo = isCallVideo,
                                onEndCall = {
                                    scope.launch {
                                        firebaseManager.endCall(activeChatUser!!.uid)
                                    }
                                    destination = AppDestination.CHAT
                                }
                            )
                        }
                    }
                }
            }
        }

        // Incoming Call Ringtone Modal
        if (isIncomingActive && incomingCall != null) {
            val call = incomingCall!!
            IncomingCallModal(
                callSession = call,
                soundEnabled = currentUser?.soundEnabled ?: true,
                onAccept = {
                    scope.launch {
                        firebaseManager.updateCallStatus(call.callee, "connected")
                        // Lookup caller profile to show in CallingScreen
                        val callerProfile = firebaseManager.getUserProfile(call.caller)
                        if (callerProfile != null) {
                            activeChatUser = callerProfile
                            isCallVideo = call.video
                            destination = AppDestination.CALLING
                        }
                    }
                },
                onDecline = {
                    scope.launch {
                        firebaseManager.updateCallStatus(call.callee, "rejected")
                        firebaseManager.endCall(call.callee)
                    }
                }
            )
        }

        // Verification Dialog
        if (showVerificationDialog && currentUser != null) {
            VerificationDialog(
                uid = currentUser!!.uid,
                isAlreadyVerified = currentUser!!.verified,
                verifyPackage = currentUser!!.verifyPackage,
                firebaseManager = firebaseManager,
                onDismiss = { showVerificationDialog = false },
                onVerifiedSuccess = {
                    scope.launch {
                        val refreshed = firebaseManager.getUserProfile(currentUser!!.uid)
                        if (refreshed != null) {
                            currentUser = refreshed
                        }
                    }
                }
            )
        }

        // PWA Info Modal
        if (showPwaInfoModal) {
            PwaInfoModal(
                onDismiss = { showPwaInfoModal = false }
            )
        }
    }
}
