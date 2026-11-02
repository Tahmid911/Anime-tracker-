package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class UserPreferencesManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("anime_tracker_prefs", Context.MODE_PRIVATE)

    private val _themeMode = MutableStateFlow(prefs.getString("theme_mode", "SYSTEM") ?: "SYSTEM")
    val themeMode: StateFlow<String> = _themeMode.asStateFlow()

    private val _customApiKey = MutableStateFlow(prefs.getString("custom_api_key", "") ?: "")
    val customApiKey: StateFlow<String> = _customApiKey.asStateFlow()

    private val _selectedApiModel = MutableStateFlow(prefs.getString("selected_api_model", "gemini-2.5-flash") ?: "gemini-2.5-flash")
    val selectedApiModel: StateFlow<String> = _selectedApiModel.asStateFlow()

    private val _tokenUsageState = MutableStateFlow(loadTokenUsage())
    val tokenUsageState: StateFlow<TokenUsageState> = _tokenUsageState.asStateFlow()

    fun setThemeMode(mode: String) {
        prefs.edit().putString("theme_mode", mode).apply()
        _themeMode.value = mode
    }

    fun setCustomApiKey(key: String) {
        prefs.edit().putString("custom_api_key", key).apply()
        _customApiKey.value = key
    }

    fun setSelectedApiModel(model: String) {
        prefs.edit().putString("selected_api_model", model).apply()
        _selectedApiModel.value = model
    }

    fun recordApiUsage(tokens: Long) {
        val current = loadTokenUsage()
        val now = System.currentTimeMillis()
        val isSameDay = (now - current.lastResetTimestamp) < 24 * 60 * 60 * 1000L

        val updated = if (isSameDay) {
            current.copy(
                usedTokens = current.usedTokens + tokens,
                requestsCount = current.requestsCount + 1
            )
        } else {
            TokenUsageState(
                usedTokens = tokens,
                requestsCount = 1,
                lastResetTimestamp = now
            )
        }
        saveTokenUsage(updated)
        _tokenUsageState.value = updated
    }

    fun resetTokenUsage() {
        val resetState = TokenUsageState(lastResetTimestamp = System.currentTimeMillis())
        saveTokenUsage(resetState)
        _tokenUsageState.value = resetState
    }

    private fun loadTokenUsage(): TokenUsageState {
        val now = System.currentTimeMillis()
        val lastReset = prefs.getLong("token_last_reset", now)
        if ((now - lastReset) >= 24 * 60 * 60 * 1000L) {
            val resetState = TokenUsageState(lastResetTimestamp = now)
            saveTokenUsage(resetState)
            return resetState
        }
        return TokenUsageState(
            usedTokens = prefs.getLong("token_used", 0L),
            maxDailyTokens = prefs.getLong("token_max", 100000L),
            requestsCount = prefs.getInt("token_requests", 0),
            lastResetTimestamp = lastReset
        )
    }

    private fun saveTokenUsage(state: TokenUsageState) {
        prefs.edit()
            .putLong("token_used", state.usedTokens)
            .putLong("token_max", state.maxDailyTokens)
            .putInt("token_requests", state.requestsCount)
            .putLong("token_last_reset", state.lastResetTimestamp)
            .apply()
    }
}
