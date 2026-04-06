package com.example.smsledger.domain.usecase

import com.example.smsledger.domain.model.Transaction
import com.example.smsledger.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow

/**
 * 모든 거래 내역을 가져오는 유스케이스
 */
class GetTransactionsUseCase(private val repository: TransactionRepository) {
    /**
     * 유스케이스 실행
     * @return 거래 내역 리스트의 Flow
     */
    operator fun invoke(): Flow<List<Transaction>> = repository.getTransactions()
}

/**
 * 새로운 거래 내역을 추가하는 유스케이스
 */
class AddTransactionUseCase(private val repository: TransactionRepository) {
    /**
     * 유스케이스 실행
     * @param transaction 추가할 거래 정보
     */
    suspend operator fun invoke(transaction: Transaction) = repository.insertTransaction(transaction)
}

/**
 * 기존 거래 내역을 수정하는 유스케이스
 */
class UpdateTransactionUseCase(private val repository: TransactionRepository) {
    /**
     * 유스케이스 실행
     * @param transaction 수정할 거래 정보
     */
    suspend operator fun invoke(transaction: Transaction) = repository.updateTransaction(transaction)
}

/**
 * 거래 내역을 삭제하는 유스케이스
 */
class DeleteTransactionUseCase(private val repository: TransactionRepository) {
    /**
     * 유스케이스 실행
     * @param transaction 삭제할 거래 정보
     */
    suspend operator fun invoke(transaction: Transaction) = repository.deleteTransaction(transaction)
}
