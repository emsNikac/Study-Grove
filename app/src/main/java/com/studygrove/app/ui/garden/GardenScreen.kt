package com.studygrove.app.ui.garden

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.studygrove.app.data.model.GameData
import com.studygrove.app.data.model.GardenObject
import com.studygrove.app.data.model.ShopCatalog
import com.studygrove.app.ui.StudyGroveScreen
import com.studygrove.app.ui.components.GardenObjectSprite
import com.studygrove.app.ui.components.PixelButton
import com.studygrove.app.ui.components.PixelPanel
import com.studygrove.app.ui.components.QuestList
import com.studygrove.app.ui.components.SectionTitle
import com.studygrove.app.ui.components.StatChip
import com.studygrove.app.ui.theme.GroveBerry
import com.studygrove.app.ui.theme.GroveClay
import com.studygrove.app.ui.theme.GroveCream
import com.studygrove.app.ui.theme.GroveInk
import com.studygrove.app.ui.theme.GroveLeaf
import com.studygrove.app.ui.theme.GroveMoss
import com.studygrove.app.ui.theme.GroveNight
import com.studygrove.app.ui.theme.GroveSky
import java.time.Instant
import java.time.ZoneId

@Composable
fun GardenScreen(
    gameData: GameData,
    selectedItemId: String?,
    nowMillis: Long,
    onTileTapped: (Int, Int) -> Unit,
    onClearSelection: () -> Unit,
    onNavigate: (StudyGroveScreen) -> Unit
) {
    val isNight = Instant.ofEpochMilli(nowMillis).atZone(ZoneId.systemDefault()).hour !in 6..18
    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        GardenHeader(gameData = gameData, isNight = isNight)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            StatChip("Coins", gameData.player.coins.toString(), Modifier.weight(1f))
            StatChip("Level", gameData.player.level.toString(), Modifier.weight(1f))
            StatChip("Streak", "${gameData.player.currentStreak}d", Modifier.weight(1f))
        }
        selectedItemId?.let { itemId ->
            val item = ShopCatalog.byId(itemId)
            PixelPanel(color = Color(0xFFFFE9BD)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Placing ${item?.name.orEmpty()}",
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                    PixelButton("Cancel", color = GroveBerry, onClick = onClearSelection)
                }
            }
        }
        SectionTitle("Garden")
        GardenGrid(gameData = gameData, onTileTapped = onTileTapped)
        SectionTitle("Daily quests")
        QuestList(gameData.dailyQuests.quests)
        SectionTitle("Grove paths")
        GardenActionGrid(onNavigate = onNavigate)
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun GardenActionGrid(onNavigate: (StudyGroveScreen) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            PixelButton("Study", color = GroveMoss, modifier = Modifier.weight(1f), onClick = { onNavigate(StudyGroveScreen.TIMER) })
            PixelButton("Shop", color = GroveClay, modifier = Modifier.weight(1f), onClick = { onNavigate(StudyGroveScreen.SHOP) })
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            PixelButton("Inventory", color = GroveBerry, modifier = Modifier.weight(1f), onClick = { onNavigate(StudyGroveScreen.INVENTORY) })
            PixelButton("House", color = Color(0xFF5F8FB1), modifier = Modifier.weight(1f), onClick = { onNavigate(StudyGroveScreen.HOUSE) })
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            PixelButton("Profile", color = Color(0xFF7B6BB7), modifier = Modifier.weight(1f), onClick = { onNavigate(StudyGroveScreen.PROFILE) })
            PixelButton("Settings", color = Color(0xFF6E695E), modifier = Modifier.weight(1f), onClick = { onNavigate(StudyGroveScreen.SETTINGS) })
        }
    }
}

@Composable
private fun GardenHeader(gameData: GameData, isNight: Boolean) {
    val transition = rememberInfiniteTransition(label = "clouds")
    val drift by transition.animateFloat(
        initialValue = -40f,
        targetValue = 40f,
        animationSpec = infiniteRepeatable(tween(9000), RepeatMode.Reverse),
        label = "drift"
    )
    PixelPanel(color = if (isNight) GroveNight else GroveSky) {
        Box(modifier = Modifier.fillMaxWidth().height(148.dp)) {
            Canvas(Modifier.matchParentSize()) {
                val skyAccent = if (isNight) Color(0xFFFFE6A8) else Color(0xFFFFC94A)
                val u = size.width / 18f
                drawRect(skyAccent, topLeft = Offset(size.width - 4 * u, 1.2f * u), size = Size(2.4f * u, 2.4f * u))
                drawRect(Color.White.copy(alpha = if (isNight) 0.25f else 0.9f), topLeft = Offset(1.5f * u + drift, 2.3f * u), size = Size(4 * u, u))
                drawRect(Color.White.copy(alpha = if (isNight) 0.18f else 0.75f), topLeft = Offset(7.5f * u - drift, 3.1f * u), size = Size(3 * u, u))
                drawRect(Color(0xFF7AA558), topLeft = Offset(0f, size.height - 28f), size = Size(size.width, 28f))
                drawRect(GroveClay, topLeft = Offset(3 * u, size.height - 74f), size = Size(4.4f * u, 46f))
                drawRect(Color(0xFF8D4C3B), topLeft = Offset(2.4f * u, size.height - 94f), size = Size(5.6f * u, 22f))
                drawRect(GroveCream, topLeft = Offset(4.4f * u, size.height - 55f), size = Size(u, 27f))
                drawRect(GroveInk.copy(alpha = 0.25f), topLeft = Offset(6.2f * u, size.height - 58f), size = Size(0.8f * u, 0.8f * u))
                if (isNight) {
                    repeat(7) { index ->
                        drawRect(Color(0xFFFFF4BD), topLeft = Offset((index * 2 + 1) * u, (index % 3 + 1) * u), size = Size(4f, 4f))
                    }
                }
            }
            Column(
                modifier = Modifier.align(Alignment.BottomEnd).padding(12.dp),
                horizontalAlignment = Alignment.End
            ) {
                Text("Study Grove", fontSize = 26.sp, fontWeight = FontWeight.Black, color = if (isNight) GroveCream else GroveInk)
                Text(gameData.house.title, fontSize = 13.sp, color = if (isNight) GroveCream.copy(alpha = 0.8f) else GroveInk.copy(alpha = 0.72f))
            }
        }
    }
}

@Composable
private fun GardenGrid(gameData: GameData, onTileTapped: (Int, Int) -> Unit) {
    val haptic = LocalHapticFeedback.current
    val objectsByTile = remember(gameData.garden.objects) {
        gameData.garden.objects.associateBy { it.x to it.y }
    }
    PixelPanel(color = Color(0xFF9FCB72)) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val tileSize = maxWidth / gameData.garden.width
            Column(modifier = Modifier.fillMaxWidth()) {
                repeat(gameData.garden.height) { y ->
                    Row {
                        repeat(gameData.garden.width) { x ->
                            val obj = objectsByTile[x to y]
                            GardenTileBox(
                                modifier = Modifier.size(tileSize),
                                objectOnTile = obj,
                                clover = (x + y) % 5 == 0,
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onTileTapped(x, y)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GardenTileBox(
    modifier: Modifier,
    objectOnTile: GardenObject?,
    clover: Boolean,
    onClick: () -> Unit
) {
    val base = if (clover) Color(0xFF8DC76A) else Color(0xFF7EB95A)
    Box(
        modifier = modifier
            .background(base)
            .border(0.5.dp, GroveMoss.copy(alpha = 0.45f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (clover && objectOnTile == null) {
            Canvas(Modifier.size(12.dp)) {
                drawRect(GroveLeaf.copy(alpha = 0.55f), topLeft = Offset(4f, 2f), size = Size(4f, 8f))
            }
        }
        objectOnTile?.let {
            GardenObjectSprite(it, Modifier.padding(2.dp).size(34.dp))
        }
    }
}
