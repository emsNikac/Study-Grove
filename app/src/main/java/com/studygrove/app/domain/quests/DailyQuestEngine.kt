package com.studygrove.app.domain.quests

import com.studygrove.app.data.model.DailyQuest
import com.studygrove.app.data.model.DailyQuestCatalog
import com.studygrove.app.data.model.DailyQuestState

data class QuestUpdateResult(
    val state: DailyQuestState,
    val bonusCoins: Int
)

object DailyQuestEngine {
    fun ensureToday(state: DailyQuestState, today: String): DailyQuestState =
        if (state.localDate == today && state.quests.isNotEmpty()) state else DailyQuestCatalog.questsFor(today)

    fun onSessionCompleted(
        state: DailyQuestState,
        today: String,
        completedMinutes: Int
    ): QuestUpdateResult {
        val updatedToday = ensureToday(state, today)
        var quests = updatedToday.quests
        quests = advance(quests, "daily_focus_25", if (completedMinutes >= 25) 1 else 0)
        quests = advance(quests, "daily_60_minutes", completedMinutes)
        return withRewards(updatedToday.copy(quests = quests))
    }

    fun onCropPlanted(state: DailyQuestState, today: String): QuestUpdateResult {
        val updatedToday = ensureToday(state, today)
        return withRewards(updatedToday.copy(quests = advance(updatedToday.quests, "daily_plant_crop", 1)))
    }

    private fun advance(quests: List<DailyQuest>, id: String, amount: Int): List<DailyQuest> {
        if (amount <= 0) return quests
        return quests.map { quest ->
            if (quest.id != id || quest.completed) {
                quest
            } else {
                val nextProgress = (quest.progress + amount).coerceAtMost(quest.target)
                quest.copy(progress = nextProgress, completed = nextProgress >= quest.target)
            }
        }
    }

    private fun withRewards(state: DailyQuestState): QuestUpdateResult {
        var bonus = 0
        val rewarded = state.quests.map { quest ->
            if (quest.completed && !quest.rewarded) {
                bonus += quest.rewardCoins
                quest.copy(rewarded = true)
            } else {
                quest
            }
        }
        return QuestUpdateResult(state.copy(quests = rewarded), bonus)
    }
}
