package com.example.smsledger.domain.usecase

import com.example.smsledger.domain.model.RecurringTransaction
import com.example.smsledger.domain.repository.RecurringTransactionRepository
import kotlinx.coroutines.flow.Flow

class GetRecurringTransactionsUseCase(private val repository: RecurringTransactionRepository) {
    operator fun invoke(): Flow<List<RecurringTransaction>> = repository.getRecurringTransactions()
}

class AddRecurringTransactionUseCase(private val repository: RecurringTransactionRepository) {
    suspend operator fun invoke(recurringTransaction: RecurringTransaction) = repository.addRecurringTransaction(recurringTransaction)
}

class UpdateRecurringTransactionUseCase(private val repository: RecurringTransactionRepository) {
    suspend operator fun invoke(recurringTransaction: RecurringTransaction) = repository.updateRecurringTransaction(recurringTransaction)
}

class DeleteRecurringTransactionUseCase(private val repository: RecurringTransactionRepository) {
    suspend operator fun invoke(recurringTransaction: RecurringTransaction) = repository.deleteRecurringTransaction(recurringTransaction)
}
