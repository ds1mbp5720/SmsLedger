package com.example.smsledger.domain.repository

import com.example.smsledger.domain.model.RecurringTransaction
import kotlinx.coroutines.flow.Flow

/**
 * 고정 거래 데이터에 접근하기 위한 리포지토리 인터페이스
 */
interface RecurringTransactionRepository {
    /**
     * 모든 고정 거래 목록을 Flow 형태로 조회
     * @return 고정 거래 리스트의 Flow
     */
    fun getRecurringTransactions(): Flow<List<RecurringTransaction>>

    /**
     * 새로운 고정 거래 추가
     * @param recurringTransaction 추가할 고정 거래 정보
     */
    suspend fun addRecurringTransaction(recurringTransaction: RecurringTransaction)

    /**
     * 기존 고정 거래 정보 수정
     * @param recurringTransaction 수정할 고정 거래 정보
     */
    suspend fun updateRecurringTransaction(recurringTransaction: RecurringTransaction)

    /**
     * 고정 거래 삭제
     * @param recurringTransaction 삭제할 고정 거래 정보
     */
    suspend fun deleteRecurringTransaction(recurringTransaction: RecurringTransaction)
}
