package com.example.smsledger.domain.model

/**
 * 개별 거래 내역 정보를 담는 데이터 클래스
 * 
 * @property id 거래의 고유 식별자 (기본값: 0)
 * @property amount 거래 금액
 * @property storeName 상점명 또는 거래처명
 * @property date 거래 발생 일시 (Unix Timestamp, 기본값: 현재 시간)
 * @property category 거래 카테고리 (기본값: "기타")
 * @property originalMessage 거래의 근거가 된 원본 SMS 메시지
 * @property type 거래 유형 (지출 또는 수입, 기본값: EXPENSE)
 * @property recurringId 고정 거래에서 생성된 경우 해당 고정 거래의 ID
 */
data class Transaction(
    val id: Long = 0,
    val amount: Long,
    val storeName: String,
    val date: Long = System.currentTimeMillis(),
    val category: String = "기타",
    val originalMessage: String = "",
    val type: TransactionType = TransactionType.EXPENSE,
    val recurringId: Long? = null
)

/**
 * 거래의 유형을 정의하는 열거형 클래스
 */
enum class TransactionType {
    /** 수입 */
    INCOME, 
    /** 지출 */
    EXPENSE
}
