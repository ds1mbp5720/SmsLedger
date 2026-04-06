package com.example.smsledger.domain.usecase

import com.example.smsledger.domain.model.ParsingRule
import com.example.smsledger.domain.repository.ParsingRuleRepository
import kotlinx.coroutines.flow.Flow

/**
 * 모든 파싱 규칙 목록을 가져오는 유스케이스
 */
class GetParsingRulesUseCase(private val repository: ParsingRuleRepository) {
    /**
     * 유스케이스 실행
     * @return 파싱 규칙 리스트의 Flow
     */
    operator fun invoke(): Flow<List<ParsingRule>> = repository.getParsingRules()
}

/**
 * 새로운 파싱 규칙을 추가하는 유스케이스
 */
class AddParsingRuleUseCase(private val repository: ParsingRuleRepository) {
    /**
     * 유스케이스 실행
     * @param rule 추가할 파싱 규칙 정보
     */
    suspend operator fun invoke(rule: ParsingRule) = repository.addParsingRule(rule)
}

/**
 * 기존 파싱 규칙을 수정하는 유스케이스
 */
class UpdateParsingRuleUseCase(private val repository: ParsingRuleRepository) {
    /**
     * 유스케이스 실행
     * @param rule 수정할 파싱 규칙 정보
     */
    suspend operator fun invoke(rule: ParsingRule) = repository.updateParsingRule(rule)
}

/**
 * 파싱 규칙을 삭제하는 유스케이스
 */
class DeleteParsingRuleUseCase(private val repository: ParsingRuleRepository) {
    /**
     * 유스케이스 실행
     * @param rule 삭제할 파싱 규칙 정보
     */
    suspend operator fun invoke(rule: ParsingRule) = repository.deleteParsingRule(rule)
}
