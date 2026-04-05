package com.example.smsledger.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [TransactionEntity::class, ParsingRuleEntity::class, CategoryEntity::class, RecurringTransactionEntity::class], version = 5)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun parsingRuleDao(): ParsingRuleDao
    abstract fun categoryDao(): CategoryDao
    abstract fun recurringTransactionDao(): RecurringTransactionDao
}
