package com.example.smsledger.domain.model

/**
 * 매달 반복되는 고정 거래 정보를 담는 데이터 클래스
 * 
 * @property id 고정 거래의 고유 식별자 (기본값: 0)
 * @property amount 거래 금액
 * @property storeName 상점명 또는 거래처명
 * @property category 거래 카테고리 (기본값: "기타")
 * @property type 거래 유형 (지출 또는 수입, 기본값: EXPENSE)
 * @property dayOfMonth 매달 거래가 발생하는 일자 (1-31)
 */
data class RecurringTransaction(
    val id: Long = 0,
    val amount: Long,
    val storeName: String,
    val category: String = "기타",
    val type: TransactionType = TransactionType.EXPENSE,
    val dayOfMonth: Int // 1-31
)
