package com.example.smsledger.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * 거래 내역 데이터베이스 접근 객체 (DAO)
 */
@Dao
interface TransactionDao {
    /**
     * 모든 거래 내역을 날짜 내림차순으로 조회
     * @return 거래 내역 엔티티 리스트의 Flow
     */
    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    /**
     * 새로운 거래 내역 삽입 (충돌 시 덮어쓰기)
     * @param transaction 삽입할 거래 내역 엔티티
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity)

    /**
     * 기존 거래 내역 수정
     * @param transaction 수정할 거래 내역 엔티티
     */
    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    /**
     * 거래 내역 삭제
     * @param transaction 삭제할 거래 내역 엔티티
     */
    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)
}
