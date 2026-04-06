package com.example.smsledger.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * 파싱 규칙 데이터베이스 접근 객체 (DAO)
 */
@Dao
interface ParsingRuleDao {
    /**
     * 모든 파싱 규칙 목록 조회
     * @return 파싱 규칙 엔티티 리스트의 Flow
     */
    @Query("SELECT * FROM parsing_rules")
    fun getParsingRules(): Flow<List<ParsingRuleEntity>>

    /**
     * 새로운 파싱 규칙 삽입
     * @param rule 삽입할 파싱 규칙 엔티티
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(rule: ParsingRuleEntity)

    /**
     * 파싱 규칙 정보 수정
     * @param rule 수정할 파싱 규칙 엔티티
     */
    @Update
    suspend fun update(rule: ParsingRuleEntity)

    /**
     * 파싱 규칙 삭제
     * @param rule 삭제할 파싱 규칙 엔티티
     */
    @Delete
    suspend fun delete(rule: ParsingRuleEntity)
}
