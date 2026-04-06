package com.example.smsledger.data.repository

import com.example.smsledger.data.local.CategoryDao
import com.example.smsledger.data.local.toDomain
import com.example.smsledger.data.local.toEntity
import com.example.smsledger.domain.model.Category
import com.example.smsledger.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * CategoryRepository의 구현체
 * Room 데이터베이스(CategoryDao)를 사용하여 카테고리를 관리합니다.
 */
class CategoryRepositoryImpl(private val dao: CategoryDao) : CategoryRepository {
    /**
     * 데이터베이스에서 모든 카테고리를 가져와 도메인 모델 리스트로 변환하여 반환
     */
    override fun getCategories(): Flow<List<Category>> = 
        dao.getCategories().map { list -> list.map { it.toDomain() } }

    /**
     * 카테고리 추가
     */
    override suspend fun addCategory(category: Category) = dao.insert(category.toEntity())
    /**
     * 카테고리 수정
     */
    override suspend fun updateCategory(category: Category) = dao.update(category.toEntity())
    /**
     * 카테고리 삭제
     */
    override suspend fun deleteCategory(category: Category) = dao.delete(category.toEntity())
}
