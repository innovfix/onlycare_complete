package com.onlycare.app.domain.model

data class Message(
    val id: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val content: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)

data class ChatConversation(
    val userId: String = "",
    val userName: String = "",
    val userImage: String = "",
    val lastMessage: String = "",
    val lastMessageTime: Long = 0L,
    val unreadCount: Int = 0,
    val isOnline: Boolean = false
)



