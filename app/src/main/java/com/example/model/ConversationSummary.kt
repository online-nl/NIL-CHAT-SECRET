package com.example.model

data class ConversationSummary(
    val conversationId: String,
    val otherUser: UserProfile,
    val lastMessage: ChatMessage? = null,
    val unreadCount: Int = 0,
    val updatedAt: Long = 0L,
    val isTyping: Boolean = false,
    val isOnline: Boolean = false
)
