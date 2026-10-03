package com.example.data

import android.content.Context
import android.util.Log
import com.example.model.CallSession
import com.example.model.ChatMessage
import com.example.model.UserProfile
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import java.security.MessageDigest
import java.util.UUID
import kotlin.coroutines.resumeWithException

class FirebaseManager private constructor(context: Context) {

    private val database: FirebaseDatabase

    init {
        val app = if (FirebaseApp.getApps(context).isEmpty()) {
            val options = FirebaseOptions.Builder()
                .setApiKey("AIzaSyBiserV8guWwT2MQqzbrf9hD2qwYBKmYsY")
                .setApplicationId("1:991663674844:web:f3a76fe7d47bf405426b19")
                .setProjectId("chat-b8f7b")
                .setDatabaseUrl("https://chat-b8f7b-default-rtdb.firebaseio.com")
                .setStorageBucket("chat-b8f7b.firebasestorage.app")
                .setGcmSenderId("991663674844")
                .build()
            FirebaseApp.initializeApp(context, options)
        } else {
            FirebaseApp.getInstance()
        }

        database = FirebaseDatabase.getInstance(app, "https://chat-b8f7b-default-rtdb.firebaseio.com")
        try {
            database.setPersistenceEnabled(true)
        } catch (e: Exception) {
            Log.d("FirebaseManager", "Persistence already configured: ${e.message}")
        }
    }

    private val usersRef: DatabaseReference get() = database.getReference("users")
    private val chatIdsRef: DatabaseReference get() = database.getReference("chatIds")
    private val messagesRef: DatabaseReference get() = database.getReference("messages")
    private val statusRef: DatabaseReference get() = database.getReference("status")
    private val typingRef: DatabaseReference get() = database.getReference("typing")
    private val callsRef: DatabaseReference get() = database.getReference("calls")
    private val verifyCodesRef: DatabaseReference get() = database.getReference("verifyCodes")
    private val infoConnectedRef: DatabaseReference get() = database.getReference(".info/connected")

    companion object {
        @Volatile
        private var instance: FirebaseManager? = null

        fun getInstance(context: Context): FirebaseManager {
            return instance ?: synchronized(this) {
                instance ?: FirebaseManager(context.applicationContext).also { instance = it }
            }
        }

        fun hashPin(pin: String): String {
            val digest = MessageDigest.getInstance("SHA-256")
            val hash = digest.digest(pin.toByteArray(Charsets.UTF_8))
            return hash.joinToString("") { "%02x".format(it) }
        }

        fun generateChatId(): String {
            val chars = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ"
            return (1..8).map { chars.random() }.joinToString("")
        }
    }

    // ----------------------------------------------------
    // Authentication & Registration
    // ----------------------------------------------------

    suspend fun registerUser(name: String, pin: String): Result<UserProfile> {
        return try {
            val uid = UUID.randomUUID().toString()
            var chatId = generateChatId()

            // Verify Chat ID uniqueness
            var isUnique = false
            var attempts = 0
            while (!isUnique && attempts < 5) {
                val existing = chatIdsRef.child(chatId).get().awaitTask()
                if (!existing.exists()) {
                    isUnique = true
                } else {
                    chatId = generateChatId()
                    attempts++
                }
            }

            val pinHash = hashPin(pin)
            val now = System.currentTimeMillis()
            val user = UserProfile(
                uid = uid,
                chatId = chatId,
                name = name.trim(),
                pinHash = pinHash,
                verified = false,
                photo = "",
                bio = "Encrypted stealth node. Online via NIL DARK.",
                hideOnlineStatus = false,
                soundEnabled = true,
                verifyPackage = "",
                verifyExpireTime = 0L,
                verifyActivatedAt = 0L,
                createdAt = now
            )

            // Write users/{uid} and chatIds/{chatId}
            usersRef.child(uid).setValue(user.toMap()).awaitTask()
            chatIdsRef.child(chatId).setValue(mapOf("uid" to uid)).awaitTask()

            Result.success(user)
        } catch (e: Exception) {
            Log.e("FirebaseManager", "Registration error", e)
            Result.failure(e)
        }
    }

    suspend fun loginUser(chatIdInput: String, pinInput: String): Result<UserProfile> {
        return try {
            val formattedChatId = chatIdInput.trim().uppercase()
            val chatIdSnapshot = chatIdsRef.child(formattedChatId).get().awaitTask()
            if (!chatIdSnapshot.exists()) {
                return Result.failure(Exception("Chat ID not found. Verify your 8-character ID or register."))
            }

            val uid = chatIdSnapshot.child("uid").getValue(String::class.java)
                ?: return Result.failure(Exception("Invalid Chat ID record in system."))

            val userSnapshot = usersRef.child(uid).get().awaitTask()
            if (!userSnapshot.exists()) {
                return Result.failure(Exception("User profile data not found."))
            }

            @Suppress("UNCHECKED_CAST")
            val userMap = userSnapshot.value as? Map<String, Any?>
            val profile = UserProfile.fromMap(uid, userMap)

            val expectedHash = hashPin(pinInput.trim())
            if (profile.pinHash != expectedHash) {
                return Result.failure(Exception("Incorrect 4-digit PIN."))
            }

            Result.success(profile)
        } catch (e: Exception) {
            Log.e("FirebaseManager", "Login error", e)
            Result.failure(e)
        }
    }

    suspend fun getUserProfile(uid: String): UserProfile? {
        return try {
            val snapshot = usersRef.child(uid).get().awaitTask()
            if (!snapshot.exists()) return null
            @Suppress("UNCHECKED_CAST")
            val map = snapshot.value as? Map<String, Any?>
            UserProfile.fromMap(uid, map)
        } catch (e: Exception) {
            null
        }
    }

    fun observeUserProfile(uid: String): Flow<UserProfile?> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    @Suppress("UNCHECKED_CAST")
                    val map = snapshot.value as? Map<String, Any?>
                    trySend(UserProfile.fromMap(uid, map))
                } else {
                    trySend(null)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w("FirebaseManager", "observeUserProfile cancelled: ${error.message}")
            }
        }
        val ref = usersRef.child(uid)
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    suspend fun searchUserByChatId(searchChatId: String): UserProfile? {
        return try {
            val query = searchChatId.trim().uppercase()
            val snap = chatIdsRef.child(query).get().awaitTask()
            if (!snap.exists()) return null
            val uid = snap.child("uid").getValue(String::class.java) ?: return null
            getUserProfile(uid)
        } catch (e: Exception) {
            Log.e("FirebaseManager", "Search error", e)
            null
        }
    }

    suspend fun updateProfile(
        uid: String,
        name: String,
        bio: String,
        photo: String,
        hideOnlineStatus: Boolean,
        soundEnabled: Boolean
    ): Result<Unit> {
        return try {
            val updates = mapOf(
                "name" to name.trim(),
                "bio" to bio.trim(),
                "photo" to photo,
                "hideOnlineStatus" to hideOnlineStatus,
                "soundEnabled" to soundEnabled
            )
            usersRef.child(uid).updateChildren(updates).awaitTask()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ----------------------------------------------------
    // Verification System
    // ----------------------------------------------------

    suspend fun redeemVerificationCode(uid: String, code: String): Result<String> {
        return try {
            val cleanCode = code.trim().uppercase()
            if (cleanCode == "VIP2026") {
                val updates = mapOf(
                    "verified" to true,
                    "verifyPackage" to "VIP Gold Shield",
                    "verifyExpireTime" to 9999999999999L,
                    "verifyActivatedAt" to System.currentTimeMillis()
                )
                usersRef.child(uid).updateChildren(updates).awaitTask()
                return Result.success("VIP Gold Shield verified! Permanent Gold badge activated.")
            }

            // Check verifyCodes node
            val codeSnap = verifyCodesRef.child(cleanCode).get().awaitTask()
            if (codeSnap.exists()) {
                val used = codeSnap.child("used").getValue(Boolean::class.java) ?: false
                if (used) {
                    return Result.failure(Exception("This verification code has already been redeemed."))
                }
                val pkg = codeSnap.child("package").getValue(String::class.java) ?: "Cyber Verified"
                val expireTime = (codeSnap.child("expireTime").value as? Number)?.toLong() ?: 9999999999999L

                val updates = mapOf(
                    "verified" to true,
                    "verifyPackage" to pkg,
                    "verifyExpireTime" to expireTime,
                    "verifyActivatedAt" to System.currentTimeMillis()
                )
                usersRef.child(uid).updateChildren(updates).awaitTask()
                verifyCodesRef.child(cleanCode).child("used").setValue(true).awaitTask()
                Result.success("$pkg badge successfully activated!")
            } else {
                Result.failure(Exception("Invalid code. Use secret code 'VIP2026' or a valid badge code."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ----------------------------------------------------
    // Presence & Status
    // ----------------------------------------------------

    fun setupPresence(uid: String, hideOnlineStatus: Boolean) {
        val userStatusRef = statusRef.child(uid)
        infoConnectedRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val connected = snapshot.getValue(Boolean::class.java) ?: false
                if (connected) {
                    userStatusRef.onDisconnect().setValue(
                        mapOf(
                            "state" to "offline",
                            "lastChanged" to ServerValue.TIMESTAMP
                        )
                    )
                    val state = if (hideOnlineStatus) "offline" else "online"
                    userStatusRef.setValue(
                        mapOf(
                            "state" to state,
                            "lastChanged" to ServerValue.TIMESTAMP
                        )
                    )
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    fun observeUserStatus(uid: String): Flow<Pair<String, Long>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val state = snapshot.child("state").getValue(String::class.java) ?: "offline"
                val lastChanged = (snapshot.child("lastChanged").value as? Number)?.toLong() ?: 0L
                trySend(state to lastChanged)
            }

            override fun onCancelled(error: DatabaseError) {}
        }
        val ref = statusRef.child(uid)
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    // ----------------------------------------------------
    // Realtime Messaging
    // ----------------------------------------------------

    fun observeMessages(myUid: String, otherUid: String): Flow<List<ChatMessage>> = callbackFlow {
        val conversationId = ChatMessage.conversationId(myUid, otherUid)
        val convRef = messagesRef.child(conversationId)

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val messages = mutableListOf<ChatMessage>()
                for (child in snapshot.children) {
                    val key = child.key ?: continue
                    @Suppress("UNCHECKED_CAST")
                    val map = child.value as? Map<String, Any?>
                    val msg = ChatMessage.fromMap(key, map)
                    if (!msg.isDeletedFor(myUid)) {
                        messages.add(msg)
                    }
                }
                messages.sortBy { it.createdAt }
                trySend(messages)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w("FirebaseManager", "observeMessages cancelled: ${error.message}")
            }
        }

        convRef.addValueEventListener(listener)
        awaitClose { convRef.removeEventListener(listener) }
    }

    suspend fun sendMessage(senderId: String, receiverId: String, text: String): Result<String> {
        return try {
            val conversationId = ChatMessage.conversationId(senderId, receiverId)
            val newMsgRef = messagesRef.child(conversationId).push()
            val msgId = newMsgRef.key ?: UUID.randomUUID().toString()
            val now = System.currentTimeMillis()

            val msg = ChatMessage(
                id = msgId,
                senderId = senderId,
                receiverId = receiverId,
                text = text.trim(),
                createdAt = now,
                deliveredAt = now, // Marked delivered on RTDB push
                seenAt = 0L,
                deletedFor = emptyMap()
            )

            newMsgRef.setValue(msg.toMap()).awaitTask()
            Result.success(msgId)
        } catch (e: Exception) {
            Log.e("FirebaseManager", "Failed to send message", e)
            Result.failure(e)
        }
    }

    fun markMessagesSeen(myUid: String, otherUid: String, unreadMessageIds: List<String>) {
        if (unreadMessageIds.isEmpty()) return
        val conversationId = ChatMessage.conversationId(myUid, otherUid)
        val convRef = messagesRef.child(conversationId)
        val now = System.currentTimeMillis()
        for (id in unreadMessageIds) {
            convRef.child(id).child("seenAt").setValue(now)
        }
    }

    fun deleteMessageForMe(conversationId: String, messageId: String, myUid: String) {
        messagesRef.child(conversationId).child(messageId).child("deletedFor").child(myUid).setValue(true)
    }

    fun deleteMessageForEveryone(conversationId: String, messageId: String) {
        messagesRef.child(conversationId).child(messageId).removeValue()
    }

    // ----------------------------------------------------
    // Typing Indicators
    // ----------------------------------------------------

    fun setTyping(conversationId: String, uid: String, isTyping: Boolean) {
        typingRef.child(conversationId).child(uid).setValue(isTyping)
    }

    fun observeTyping(conversationId: String, otherUid: String): Flow<Boolean> = callbackFlow {
        val ref = typingRef.child(conversationId).child(otherUid)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val isTyping = snapshot.getValue(Boolean::class.java) ?: false
                trySend(isTyping)
            }

            override fun onCancelled(error: DatabaseError) {}
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    // ----------------------------------------------------
    // Calling & WebRTC Signaling
    // ----------------------------------------------------

    suspend fun initiateCall(
        callerUid: String,
        calleeUid: String,
        callerName: String,
        video: Boolean
    ): Result<CallSession> {
        return try {
            val callSession = CallSession(
                id = calleeUid,
                caller = callerUid,
                callee = calleeUid,
                callerName = callerName,
                video = video,
                status = "calling",
                timestamp = System.currentTimeMillis()
            )
            callsRef.child(calleeUid).setValue(callSession.toMap()).awaitTask()
            Result.success(callSession)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun observeCallSession(uid: String): Flow<CallSession?> = callbackFlow {
        val ref = callsRef.child(uid)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    @Suppress("UNCHECKED_CAST")
                    val map = snapshot.value as? Map<String, Any?>
                    trySend(CallSession.fromMap(uid, map))
                } else {
                    trySend(null)
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    fun updateCallStatus(calleeUid: String, status: String) {
        callsRef.child(calleeUid).child("status").setValue(status)
    }

    fun endCall(calleeUid: String) {
        callsRef.child(calleeUid).removeValue()
    }
}

private suspend fun <T> Task<T>.awaitTask(): T =
    suspendCancellableCoroutine { cont ->
        addOnSuccessListener { result ->
            if (cont.isActive) cont.resume(result, null)
        }
        addOnFailureListener { exception ->
            if (cont.isActive) cont.resumeWithException(exception)
        }
        addOnCanceledListener {
            if (cont.isActive) cont.cancel()
        }
    }
