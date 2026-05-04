package com.studygrove.app.domain.timer

import com.studygrove.app.data.model.ActiveTimerState
import kotlin.math.max

object FocusTimerEngine {
    const val MaxPauses = 3
    private const val MinuteMillis = 60_000L

    fun durationMillis(timer: ActiveTimerState): Long = timer.durationMinutes * MinuteMillis

    fun elapsedMillis(timer: ActiveTimerState, nowMillis: Long): Long {
        val pauseInProgress = timer.pauseStartedAtEpochMillis?.let { pauseStart ->
            max(0L, nowMillis - pauseStart)
        } ?: 0L
        return max(
            0L,
            nowMillis - timer.startedAtEpochMillis - timer.accumulatedPausedMillis - pauseInProgress
        )
    }

    fun remainingMillis(timer: ActiveTimerState, nowMillis: Long): Long =
        max(0L, durationMillis(timer) - elapsedMillis(timer, nowMillis))

    fun elapsedWholeMinutes(timer: ActiveTimerState, nowMillis: Long): Int =
        (elapsedMillis(timer, nowMillis) / MinuteMillis).toInt()

    fun isComplete(timer: ActiveTimerState, nowMillis: Long): Boolean =
        remainingMillis(timer, nowMillis) <= 0L

    fun finishEpochMillis(timer: ActiveTimerState, nowMillis: Long): Long =
        nowMillis + remainingMillis(timer, nowMillis)
}
