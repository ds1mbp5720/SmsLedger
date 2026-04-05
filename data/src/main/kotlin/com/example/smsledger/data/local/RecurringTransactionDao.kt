package com.example.smsledger.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface RecurringTransactionDao {
    @Query("SELECT * FROM recurring_transactions")
    fun getAll(): Flow<List<RecurringTransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(recurringTransaction: RecurringTransactionEntity)

    @Update
    suspend fun update(recurringTransaction: RecurringTransactionEntity)

    @Delete
    suspend fun delete(recurringTransaction: RecurringTransactionEntity)
}
