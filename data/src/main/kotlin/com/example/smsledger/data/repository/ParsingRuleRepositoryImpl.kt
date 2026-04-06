package com.example.smsledger.data.repository

import com.example.smsledger.data.local.ParsingRuleDao
import com.example.smsledger.data.local.toDomain
import com.example.smsledger.data.local.toEntity
import com.example.smsledger.domain.model.ParsingRule
import com.example.smsledger.domain.repository.ParsingRuleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * ParsingRuleRepository의 구현체
 * Room 데이터베이스(ParsingRuleDao)를 사용하여 파싱 규칙을 관리합니다.
 */
class ParsingRuleRepositoryImpl(private val dao: ParsingRuleDao) : ParsingRuleRepository {
    /**
     * 데이터베이스에서 모든 파싱 규칙을 가져와 도메인 모델 리스트로 변환하여 반환
     */
    override fun getParsingRules(): Flow<List<ParsingRule>> = 
        dao.getParsingRules().map { list -> list.map { it.toDomain() } }

    /**
     * 파싱 규칙 추가
     */
    override suspend fun addParsingRule(rule: ParsingRule) = dao.insert(rule.toEntity())

    /**
     * 파싱 규칙 수정
     */
    override suspend fun updateParsingRule(rule: ParsingRule) = dao.update(rule.toEntity())

    /**
     * 파싱 규칙 삭제
     */
    override suspend fun deleteParsingRule(rule: ParsingRule) = dao.delete(rule.toEntity())
}
