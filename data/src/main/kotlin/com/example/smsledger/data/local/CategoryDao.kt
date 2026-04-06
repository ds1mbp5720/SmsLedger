package com.example.smsledger.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * 카테고리 데이터베이스 접근 객체 (DAO)
 */
@Dao
interface CategoryDao {
    /**
     * 모든 카테고리 목록 조회
     * @return 카테고리 엔티티 리스트의 Flow
     */
    @Query("SELECT * FROM categories")
    fun getCategories(): Flow<List<CategoryEntity>>

    /**
     * 새로운 카테고리 삽입
     * @param category 삽입할 카테고리 엔티티
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(category: CategoryEntity)

    /**
     * 카테고리 정보 수정
     * @param category 수정할 카테고리 엔티티
     */
    @Update
    suspend fun update(category: CategoryEntity)

    /**
     * 카테고리 삭제
     * @param category 삭제할 카테고리 엔티티
     */
    @Delete
    suspend fun delete(category: CategoryEntity)
}
