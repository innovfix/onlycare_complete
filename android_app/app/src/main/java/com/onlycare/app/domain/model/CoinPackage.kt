package com.onlycare.app.domain.model

data class CoinPackage(
    val id: String = "",
    val coins: Int = 0,
    val price: Double = 0.0,
    val originalPrice: Double = 0.0,
    val discount: Int = 0,
    val isPopular: Boolean = false,
    val isBestValue: Boolean = false
)

data class Transaction(
    val id: String = "",
    val type: TransactionType = TransactionType.PURCHASE,
    val amount: Double = 0.0,
    val coins: Int = 0,
    val isCredit: Boolean = true,
    val status: TransactionStatus = TransactionStatus.SUCCESS,
    val timestamp: Long = System.currentTimeMillis(),
    val paymentMethod: String = "",
    val title: String = "",
    val description: String = ""
)

enum class TransactionType {
    PURCHASE, CALL, GIFT, WITHDRAWAL, BONUS
}

enum class TransactionStatus {
    PENDING, SUCCESS, FAILED
}



