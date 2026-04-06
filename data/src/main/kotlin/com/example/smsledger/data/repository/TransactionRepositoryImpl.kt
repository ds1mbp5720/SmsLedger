package com.example.smsledger.data.repository

import com.example.smsledger.data.local.TransactionDao
import com.example.smsledger.data.local.TransactionEntity
import com.example.smsledger.domain.model.Transaction
import com.example.smsledger.domain.model.TransactionType
import com.example.smsledger.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * TransactionRepository의 구현체
 * Room 데이터베이스(TransactionDao)를 사용하여 거래 내역을 관리합니다.
 */
class TransactionRepositoryImpl(private val dao: TransactionDao) : TransactionRepository {
    
    /**
     * 데이터베이스에서 모든 거래 내역을 가져와 도메인 모델 리스트로 변환하여 반환
     */
    override fun getTransactions(): Flow<List<Transaction>> {
        return dao.getAllTransactions().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    /**
     * 도메인 모델을 엔티티로 변환하여 데이터베이스에 삽입
     */
    override suspend fun insertTransaction(transaction: Transaction) {
        dao.insertTransaction(transaction.toEntity())
    }

    /**
     * 도메인 모델을 엔티티로 변환하여 데이터베이스 업데이트
     */
    override suspend fun updateTransaction(transaction: Transaction) {
        dao.updateTransaction(transaction.toEntity())
    }

    /**
     * 도메인 모델을 엔티티로 변환하여 데이터베이스에서 삭제
     */
    override suspend fun deleteTransaction(transaction: Transaction) {
        dao.deleteTransaction(transaction.toEntity())
    }

    /**
     * TransactionEntity를 도메인 모델 Transaction으로 변환
     */
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

    /**
     * 도메인 모델 Transaction을 TransactionEntity로 변환
     */
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
