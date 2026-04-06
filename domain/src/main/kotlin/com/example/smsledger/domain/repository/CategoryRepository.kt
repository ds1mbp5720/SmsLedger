package com.example.smsledger.domain.repository

import com.example.smsledger.domain.model.Category
import kotlinx.coroutines.flow.Flow

/**
 * 카테고리 데이터에 접근하기 위한 리포지토리 인터페이스
 */
interface CategoryRepository {
    /**
     * 모든 카테고리 목록을 Flow 형태로 조회
     * @return 카테고리 리스트의 Flow
     */
    fun getCategories(): Flow<List<Category>>

    /**
     * 새로운 카테고리 추가
     * @param category 추가할 카테고리 정보
     */
    suspend fun addCategory(category: Category)

    /**
     * 기존 카테고리 정보 수정 (이름 등)
     * @param category 수정할 카테고리 정보
     */
    suspend fun updateCategory(category: Category)

    /**
     * 카테고리 삭제
     * @param category 삭제할 카테고리 정보
     */
    suspend fun deleteCategory(category: Category)
}
