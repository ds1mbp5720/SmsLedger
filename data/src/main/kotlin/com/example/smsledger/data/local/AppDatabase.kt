package com.example.smsledger.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * 앱의 메인 데이터베이스 클래스 (Room Database)
 * 거래 내역, 파싱 규칙, 카테고리, 고정 거래 정보를 관리합니다.
 */
@Database(entities = [TransactionEntity::class, ParsingRuleEntity::class, CategoryEntity::class, RecurringTransactionEntity::class], version = 5)
abstract class AppDatabase : RoomDatabase() {
    /** 거래 내역 DAO */
    abstract fun transactionDao(): TransactionDao
    /** 파싱 규칙 DAO */
    abstract fun parsingRuleDao(): ParsingRuleDao
    /** 카테고리 DAO */
    abstract fun categoryDao(): CategoryDao
    /** 고정 거래 DAO */
    abstract fun recurringTransactionDao(): RecurringTransactionDao
}
