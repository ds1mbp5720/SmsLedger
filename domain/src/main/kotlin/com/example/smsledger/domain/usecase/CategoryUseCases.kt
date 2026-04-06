package com.example.smsledger.domain.usecase

import com.example.smsledger.domain.model.Category
import com.example.smsledger.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow

/**
 * 모든 카테고리 목록을 가져오는 유스케이스
 */
class GetCategoriesUseCase(private val repository: CategoryRepository) {
    /**
     * 유스케이스 실행
     * @return 카테고리 리스트의 Flow
     */
    operator fun invoke(): Flow<List<Category>> = repository.getCategories()
}

/**
 * 새로운 카테고리를 추가하는 유스케이스
 */
class AddCategoryUseCase(private val repository: CategoryRepository) {
    /**
     * 유스케이스 실행
     * @param category 추가할 카테고리 정보
     */
    suspend operator fun invoke(category: Category) = repository.addCategory(category)
}

/**
 * 기존 카테고리 정보를 수정하는 유스케이스
 */
class UpdateCategoryUseCase(private val repository: CategoryRepository) {
    /**
     * 유스케이스 실행
     * @param category 수정할 카테고리 정보
     */
    suspend operator fun invoke(category: Category) = repository.updateCategory(category)
}

/**
 * 카테고리를 삭제하는 유스케이스
 */
class DeleteCategoryUseCase(private val repository: CategoryRepository) {
    /**
     * 유스케이스 실행
     * @param category 삭제할 카테고리 정보
     */
    suspend operator fun invoke(category: Category) = repository.deleteCategory(category)
}
