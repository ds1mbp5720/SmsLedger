package com.example.smsledger.domain.repository

import com.example.smsledger.domain.model.RecurringTransaction
import kotlinx.coroutines.flow.Flow

interface RecurringTransactionRepository {
    fun getRecurringTransactions(): Flow<List<RecurringTransaction>>
    suspend fun addRecurringTransaction(recurringTransaction: RecurringTransaction)
    suspend fun updateRecurringTransaction(recurringTransaction: RecurringTransaction)
    suspend fun deleteRecurringTransaction(recurringTransaction: RecurringTransaction)
}
