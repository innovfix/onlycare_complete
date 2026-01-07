package com.onlycare.app.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Message DTO
 */
data class MessageDto(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("sender_id")
    val senderId: String,
    
    @SerializedName("receiver_id")
    val receiverId: String,
    
    @SerializedName("message")
    val message: String,
    
    @SerializedName("created_at")
    val createdAt: String,
    
    @SerializedName("is_read")
    val isRead: Boolean = false
)

/**
 * Conversation DTO
 */
data class ConversationDto(
    @SerializedName("user")
    val user: UserDto,
    
    @SerializedName("last_message")
    val lastMessage: String? = null,
    
    @SerializedName("last_message_time")
    val lastMessageTime: String? = null,
    
    @SerializedName("unread_count")
    val unreadCount: Int = 0
)

/**
 * Send Message Request
 */
data class SendMessageRequest(
    @SerializedName("message")
    val message: String
)

/**
 * Get Conversations Response
 */
data class GetConversationsResponse(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("conversations")
    val conversations: List<ConversationDto>
)

/**
 * Get Messages Response
 */
data class GetMessagesResponse(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("messages")
    val messages: List<MessageDto>
)

