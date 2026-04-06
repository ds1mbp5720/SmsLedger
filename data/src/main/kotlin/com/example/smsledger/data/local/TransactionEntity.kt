package com.example.smsledger.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 거래 내역을 데이터베이스에 저장하기 위한 Room 엔티티 클래스
 * 
 * @property id 거래의 고유 식별자 (자동 생성)
 * @property amount 거래 금액
 * @property storeName 상점명 또는 거래처명
 * @property date 거래 발생 일시 (Unix Timestamp)
 * @property category 거래 카테고리
 * @property originalMessage 원본 SMS 메시지
 * @property type 거래 유형 (INCOME, EXPENSE)
 * @property recurringId 고정 거래 ID (고정 거래인 경우)
 */
@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amount: Long,
    val storeName: String,
    val date: Long,
    val category: String,
    val originalMessage: String,
    val type: String = "EXPENSE",
    val recurringId: Long? = null
)
