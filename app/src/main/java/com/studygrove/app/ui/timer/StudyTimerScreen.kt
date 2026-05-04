package com.studygrove.app.ui.timer

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.studygrove.app.data.model.ActiveTimerState
import com.studygrove.app.data.model.GameData
import com.studygrove.app.data.model.TimerPreset
import com.studygrove.app.domain.timer.FocusTimerEngine
import com.studygrove.app.ui.components.PixelButton
import com.studygrove.app.ui.components.PixelPanel
import com.studygrove.app.ui.components.PixelProgressBar
import com.studygrove.app.ui.components.SectionTitle
import com.studygrove.app.ui.components.formatTimer
import com.studygrove.app.ui.theme.GroveBerry
import com.studygrove.app.ui.theme.GroveClay
import com.studygrove.app.ui.theme.GroveCream
import com.studygrove.app.ui.theme.GroveInk
import com.studygrove.app.ui.theme.GroveLeaf
import com.studygrove.app.ui.theme.GroveMoss

@Composable
fun StudyTimerScreen(
    gameData: GameData,
    nowMillis: Long,
    onStart: (TimerPreset, Int?) -> Unit,
    onPause: () -> Unit,
    onCancel: () -> Unit
) {
    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SectionTitle("Study timer")
        gameData.activeTimer?.let { timer ->
            ActiveTimerCard(timer = timer, nowMillis = nowMillis, onPause = onPause, onCancel = onCancel)
        } ?: TimerPresets(
            defaultMinutes = gameData.settings.defaultTimerMinutes,
            onStart = onStart
        )
    }
}

@Composable
private fun TimerPresets(defaultMinutes: Int, onStart: (TimerPreset, Int?) -> Unit) {
    var customMinutes by remember(defaultMinutes) { mutableIntStateOf(defaultMinutes.coerceIn(5, 180)) }
    PixelPanel(color = GroveCream) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            FocusAnimation(active = false)
            Text(
                "Choose a gentle focus length. Complete sessions earn coins, XP, crop growth, and streak progress.",
                color = GroveInk.copy(alpha = 0.86f),
                fontWeight = FontWeight.Bold
            )
            PixelButton("Quick Study 15m", modifier = Modifier.fillMaxWidth(), onClick = { onStart(TimerPreset.QUICK_STUDY, null) })
            PixelButton("Focus Session 25m", modifier = Modifier.fillMaxWidth(), color = GroveClay, onClick = { onStart(TimerPreset.FOCUS_SESSION, null) })
            PixelButton("Deep Work 50m", modifier = Modifier.fillMaxWidth(), color = GroveBerry, onClick = { onStart(TimerPreset.DEEP_WORK, null) })
            Text("Custom Timer: $customMinutes minutes", color = GroveInk, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
            Slider(
                value = customMinutes.toFloat(),
                onValueChange = { customMinutes = it.toInt().coerceIn(5, 180) },
                valueRange = 5f..180f,
                steps = 174
            )
            PixelButton(
                "Start Custom",
                modifier = Modifier.fillMaxWidth(),
                color = GroveMoss,
                onClick = { onStart(TimerPreset.CUSTOM, customMinutes) }
            )
        }
    }
}

@Composable
private fun ActiveTimerCard(
    timer: ActiveTimerState,
    nowMillis: Long,
    onPause: () -> Unit,
    onCancel: () -> Unit
) {
    val remaining = FocusTimerEngine.remainingMillis(timer, nowMillis)
    val elapsed = FocusTimerEngine.elapsedMillis(timer, nowMillis)
    val progress = elapsed.toFloat() / FocusTimerEngine.durationMillis(timer).toFloat()
    PixelPanel(color = GroveCream) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(14.dp)) {
            FocusAnimation(active = !timer.isPaused)
            Text(
                if (timer.isPaused) "Paused" else timer.preset.label,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                fontSize = 20.sp
            )
            Text(
                formatTimer(remaining),
                fontWeight = FontWeight.Black,
                fontSize = 54.sp,
                fontFamily = FontFamily.Monospace,
                textAlign = TextAlign.Center
            )
            PixelProgressBar(progress = progress, modifier = Modifier.fillMaxWidth(), height = 22.dp, fill = GroveLeaf)
            Text(
                "Pauses used: ${timer.pauseCount}/3",
                color = GroveInk.copy(alpha = 0.68f),
                fontWeight = FontWeight.Bold
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                PixelButton(
                    text = if (timer.isPaused) "Resume" else "Pause",
                    modifier = Modifier.weight(1f),
                    color = GroveClay,
                    onClick = onPause
                )
                PixelButton(
                    text = "Cancel",
                    modifier = Modifier.weight(1f),
                    color = GroveBerry,
                    onClick = onCancel
                )
            }
            Text(
                if (timer.isPaused) "Take the breath you need. Your timer is waiting." else "Your garden is resting while you focus.",
                color = GroveInk.copy(alpha = 0.72f),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun FocusAnimation(active: Boolean) {
    val transition = rememberInfiniteTransition(label = "focus")
    val pulse by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(if (active) 1800 else 3200), RepeatMode.Reverse),
        label = "pulse"
    )
    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(132.dp)) {
            val u = size.minDimension / 12f
            drawRect(Color(0xFFA9D7D8), topLeft = Offset(0f, 0f), size = size)
            drawRect(Color(0xFFFFC94A), topLeft = Offset((4.2f + pulse) * u, 2 * u), size = Size(3.2f * u, 3.2f * u))
            drawRect(GroveLeaf, topLeft = Offset(0f, 8 * u), size = Size(12 * u, 4 * u))
            drawRect(GroveMoss, topLeft = Offset(5 * u, 5.5f * u), size = Size(2 * u, 4 * u))
            drawRect(Color(0xFFE8F5B4), topLeft = Offset(4 * u, 4.2f * u), size = Size(4 * u, 2.5f * u))
            drawRect(GroveInk.copy(alpha = 0.15f), topLeft = Offset(2 * u, 9.4f * u), size = Size(8 * u, 0.8f * u))
        }
    }
    Spacer(Modifier.height(2.dp))
}
