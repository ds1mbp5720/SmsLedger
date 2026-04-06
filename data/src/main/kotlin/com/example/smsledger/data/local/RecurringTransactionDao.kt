package com.example.smsledger.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * 고정 거래 데이터베이스 접근 객체 (DAO)
 */
@Dao
interface RecurringTransactionDao {
    /**
     * 모든 고정 거래 목록 조회
     * @return 고정 거래 엔티티 리스트의 Flow
     */
    @Query("SELECT * FROM recurring_transactions")
    fun getAll(): Flow<List<RecurringTransactionEntity>>

    /**
     * 새로운 고정 거래 삽입
     * @param recurringTransaction 삽입할 고정 거래 엔티티
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(recurringTransaction: RecurringTransactionEntity)

    /**
     * 고정 거래 정보 수정
     * @param recurringTransaction 수정할 고정 거래 엔티티
     */
    @Update
    suspend fun update(recurringTransaction: RecurringTransactionEntity)

    /**
     * 고정 거래 삭제
     * @param recurringTransaction 삭제할 고정 거래 엔티티
     */
    @Delete
    suspend fun delete(recurringTransaction: RecurringTransactionEntity)
}
