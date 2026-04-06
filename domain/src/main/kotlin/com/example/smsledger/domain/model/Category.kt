package com.example.smsledger.domain.model

/**
 * 가계부의 카테고리 정보를 담는 데이터 클래스
 * 
 * @property id 카테고리의 고유 식별자 (기본값: 0)
 * @property name 카테고리 이름 (예: 식비, 교통비 등)
 */
data class Category(
    val id: Long = 0,
    val name: String
)
