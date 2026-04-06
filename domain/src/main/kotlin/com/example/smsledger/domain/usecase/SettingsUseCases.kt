package com.example.smsledger.domain.usecase

import com.example.smsledger.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow

/**
 * 저장된 Gemini API 키를 가져오는 유스케이스
 */
class GetGeminiApiKeyUseCase(private val repository: SettingsRepository) {
    /**
     * 유스케이스 실행
     * @return API 키의 Flow
     */
    operator fun invoke(): Flow<String> = repository.getGeminiApiKey()
}

/**
 * Gemini API 키를 저장하는 유스케이스
 */
class SaveGeminiApiKeyUseCase(private val repository: SettingsRepository) {
    /**
     * 유스케이스 실행
     * @param key 저장할 API 키
     */
    suspend operator fun invoke(key: String) = repository.saveGeminiApiKey(key)
}

/**
 * 스마트 AI 기능 사용 여부를 가져오는 유스케이스
 */
class GetUseSmartAiUseCase(private val repository: SettingsRepository) {
    /**
     * 유스케이스 실행
     * @return 사용 여부의 Flow
     */
    operator fun invoke(): Flow<Boolean> = repository.getUseSmartAi()
}

/**
 * 스마트 AI 기능 사용 여부를 설정하는 유스케이스
 */
class SetUseSmartAiUseCase(private val repository: SettingsRepository) {
    /**
     * 유스케이스 실행
     * @param use 사용 여부
     */
    suspend operator fun invoke(use: Boolean) = repository.setUseSmartAi(use)
}
