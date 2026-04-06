package com.example.smsledger.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.smsledger.domain.model.ParsingRule
import com.example.smsledger.domain.model.TransactionType

/**
 * SMS 파싱 규칙을 데이터베이스에 저장하기 위한 Room 엔티티 클래스
 * 
 * @property id 규칙의 고유 식별자 (자동 생성)
 * @property name 규칙 이름
 * @property senderNumber 발신 번호
 * @property amountPattern 금액 추출 패턴
 * @property storePattern 상점명 추출 패턴
 * @property isActive 활성화 여부
 * @property type 거래 유형 (INCOME, EXPENSE)
 */
@Entity(tableName = "parsing_rules")
data class ParsingRuleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val senderNumber: String? = null,
    val amountPattern: String,
    val storePattern: String,
    val isActive: Boolean = true,
    val type: String = "EXPENSE"
)

/**
 * ParsingRuleEntity를 도메인 모델 ParsingRule로 변환
 */
fun ParsingRuleEntity.toDomain() = ParsingRule(
    id = id,
    name = name,
    senderNumber = senderNumber,
    amountPattern = amountPattern,
    storePattern = storePattern,
    isActive = isActive,
    type = TransactionType.valueOf(type)
)

/**
 * 도메인 모델 ParsingRule을 ParsingRuleEntity로 변환
 */
fun ParsingRule.toEntity() = ParsingRuleEntity(
    id = id,
    name = name,
    senderNumber = senderNumber,
    amountPattern = amountPattern,
    storePattern = storePattern,
    isActive = isActive,
    type = type.name
)
