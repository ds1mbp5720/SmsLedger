package com.example.smsledger.domain.model

/**
 * SMS 메시지에서 데이터를 추출하기 위한 파싱 규칙 정보를 담는 데이터 클래스
 * 
 * @property id 규칙의 고유 식별자 (기본값: 0)
 * @property name 규칙의 이름 (예: 신한카드, 국민은행 등)
 * @property senderNumber SMS 발신 번호 (선택 사항)
 * @property amountPattern 금액을 추출하기 위한 정규표현식 패턴
 * @property storePattern 상점명을 추출하기 위한 정규표현식 패턴
 * @property isActive 규칙의 활성화 여부 (기본값: true)
 * @property type 거래 유형 (지출 또는 수입, 기본값: EXPENSE)
 */
data class ParsingRule(
    val id: Long = 0,
    val name: String,
    val senderNumber: String? = null,
    val amountPattern: String,
    val storePattern: String,
    val isActive: Boolean = true,
    val type: TransactionType = TransactionType.EXPENSE
)
