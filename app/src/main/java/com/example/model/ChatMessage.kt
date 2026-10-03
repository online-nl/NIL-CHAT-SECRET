package com.example.model

data class ChatMessage(
    val id: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val text: String = "",
    val createdAt: Long = 0L,
    val deliveredAt: Long = 0L,
    val seenAt: Long = 0L,
    val deletedFor: Map<String, Boolean> = emptyMap()
) {
    fun isDeletedFor(uid: String): Boolean {
        return deletedFor[uid] == true
    }

    fun toMap(): Map<String, Any?> {
        return mapOf(
            "senderId" to senderId,
            "receiverId" to receiverId,
            "text" to text,
            "createdAt" to createdAt,
            "deliveredAt" to deliveredAt,
            "seenAt" to seenAt,
            "deletedFor" to deletedFor
        )
    }

    companion object {
        fun conversationId(uid1: String, uid2: String): String {
            val list = listOf(uid1, uid2).sorted()
            return "${list[0]}_${list[1]}"
        }

        @Suppress("UNCHECKED_CAST")
        fun fromMap(id: String, map: Map<String, Any?>?): ChatMessage {
            if (map == null) return ChatMessage(id = id)
            val deletedMap = (map["deletedFor"] as? Map<String, Boolean>) ?: emptyMap()
            return ChatMessage(
                id = id,
                senderId = (map["senderId"] as? String).orEmpty(),
                receiverId = (map["receiverId"] as? String).orEmpty(),
                text = (map["text"] as? String).orEmpty(),
                createdAt = (map["createdAt"] as? Number)?.toLong() ?: 0L,
                deliveredAt = (map["deliveredAt"] as? Number)?.toLong() ?: 0L,
                seenAt = (map["seenAt"] as? Number)?.toLong() ?: 0L,
                deletedFor = deletedMap
            )
        }
    }
}
