package com.example.smsledger.domain.usecase

import com.example.smsledger.domain.model.RecurringTransaction
import com.example.smsledger.domain.repository.RecurringTransactionRepository
import kotlinx.coroutines.flow.Flow

/**
 * 모든 고정 거래 목록을 가져오는 유스케이스
 */
class GetRecurringTransactionsUseCase(private val repository: RecurringTransactionRepository) {
    /**
     * 유스케이스 실행
     * @return 고정 거래 리스트의 Flow
     */
    operator fun invoke(): Flow<List<RecurringTransaction>> = repository.getRecurringTransactions()
}

/**
 * 새로운 고정 거래를 추가하는 유스케이스
 */
class AddRecurringTransactionUseCase(private val repository: RecurringTransactionRepository) {
    /**
     * 유스케이스 실행
     * @param recurringTransaction 추가할 고정 거래 정보
     */
    suspend operator fun invoke(recurringTransaction: RecurringTransaction) = repository.addRecurringTransaction(recurringTransaction)
}

/**
 * 기존 고정 거래 정보를 수정하는 유스케이스
 */
class UpdateRecurringTransactionUseCase(private val repository: RecurringTransactionRepository) {
    /**
     * 유스케이스 실행
     * @param recurringTransaction 수정할 고정 거래 정보
     */
    suspend operator fun invoke(recurringTransaction: RecurringTransaction) = repository.updateRecurringTransaction(recurringTransaction)
}

/**
 * 고정 거래를 삭제하는 유스케이스
 */
class DeleteRecurringTransactionUseCase(private val repository: RecurringTransactionRepository) {
    /**
     * 유스케이스 실행
     * @param recurringTransaction 삭제할 고정 거래 정보
     */
    suspend operator fun invoke(recurringTransaction: RecurringTransaction) = repository.deleteRecurringTransaction(recurringTransaction)
}
