package com.studygrove.app.domain.rewards

import com.studygrove.app.data.model.StudyReward
import kotlin.math.roundToInt

object RewardCalculator {
    fun fullSessionBonus(minutes: Int): Int = when (minutes) {
        15 -> 5
        25 -> 10
        50 -> 25
        else -> (minutes / 5).coerceIn(2, 30)
    }

    fun streakMultiplier(streak: Int): Double = when {
        streak >= 14 -> 0.35
        streak >= 7 -> 0.20
        streak >= 3 -> 0.10
        else -> 0.0
    }

    fun nextStreak(
        lastStudyDate: String?,
        currentStreak: Int,
        today: String,
        yesterday: String
    ): Int = when (lastStudyDate) {
        today -> currentStreak.coerceAtLeast(1)
        yesterday -> currentStreak + 1
        else -> 1
    }

    fun calculate(
        minutes: Int,
        newStreak: Int,
        questBonus: Int,
        harvestBonus: Int,
        newLevel: Int,
        completedSessionsBefore: Int
    ): StudyReward {
        val base = minutes
        val full = fullSessionBonus(minutes)
        val streakBonus = ((base + full) * streakMultiplier(newStreak)).roundToInt()
        val total = base + full + streakBonus + questBonus + harvestBonus
        val messages = listOf(
            "Your garden grew because you showed up.",
            "Small focus sessions build big dreams.",
            "Nice work! Your grove is brighter now.",
            "You studied, your world improved.",
            "Come back tomorrow to keep your streak alive."
        )
        return StudyReward(
            baseCoins = base,
            fullSessionBonus = full,
            streakBonus = streakBonus,
            questBonus = questBonus,
            harvestBonus = harvestBonus,
            totalCoins = total,
            xpEarned = minutes,
            newLevel = newLevel,
            newStreak = newStreak,
            message = messages[completedSessionsBefore % messages.size]
        )
    }
}
