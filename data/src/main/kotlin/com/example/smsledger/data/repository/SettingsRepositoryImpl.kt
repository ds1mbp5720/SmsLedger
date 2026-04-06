package com.example.smsledger.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.smsledger.domain.repository.SettingsRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.onStart

/**
 * SettingsRepository의 구현체
 * SharedPreferences를 사용하여 앱 설정을 관리합니다.
 */
class SettingsRepositoryImpl(context: Context) : SettingsRepository {
    private val prefs: SharedPreferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE)

    /**
     * SharedPreferences에서 Gemini API 키를 가져오고 변경 사항을 감시
     */
    override fun getGeminiApiKey(): Flow<String> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
            if (key == "gemini_api_key") {
                trySend(sharedPreferences.getString("gemini_api_key", "") ?: "")
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        trySend(prefs.getString("gemini_api_key", "") ?: "")
        awaitClose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    /**
     * Gemini API 키 저장
     */
    override suspend fun saveGeminiApiKey(key: String) {
        prefs.edit().putString("gemini_api_key", key).apply()
    }

    /**
     * SharedPreferences에서 스마트 AI 사용 여부를 가져오고 변경 사항을 감시
     */
    override fun getUseSmartAi(): Flow<Boolean> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
            if (key == "use_smart_ai") {
                trySend(sharedPreferences.getBoolean("use_smart_ai", true))
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        trySend(prefs.getBoolean("use_smart_ai", true))
        awaitClose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    /**
     * 스마트 AI 사용 여부 설정
     */
    override suspend fun setUseSmartAi(use: Boolean) {
        prefs.edit().putBoolean("use_smart_ai", use).apply()
    }
}
