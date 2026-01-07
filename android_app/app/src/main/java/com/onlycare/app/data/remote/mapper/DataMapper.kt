package com.onlycare.app.data.remote.mapper

import com.onlycare.app.data.remote.dto.*
import com.onlycare.app.domain.model.*

/**
 * Extension functions to map DTOs to domain models
 */

// User Mapping
fun UserDto.toDomainModel(): User {
    val resolvedAudioEnabled = resolveBoolean(this.audioCallEnabled, false)
    val resolvedVideoEnabled = resolveBoolean(this.videoCallEnabled, false)
    val resolvedIsOnline = resolveBoolean(this.isOnline, false)
    val resolvedIsVerified = resolveBoolean(this.isVerified, false)

    return User(
        id = this.id,
        name = this.name,
        username = this.username ?: "",
        age = this.age ?: 0,
        gender = if (this.gender == "FEMALE") Gender.FEMALE else Gender.MALE,
        phone = this.phone ?: "",
        profileImage = this.profileImage ?: "",
        bio = this.bio ?: "",
        language = parseLanguage(this.language),
        interests = this.interests ?: emptyList(),
        isOnline = resolvedIsOnline,
        lastSeen = this.lastSeen ?: 0L,
        rating = this.rating,
        totalRatings = this.totalRatings,
        coinBalance = this.coinBalance ?: 0,
        totalEarnings = this.totalEarnings?.toInt() ?: 0,
        audioCallEnabled = resolvedAudioEnabled,
        videoCallEnabled = resolvedVideoEnabled,
        isVerified = resolvedIsVerified
    )
}

/**
 * Helper to resolve a boolean value from various possible API types (Boolean, Int, String)
 */
private fun resolveBoolean(value: Any?, defaultValue: Boolean): Boolean {
    return when (value) {
        null -> defaultValue
        is Boolean -> value
        is Number -> value.toInt() == 1
        is String -> {
            val v = value.lowercase().trim()
            v == "1" || v == "true" || v == "yes" || v == "on" || v == "enabled"
        }
        else -> defaultValue
    }
}

// Call Mapping
fun CallDto.toDomainModel(): Call {
    return Call(
        id = this.id ?: "",  // Handle nullable id
        callerId = this.callerId ?: "",  // Handle nullable callerId
        callerName = this.callerName ?: "",
        callerImage = this.callerImage ?: "",
        receiverId = this.receiverId ?: "",  // Handle nullable receiverId
        receiverName = this.receiverName ?: "",
        receiverImage = this.receiverImage ?: "",
        otherUserId = this.otherUserId ?: "",
        otherUserName = this.otherUserName ?: "",
        otherUserImage = this.otherUserImage ?: "",
        callType = if (this.callType == "VIDEO") CallType.VIDEO else CallType.AUDIO,
        status = parseCallStatus(this.status),  // status can be null, handled in parseCallStatus
        duration = this.duration,
        coinsSpent = this.coinsSpent,
        coinsEarned = this.coinsEarned,
        timestamp = this.timestamp ?: System.currentTimeMillis(), // Use API timestamp or fallback
        rating = this.rating ?: 0f
    )
}

// Coin Package Mapping
fun CoinPackageDto.toDomainModel(): CoinPackage {
    return CoinPackage(
        id = this.id,
        coins = this.coins,
        price = this.price,
        originalPrice = this.originalPrice,
        discount = this.discount,
        isPopular = this.isPopular,
        isBestValue = this.isBestValue
    )
}

// Transaction Mapping
fun TransactionDto.toDomainModel(): com.onlycare.app.domain.model.Transaction {
    return com.onlycare.app.domain.model.Transaction(
        id = this.id,
        type = parseTransactionType(this.type),
        amount = this.amount,
        coins = this.coins,
        isCredit = this.isCredit,
        status = parseTransactionStatus(this.status),
        timestamp = parseIso8601Timestamp(this.createdAt),
        paymentMethod = this.paymentMethod ?: "",
        title = this.title ?: parseTransactionType(this.type).name.lowercase().replaceFirstChar { it.uppercase() },
        description = this.description ?: ""
    )
}

// Helper to parse ISO8601 timestamp string to milliseconds
private fun parseIso8601Timestamp(iso8601String: String): Long {
    return try {
        // ISO8601 format: "2024-11-17T13:49:00.000000Z" or "2024-11-17T13:49:00+00:00"
        val dateTimeString = iso8601String.replace("Z", "+00:00")
        val formatter = java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME
        val instant = java.time.OffsetDateTime.parse(dateTimeString, formatter).toInstant()
        instant.toEpochMilli()
    } catch (e: Exception) {
        // Fallback to current time if parsing fails
        System.currentTimeMillis()
    }
}

// Message Mapping
fun MessageDto.toDomainModel(): Message {
    return Message(
        id = this.id,
        senderId = this.senderId,
        receiverId = this.receiverId,
        content = this.message,
        timestamp = System.currentTimeMillis(), // Or parse from createdAt
        isRead = this.isRead
    )
}

// Chat Conversation Mapping
fun ConversationDto.toDomainModel(): ChatConversation {
    return ChatConversation(
        userId = this.user.id,
        userName = this.user.name,
        userImage = this.user.profileImage ?: "",
        lastMessage = this.lastMessage ?: "",
        lastMessageTime = System.currentTimeMillis(), // Or parse from lastMessageTime
        unreadCount = this.unreadCount,
        isOnline = resolveBoolean(this.user.isOnline, false)
    )
}

// Helper functions to parse enums
private fun parseLanguage(language: String?): Language {
    return when (language?.uppercase()) {
        "HINDI" -> Language.HINDI
        "TAMIL" -> Language.TAMIL
        "TELUGU" -> Language.TELUGU
        "KANNADA" -> Language.KANNADA
        "MALAYALAM" -> Language.MALAYALAM
        "BENGALI" -> Language.BENGALI
        "MARATHI" -> Language.MARATHI
        else -> Language.ENGLISH
    }
}

private fun parseCallStatus(status: String?): CallStatus {
    return when (status?.uppercase()) {
        "PENDING" -> CallStatus.PENDING
        "CONNECTING" -> CallStatus.CONNECTING
        "ONGOING" -> CallStatus.ONGOING
        "ENDED" -> CallStatus.ENDED
        "MISSED" -> CallStatus.MISSED
        "REJECTED" -> CallStatus.REJECTED
        "CANCELLED" -> CallStatus.CANCELLED
        else -> CallStatus.PENDING
    }
}

private fun parseTransactionType(type: String): TransactionType {
    return when (type.uppercase()) {
        "PURCHASE" -> TransactionType.PURCHASE
        "CALL", "CALL_SPENT" -> TransactionType.CALL
        "GIFT" -> TransactionType.GIFT
        "WITHDRAWAL" -> TransactionType.WITHDRAWAL
        "BONUS" -> TransactionType.BONUS
        else -> TransactionType.PURCHASE
    }
}

private fun parseTransactionStatus(status: String): TransactionStatus {
    return when (status.uppercase()) {
        "PENDING" -> TransactionStatus.PENDING
        "SUCCESS" -> TransactionStatus.SUCCESS
        "FAILED" -> TransactionStatus.FAILED
        else -> TransactionStatus.PENDING
    }
}

