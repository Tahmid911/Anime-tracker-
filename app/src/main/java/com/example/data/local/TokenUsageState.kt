package com.example.data.local

data class TokenUsageState(
    val usedTokens: Long = 0L,
    val maxDailyTokens: Long = 100000L,
    val requestsCount: Int = 0,
    val lastResetTimestamp: Long = System.currentTimeMillis()
) {
    val remainingTokens: Long get() = (maxDailyTokens - usedTokens).coerceAtLeast(0L)
    val usagePercentage: Float get() = (usedTokens.toFloat() / maxDailyTokens.toFloat()).coerceIn(0f, 1f)
    val isWarning: Boolean get() = usagePercentage >= 0.8f
    val isDepleted: Boolean get() = remainingTokens <= 0L
}
