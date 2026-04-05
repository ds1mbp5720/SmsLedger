package com.example.smsledger.data.repository

import com.example.smsledger.data.local.RecurringTransactionDao
import com.example.smsledger.data.local.RecurringTransactionEntity
import com.example.smsledger.domain.model.RecurringTransaction
import com.example.smsledger.domain.model.TransactionType
import com.example.smsledger.domain.repository.RecurringTransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RecurringTransactionRepositoryImpl(
    private val dao: RecurringTransactionDao
) : RecurringTransactionRepository {

    override fun getRecurringTransactions(): Flow<List<RecurringTransaction>> {
        return dao.getAll().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun addRecurringTransaction(recurringTransaction: RecurringTransaction) {
        dao.insert(recurringTransaction.toEntity())
    }

    override suspend fun updateRecurringTransaction(recurringTransaction: RecurringTransaction) {
        dao.update(recurringTransaction.toEntity())
    }

    override suspend fun deleteRecurringTransaction(recurringTransaction: RecurringTransaction) {
        dao.delete(recurringTransaction.toEntity())
    }

    private fun RecurringTransactionEntity.toDomain() = RecurringTransaction(
        id = id,
        amount = amount,
        storeName = storeName,
        category = category,
        type = TransactionType.valueOf(type),
        dayOfMonth = dayOfMonth
    )

    private fun RecurringTransaction.toEntity() = RecurringTransactionEntity(
        id = id,
        amount = amount,
        storeName = storeName,
        category = category,
        type = type.name,
        dayOfMonth = dayOfMonth
    )
}
