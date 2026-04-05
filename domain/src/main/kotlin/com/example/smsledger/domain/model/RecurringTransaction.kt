package com.example.smsledger.domain.model

data class RecurringTransaction(
    val id: Long = 0,
    val amount: Long,
    val storeName: String,
    val category: String = "기타",
    val type: TransactionType = TransactionType.EXPENSE,
    val dayOfMonth: Int // 1-31
)
