package com.example.smsledger.domain.repository

import com.example.smsledger.domain.model.ParsingRule
import kotlinx.coroutines.flow.Flow

/**
 * SMS 파싱 규칙 데이터에 접근하기 위한 리포지토리 인터페이스
 */
interface ParsingRuleRepository {
    /**
     * 모든 파싱 규칙 목록을 Flow 형태로 조회
     * @return 파싱 규칙 리스트의 Flow
     */
    fun getParsingRules(): Flow<List<ParsingRule>>

    /**
     * 새로운 파싱 규칙 추가
     * @param rule 추가할 파싱 규칙 정보
     */
    suspend fun addParsingRule(rule: ParsingRule)

    /**
     * 기존 파싱 규칙 수정
     * @param rule 수정할 파싱 규칙 정보
     */
    suspend fun updateParsingRule(rule: ParsingRule)

    /**
     * 파싱 규칙 삭제
     * @param rule 삭제할 파싱 규칙 정보
     */
    suspend fun deleteParsingRule(rule: ParsingRule)
}
