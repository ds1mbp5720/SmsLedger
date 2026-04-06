package com.example.smsledger.domain.repository

import kotlinx.coroutines.flow.Flow

/**
 * 앱 설정 데이터에 접근하기 위한 리포지토리 인터페이스
 */
interface SettingsRepository {
    /**
     * 저장된 Gemini API 키를 Flow 형태로 조회
     * @return API 키의 Flow
     */
    fun getGeminiApiKey(): Flow<String>

    /**
     * Gemini API 키 저장
     * @param key 저장할 API 키
     */
    suspend fun saveGeminiApiKey(key: String)

    /**
     * 스마트 AI 기능 사용 여부를 Flow 형태로 조회
     * @return 사용 여부의 Flow
     */
    fun getUseSmartAi(): Flow<Boolean>

    /**
     * 스마트 AI 기능 사용 여부 설정
     * @param use 사용 여부 (true: 사용, false: 미사용)
     */
    suspend fun setUseSmartAi(use: Boolean)
}
