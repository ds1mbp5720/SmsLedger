package com.example.smsledger.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 고정 거래 정보를 데이터베이스에 저장하기 위한 Room 엔티티 클래스
 * 
 * @property id 고정 거래의 고유 식별자 (자동 생성)
 * @property amount 거래 금액
 * @property storeName 상점명
 * @property category 카테고리
 * @property type 거래 유형 (INCOME, EXPENSE)
 * @property dayOfMonth 매달 거래 발생 일자
 */
@Entity(tableName = "recurring_transactions")
data class RecurringTransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amount: Long,
    val storeName: String,
    val category: String,
    val type: String = "EXPENSE",
    val dayOfMonth: Int
)
