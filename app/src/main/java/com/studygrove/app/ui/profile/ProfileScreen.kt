package com.studygrove.app.ui.profile

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.studygrove.app.data.model.GameData
import com.studygrove.app.domain.progression.ProgressionRules
import com.studygrove.app.ui.components.AchievementRow
import com.studygrove.app.ui.components.CharacterSprite
import com.studygrove.app.ui.components.PixelPanel
import com.studygrove.app.ui.components.PixelProgressBar
import com.studygrove.app.ui.components.SectionTitle
import com.studygrove.app.ui.components.StatChip
import com.studygrove.app.ui.theme.GroveBerry
import com.studygrove.app.ui.theme.GroveCream
import com.studygrove.app.ui.theme.GroveInk
import com.studygrove.app.ui.theme.GroveLeaf
import com.studygrove.app.ui.theme.GroveMoss
import java.time.LocalDate

@Composable
fun ProfileScreen(gameData: GameData) {
    val levelXp = ProgressionRules.xpIntoCurrentLevel(gameData.player.xp)
    val nextXp = ProgressionRules.xpNeededForNextLevel(gameData.player.xp)
    Column(
        modifier = Modifier.verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SectionTitle("Profile")
        PixelPanel(color = GroveCream) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                CharacterSprite(gameData.character, Modifier.size(92.dp))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Level ${gameData.player.level} Grove Keeper", fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace, fontSize = 18.sp)
                    PixelProgressBar(progress = levelXp.toFloat() / nextXp.toFloat(), fill = GroveLeaf)
                    Text("$levelXp / $nextXp XP to next level", color = GroveInk.copy(alpha = 0.7f), fontWeight = FontWeight.Bold)
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            StatChip("Minutes", gameData.player.totalStudyMinutes.toString(), Modifier.weight(1f))
            StatChip("Sessions", gameData.player.completedSessions.toString(), Modifier.weight(1f))
            StatChip("Best streak", "${gameData.player.longestStreak}d", Modifier.weight(1f))
        }
        SectionTitle("Study history")
        StudyHistoryCalendar(gameData)
        SectionTitle("Achievements")
        Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            gameData.achievements.forEach { achievement ->
                Box(Modifier.fillMaxWidth()) {
                    AchievementRow(achievement)
                }
            }
        }
    }
}

@Composable
private fun StudyHistoryCalendar(gameData: GameData) {
    val minutesByDate = remember(gameData.sessions) {
        gameData.sessions
            .filter { it.completed }
            .groupBy { it.localDate }
            .mapValues { (_, sessions) -> sessions.sumOf { it.completedMinutes } }
    }
    val days = remember {
        val today = LocalDate.now()
        (27 downTo 0).map { today.minusDays(it.toLong()) }
    }
    PixelPanel(color = GroveCream) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Last 28 days", fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                days.chunked(7).forEach { week ->
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        week.forEach { day ->
                            val minutes = minutesByDate[day.toString()] ?: 0
                            val color = when {
                                minutes >= 60 -> GroveMoss
                                minutes >= 25 -> GroveLeaf
                                minutes > 0 -> GroveBerry
                                else -> Color(0xFFD9CBB4)
                            }
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(color)
                                    .border(1.dp, GroveInk.copy(alpha = 0.25f))
                            ) {
                                Canvas(Modifier.matchParentSize()) {
                                    if (minutes > 0) {
                                        drawRect(Color.White.copy(alpha = 0.22f), topLeft = Offset(4f, 4f), size = Size(8f, 8f))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
