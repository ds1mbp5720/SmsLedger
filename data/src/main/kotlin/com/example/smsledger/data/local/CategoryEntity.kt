package com.example.smsledger.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.smsledger.domain.model.Category

/**
 * 카테고리 정보를 데이터베이스에 저장하기 위한 Room 엔티티 클래스
 * 
 * @property id 카테고리의 고유 식별자 (자동 생성)
 * @property name 카테고리 이름
 */
@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String
)

/**
 * CategoryEntity를 도메인 모델 Category로 변환
 */
fun CategoryEntity.toDomain() = Category(id = id, name = name)

/**
 * 도메인 모델 Category를 CategoryEntity로 변환
 */
fun Category.toEntity() = CategoryEntity(id = id, name = name)
