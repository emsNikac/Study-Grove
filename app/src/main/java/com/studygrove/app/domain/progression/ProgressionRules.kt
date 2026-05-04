package com.studygrove.app.domain.progression

object ProgressionRules {
    fun levelForXp(xp: Int): Int {
        var level = 1
        while (level < 99 && xp >= xpForLevel(level + 1)) {
            level += 1
        }
        return level
    }

    fun xpForLevel(level: Int): Int {
        if (level <= 1) return 0
        val completedLevels = level - 1
        return completedLevels * 50 + completedLevels * (completedLevels - 1) * 25
    }

    fun xpIntoCurrentLevel(xp: Int): Int {
        val level = levelForXp(xp)
        return xp - xpForLevel(level)
    }

    fun xpNeededForNextLevel(xp: Int): Int {
        val level = levelForXp(xp)
        return xpForLevel(level + 1) - xpForLevel(level)
    }
}
