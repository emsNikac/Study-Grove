package com.studygrove.app.domain.achievements

import com.studygrove.app.data.model.Achievement
import com.studygrove.app.data.model.GameData

object AchievementEngine {
    fun evaluate(gameData: GameData, nowMillis: Long): List<Achievement> =
        gameData.achievements.map { achievement ->
            if (achievement.unlocked) {
                achievement
            } else if (shouldUnlock(achievement.id, gameData)) {
                achievement.copy(unlocked = true, unlockedAtEpochMillis = nowMillis)
            } else {
                achievement
            }
        }

    private fun shouldUnlock(id: String, gameData: GameData): Boolean = when (id) {
        "first_focus" -> gameData.player.completedSessions >= 1
        "tiny_scholar" -> gameData.player.totalStudyMinutes >= 60
        "garden_starter" -> gameData.player.gardenItemsBought >= 5
        "cozy_home" -> gameData.player.furnitureItemsBought >= 5
        "one_week_strong" -> gameData.player.currentStreak >= 7
        "deep_worker" -> gameData.sessions.any { it.completed && it.completedMinutes >= 50 }
        "study_grove_master" -> gameData.player.totalStudyMinutes >= 1_000
        else -> false
    }
}
