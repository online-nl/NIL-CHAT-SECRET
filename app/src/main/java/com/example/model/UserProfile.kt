package com.example.model

data class UserProfile(
    val uid: String = "",
    val chatId: String = "",
    val name: String = "",
    val pinHash: String = "",
    val verified: Boolean = false,
    val photo: String = "",
    val bio: String = "",
    val hideOnlineStatus: Boolean = false,
    val soundEnabled: Boolean = true,
    val verifyPackage: String = "",
    val verifyExpireTime: Long = 0L,
    val verifyActivatedAt: Long = 0L,
    val createdAt: Long = 0L
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "uid" to uid,
            "chatId" to chatId,
            "name" to name,
            "pinHash" to pinHash,
            "verified" to verified,
            "photo" to photo,
            "bio" to bio,
            "hideOnlineStatus" to hideOnlineStatus,
            "soundEnabled" to soundEnabled,
            "verifyPackage" to verifyPackage,
            "verifyExpireTime" to verifyExpireTime,
            "verifyActivatedAt" to verifyActivatedAt,
            "createdAt" to createdAt
        )
    }

    companion object {
        fun fromMap(uid: String, map: Map<String, Any?>?): UserProfile {
            if (map == null) return UserProfile(uid = uid)
            return UserProfile(
                uid = (map["uid"] as? String) ?: uid,
                chatId = (map["chatId"] as? String).orEmpty(),
                name = (map["name"] as? String).orEmpty(),
                pinHash = (map["pinHash"] as? String).orEmpty(),
                verified = (map["verified"] as? Boolean) ?: false,
                photo = (map["photo"] as? String).orEmpty(),
                bio = (map["bio"] as? String).orEmpty(),
                hideOnlineStatus = (map["hideOnlineStatus"] as? Boolean) ?: false,
                soundEnabled = (map["soundEnabled"] as? Boolean) ?: true,
                verifyPackage = (map["verifyPackage"] as? String).orEmpty(),
                verifyExpireTime = (map["verifyExpireTime"] as? Number)?.toLong() ?: 0L,
                verifyActivatedAt = (map["verifyActivatedAt"] as? Number)?.toLong() ?: 0L,
                createdAt = (map["createdAt"] as? Number)?.toLong() ?: 0L
            )
        }
    }
}
