package com.example.smsledger.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recurring_transactions")
data class RecurringTransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amount: Long,
    val storeName: String,
    val category: String,
    val type: String = "EXPENSE",
    val dayOfMonth: Int
)
