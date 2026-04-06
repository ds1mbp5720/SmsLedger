package com.example.smsledger.data.repository

import com.example.smsledger.data.local.RecurringTransactionDao
import com.example.smsledger.data.local.RecurringTransactionEntity
import com.example.smsledger.domain.model.RecurringTransaction
import com.example.smsledger.domain.model.TransactionType
import com.example.smsledger.domain.repository.RecurringTransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * RecurringTransactionRepository의 구현체
 * Room 데이터베이스(RecurringTransactionDao)를 사용하여 고정 거래를 관리합니다.
 */
class RecurringTransactionRepositoryImpl(
    private val dao: RecurringTransactionDao
) : RecurringTransactionRepository {

    /**
     * 데이터베이스에서 모든 고정 거래를 가져와 도메인 모델 리스트로 변환하여 반환
     */
    override fun getRecurringTransactions(): Flow<List<RecurringTransaction>> {
        return dao.getAll().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    /**
     * 고정 거래 추가
     */
    override suspend fun addRecurringTransaction(recurringTransaction: RecurringTransaction) {
        dao.insert(recurringTransaction.toEntity())
    }

    /**
     * 고정 거래 수정
     */
    override suspend fun updateRecurringTransaction(recurringTransaction: RecurringTransaction) {
        dao.update(recurringTransaction.toEntity())
    }

    /**
     * 고정 거래 삭제
     */
    override suspend fun deleteRecurringTransaction(recurringTransaction: RecurringTransaction) {
        dao.delete(recurringTransaction.toEntity())
    }

    /**
     * RecurringTransactionEntity를 도메인 모델 RecurringTransaction으로 변환
     */
    private fun RecurringTransactionEntity.toDomain() = RecurringTransaction(
        id = id,
        amount = amount,
        storeName = storeName,
        category = category,
        type = TransactionType.valueOf(type),
        dayOfMonth = dayOfMonth
    )

    /**
     * 도메인 모델 RecurringTransaction을 RecurringTransactionEntity로 변환
     */
    private fun RecurringTransaction.toEntity() = RecurringTransactionEntity(
        id = id,
        amount = amount,
        storeName = storeName,
        category = category,
        type = type.name,
        dayOfMonth = dayOfMonth
    )
}
