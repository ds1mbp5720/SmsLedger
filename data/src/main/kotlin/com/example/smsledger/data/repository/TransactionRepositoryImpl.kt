package com.example.smsledger.data.repository

import com.example.smsledger.data.local.TransactionDao
import com.example.smsledger.data.local.TransactionEntity
import com.example.smsledger.domain.model.Transaction
import com.example.smsledger.domain.model.TransactionType
import com.example.smsledger.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TransactionRepositoryImpl(private val dao: TransactionDao) : TransactionRepository {
    
    override fun getTransactions(): Flow<List<Transaction>> {
        return dao.getAllTransactions().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun insertTransaction(transaction: Transaction) {
        dao.insertTransaction(transaction.toEntity())
    }

    override suspend fun updateTransaction(transaction: Transaction) {
        dao.updateTransaction(transaction.toEntity())
    }

    override suspend fun deleteTransaction(transaction: Transaction) {
        dao.deleteTransaction(transaction.toEntity())
    }

    private fun TransactionEntity.toDomain() = Transaction(
        id = id,
        amount = amount,
        storeName = storeName,
        date = date,
        category = category,
        originalMessage = originalMessage,
        type = TransactionType.valueOf(type),
        recurringId = recurringId
    )

    private fun Transaction.toEntity() = TransactionEntity(
        id = id,
        amount = amount,
        storeName = storeName,
        date = date,
        category = category,
        originalMessage = originalMessage,
        type = type.name,
        recurringId = recurringId
    )
}
