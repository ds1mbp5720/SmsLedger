package com.example.smsledger.domain.repository

import com.example.smsledger.domain.model.Transaction
import kotlinx.coroutines.flow.Flow

/**
 * 거래 내역 데이터에 접근하기 위한 리포지토리 인터페이스
 */
interface TransactionRepository {
    /**
     * 모든 거래 내역을 Flow 형태로 조회
     * @return 거래 내역 리스트의 Flow
     */
    fun getTransactions(): Flow<List<Transaction>>

    /**
     * 새로운 거래 내역 추가
     * @param transaction 추가할 거래 정보
     */
    suspend fun insertTransaction(transaction: Transaction)

    /**
     * 기존 거래 내역 수정
     * @param transaction 수정할 거래 정보
     */
    suspend fun updateTransaction(transaction: Transaction)

    /**
     * 거래 내역 삭제
     * @param transaction 삭제할 거래 정보
     */
    suspend fun deleteTransaction(transaction: Transaction)
}
