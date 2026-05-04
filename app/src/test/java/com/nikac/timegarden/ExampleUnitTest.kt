package com.studygrove.app

import com.studygrove.app.data.model.ActiveTimerState
import com.studygrove.app.data.model.TimerPreset
import com.studygrove.app.domain.progression.ProgressionRules
import com.studygrove.app.domain.rewards.RewardCalculator
import com.studygrove.app.domain.timer.FocusTimerEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun rewardCalculator_appliesPresetBonusAndStreak() {
        val reward = RewardCalculator.calculate(
            minutes = 25,
            newStreak = 7,
            questBonus = 0,
            harvestBonus = 0,
            newLevel = 1,
            completedSessionsBefore = 0
        )

        assertEquals(25, reward.baseCoins)
        assertEquals(10, reward.fullSessionBonus)
        assertEquals(7, reward.streakBonus)
        assertEquals(42, reward.totalCoins)
    }

    @Test
    fun progression_increasesGraduallyWithXp() {
        assertEquals(1, ProgressionRules.levelForXp(49))
        assertEquals(2, ProgressionRules.levelForXp(50))
        assertEquals(3, ProgressionRules.levelForXp(150))
    }

    @Test
    fun timer_excludesPausedTimeFromElapsedProgress() {
        val timer = ActiveTimerState(
            preset = TimerPreset.FOCUS_SESSION,
            durationMinutes = 25,
            startedAtEpochMillis = 0L,
            accumulatedPausedMillis = 5_000L,
            pauseStartedAtEpochMillis = 10_000L,
            pauseCount = 1
        )

        assertEquals(5_000L, FocusTimerEngine.elapsedMillis(timer, 20_000L))
        assertFalse(FocusTimerEngine.isComplete(timer, 20_000L))
        assertTrue(FocusTimerEngine.remainingMillis(timer, 20_000L) > 0L)
    }
}
