package com.onlycare.app.domain.model

data class Call(
    val id: String = "",
    val callerId: String = "",
    val callerName: String = "",
    val callerImage: String = "",
    val receiverId: String = "",
    val receiverName: String = "",
    val receiverImage: String = "",
    val otherUserId: String = "",
    val otherUserName: String = "",
    val otherUserImage: String = "",
    val callType: CallType = CallType.AUDIO,
    val status: CallStatus = CallStatus.PENDING,
    val duration: Int = 0, // in seconds
    val coinsSpent: Int = 0,
    val coinsEarned: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val rating: Float = 0f
)

enum class CallType {
    AUDIO, VIDEO
}

enum class CallStatus {
    PENDING,
    CONNECTING,
    ONGOING,
    ENDED,
    MISSED,
    REJECTED,
    CANCELLED
}



