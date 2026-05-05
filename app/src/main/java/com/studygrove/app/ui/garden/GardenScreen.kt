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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.studygrove.app.data.model.GameData
import com.studygrove.app.data.model.GardenObject
import com.studygrove.app.data.model.GardenObjectType
import com.studygrove.app.data.model.ShopCatalog
import com.studygrove.app.ui.StudyGroveScreen
import com.studygrove.app.ui.components.GardenObjectSprite
import com.studygrove.app.ui.inventory.GardenInventoryPanel
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
    onSelectGardenItem: (String) -> Unit,
    onClearSelection: () -> Unit,
    onNavigate: (StudyGroveScreen) -> Unit
) {
    var showGardenInventory by remember { mutableStateOf(false) }
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
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            SectionTitle("Garden", Modifier.weight(1f))
            PixelButton("Garden bag", color = GroveMoss, onClick = { showGardenInventory = true })
        }
        GardenGrid(gameData = gameData, onTileTapped = onTileTapped)
        SectionTitle("Daily quests")
        QuestList(gameData.dailyQuests.quests)
        SectionTitle("Grove paths")
        GardenActionGrid(onNavigate = onNavigate)
        Spacer(Modifier.height(8.dp))
    }
    if (showGardenInventory) {
        AlertDialog(
            onDismissRequest = { showGardenInventory = false },
            title = {
                Text("Garden inventory", fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
            },
            text = {
                GardenInventoryPanel(gameData = gameData) { itemId ->
                    showGardenInventory = false
                    onSelectGardenItem(itemId)
                }
            },
            confirmButton = {
                PixelButton("Close", color = GroveBerry) { showGardenInventory = false }
            }
        )
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
            PixelButton("House", color = Color(0xFF5F8FB1), modifier = Modifier.weight(1f), onClick = { onNavigate(StudyGroveScreen.HOUSE) })
            PixelButton("Profile", color = Color(0xFF7B6BB7), modifier = Modifier.weight(1f), onClick = { onNavigate(StudyGroveScreen.PROFILE) })
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
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
                val houseLevel = gameData.house.level.coerceIn(1, 5)
                val houseWidth = (3.8f + houseLevel * 0.9f) * u
                val houseHeight = 36f + houseLevel * 5f
                val houseX = 2.7f * u
                val houseY = size.height - 28f - houseHeight
                val wall = when (houseLevel) {
                    1 -> GroveClay
                    2 -> Color(0xFFD48655)
                    3 -> Color(0xFFB96E4A)
                    4 -> Color(0xFFC77A58)
                    else -> Color(0xFFD19864)
                }
                val roof = when (houseLevel) {
                    1 -> Color(0xFF8D4C3B)
                    2 -> Color(0xFF7F483A)
                    3 -> Color(0xFF5E5A45)
                    4 -> Color(0xFF6B4052)
                    else -> Color(0xFF4F5C74)
                }
                drawRect(GroveInk.copy(alpha = 0.18f), topLeft = Offset(houseX + 0.4f * u, size.height - 30f), size = Size(houseWidth, 5f))
                drawRect(wall.copy(alpha = 0.65f), topLeft = Offset(houseX + 0.8f * u, houseY + 0.5f * u), size = Size(houseWidth, houseHeight))
                drawRect(wall, topLeft = Offset(houseX, houseY), size = Size(houseWidth, houseHeight))
                drawRect(roof, topLeft = Offset(houseX - 0.8f * u, houseY - 1.25f * u), size = Size(houseWidth + 1.6f * u, 1.55f * u))
                drawRect(roof.copy(alpha = 0.82f), topLeft = Offset(houseX - 0.2f * u, houseY - 2.1f * u), size = Size(houseWidth - 0.2f * u, 1.05f * u))
                drawRect(GroveCream, topLeft = Offset(houseX + 1.6f * u, houseY + houseHeight - 28f), size = Size(1.05f * u, 28f))
                drawRect(GroveInk.copy(alpha = 0.35f), topLeft = Offset(houseX + 2.35f * u, houseY + houseHeight - 16f), size = Size(0.35f * u, 0.35f * u))
                drawRect(Color(0xFFFFE08A), topLeft = Offset(houseX + houseWidth - 1.55f * u, houseY + 1.25f * u), size = Size(0.95f * u, 0.95f * u))
                if (houseLevel >= 2) {
                    drawRect(Color(0xFFFFE08A), topLeft = Offset(houseX + houseWidth - 3.1f * u, houseY + 1.25f * u), size = Size(0.95f * u, 0.95f * u))
                    drawRect(Color(0xFF6B3D32), topLeft = Offset(houseX + houseWidth - 1.25f * u, houseY - 2.9f * u), size = Size(0.65f * u, 1.4f * u))
                }
                if (houseLevel >= 3) {
                    drawRect(wall.copy(alpha = 0.9f), topLeft = Offset(houseX + houseWidth - 0.1f * u, houseY + 0.55f * u), size = Size(1.7f * u, houseHeight - 6f))
                    drawRect(roof.copy(alpha = 0.85f), topLeft = Offset(houseX + houseWidth - 0.5f * u, houseY - 0.6f * u), size = Size(2.5f * u, 1.2f * u))
                    drawRect(Color(0xFFFFE08A), topLeft = Offset(houseX + houseWidth + 0.45f * u, houseY + 1.9f * u), size = Size(0.7f * u, 0.7f * u))
                }
                if (houseLevel >= 4) {
                    drawRect(wall.copy(alpha = 0.95f), topLeft = Offset(houseX + 2.95f * u, houseY - 1.9f * u), size = Size(2.2f * u, 2.5f * u))
                    drawRect(roof, topLeft = Offset(houseX + 2.55f * u, houseY - 2.7f * u), size = Size(3f * u, 0.85f * u))
                    drawRect(Color(0xFFFFE08A), topLeft = Offset(houseX + 3.6f * u, houseY - 1.2f * u), size = Size(0.75f * u, 0.75f * u))
                }
                if (houseLevel >= 5) {
                    repeat(4) { index ->
                        drawRect(Color(0xFFFFE08A), topLeft = Offset(houseX + (3.2f + index * 0.75f) * u, houseY + houseHeight - 1.35f * u), size = Size(0.42f * u, 0.95f * u))
                    }
                    drawRect(Color(0xFFFFF4C0), topLeft = Offset(houseX + houseWidth - 2.3f * u, houseY + houseHeight - 2.1f * u), size = Size(1.35f * u, 0.7f * u))
                }
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
                                fenceConnections = FenceConnections(
                                    left = objectsByTile[(x - 1) to y]?.type == GardenObjectType.FENCE,
                                    right = objectsByTile[(x + 1) to y]?.type == GardenObjectType.FENCE,
                                    up = objectsByTile[x to (y - 1)]?.type == GardenObjectType.FENCE,
                                    down = objectsByTile[x to (y + 1)]?.type == GardenObjectType.FENCE
                                ),
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
    fenceConnections: FenceConnections,
    clover: Boolean,
    onClick: () -> Unit
) {
    val base = if (clover) Color(0xFF8DC76A) else Color(0xFF7EB95A)
    val item = objectOnTile?.let { ShopCatalog.byId(it.shopItemId) }
    val objectType = objectOnTile?.type
    val isSurfaceObject = objectType == GardenObjectType.PATH ||
        objectType == GardenObjectType.WATER ||
        objectType == GardenObjectType.FENCE
    Box(
        modifier = modifier
            .background(base)
            .border(0.5.dp, GroveMoss.copy(alpha = 0.45f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.matchParentSize()) {
            when (objectType) {
                GardenObjectType.PATH -> drawGardenPathTile(item?.visualKey.orEmpty())
                GardenObjectType.WATER -> drawGardenPondTile()
                GardenObjectType.FENCE -> drawGardenFenceTile(fenceConnections)
                else -> if (clover && objectOnTile == null) {
                    val u = size.minDimension / 10f
                    drawRect(GroveLeaf.copy(alpha = 0.55f), topLeft = Offset(4.5f * u, 2.4f * u), size = Size(u, 5.2f * u))
                    drawRect(GroveMoss.copy(alpha = 0.45f), topLeft = Offset(3.6f * u, 3.2f * u), size = Size(2.8f * u, u))
                }
            }
        }
        if (!isSurfaceObject) {
            objectOnTile?.let {
                GardenObjectSprite(it, Modifier.matchParentSize().padding(2.dp))
            }
        }
    }
}

private data class FenceConnections(
    val left: Boolean = false,
    val right: Boolean = false,
    val up: Boolean = false,
    val down: Boolean = false
)

private fun DrawScope.drawGardenPathTile(key: String) {
    val u = size.minDimension / 10f
    if (key.contains("stone")) {
        drawRect(Color(0xFF6D7776), size = size)
        drawRect(Color(0xFFB9C2BD), topLeft = Offset(0.4f * u, 0.5f * u), size = Size(4.2f * u, 3.7f * u))
        drawRect(Color(0xFF939D98), topLeft = Offset(0.4f * u, 3.5f * u), size = Size(4.2f * u, 0.7f * u))
        drawRect(Color(0xFFAEB7B3), topLeft = Offset(5f * u, 0.7f * u), size = Size(4.4f * u, 4f * u))
        drawRect(Color(0xFF87918D), topLeft = Offset(5f * u, 4f * u), size = Size(4.4f * u, 0.7f * u))
        drawRect(Color(0xFFC5CDC8), topLeft = Offset(0.8f * u, 5.3f * u), size = Size(3.5f * u, 3.8f * u))
        drawRect(Color(0xFF959E9A), topLeft = Offset(0.8f * u, 8.4f * u), size = Size(3.5f * u, 0.7f * u))
        drawRect(Color(0xFFADB8B2), topLeft = Offset(4.8f * u, 5.5f * u), size = Size(4.6f * u, 3.6f * u))
        drawRect(Color(0xFF87918D), topLeft = Offset(4.8f * u, 8.3f * u), size = Size(4.6f * u, 0.8f * u))
    } else {
        drawRect(Color(0xFF8E5838), size = size)
        repeat(4) { row ->
            val top = row * 2.5f * u
            val plank = if (row % 2 == 0) Color(0xFFC98954) else Color(0xFFB87445)
            drawRect(plank, topLeft = Offset(0f, top), size = Size(size.width, 2.1f * u))
            drawRect(Color(0xFF74442C), topLeft = Offset(0f, top + 1.9f * u), size = Size(size.width, 0.35f * u))
            drawRect(Color.White.copy(alpha = 0.13f), topLeft = Offset(u, top + 0.4f * u), size = Size(3.5f * u, 0.35f * u))
        }
        drawRect(Color(0xFF5D3726).copy(alpha = 0.45f), topLeft = Offset(size.width - 0.7f * u, 0f), size = Size(0.7f * u, size.height))
    }
}

private fun DrawScope.drawGardenPondTile() {
    val u = size.minDimension / 10f
    drawRect(Color(0xFF447D6F), size = size)
    drawRect(Color(0xFF56A7BE), topLeft = Offset(0.8f * u, 0.9f * u), size = Size(8.4f * u, 7.9f * u))
    drawRect(Color(0xFF357E9C), topLeft = Offset(1.1f * u, 7.4f * u), size = Size(7.8f * u, 1.4f * u))
    drawRect(Color(0xFF8ED5D6), topLeft = Offset(2f * u, 2.1f * u), size = Size(3.2f * u, 0.7f * u))
    drawRect(Color(0xFFBDE7E7), topLeft = Offset(5.9f * u, 4.2f * u), size = Size(1.8f * u, 0.55f * u))
    drawRect(GroveLeaf.copy(alpha = 0.85f), topLeft = Offset(0f, 0f), size = Size(size.width, 0.7f * u))
    drawRect(GroveMoss.copy(alpha = 0.8f), topLeft = Offset(0f, size.height - 0.8f * u), size = Size(size.width, 0.8f * u))
    drawRect(GroveMoss.copy(alpha = 0.75f), topLeft = Offset(0f, 0f), size = Size(0.7f * u, size.height))
    drawRect(GroveLeaf.copy(alpha = 0.75f), topLeft = Offset(size.width - 0.7f * u, 0f), size = Size(0.7f * u, size.height))
}

private fun DrawScope.drawGardenFenceTile(connections: FenceConnections) {
    val u = size.minDimension / 10f
    val woodTop = Color(0xFFC9874D)
    val woodSide = Color(0xFF8D5634)
    val woodDark = Color(0xFF5B3525)
    val shadow = GroveInk.copy(alpha = 0.2f)

    drawRect(shadow, topLeft = Offset(1.3f * u, 7.8f * u), size = Size(7.5f * u, 1.1f * u))

    val horizontal = connections.left || connections.right || (!connections.up && !connections.down)
    val vertical = connections.up || connections.down

    if (horizontal) {
        val start = if (connections.left) 0f else 1.4f * u
        val end = if (connections.right) size.width else 8.6f * u
        val width = end - start
        drawRect(woodSide, topLeft = Offset(start, 5.4f * u), size = Size(width, 1.35f * u))
        drawRect(woodTop, topLeft = Offset(start, 4.6f * u), size = Size(width, 0.95f * u))
        drawRect(Color.White.copy(alpha = 0.18f), topLeft = Offset(start, 4.7f * u), size = Size(width, 0.25f * u))
    }

    if (vertical) {
        val start = if (connections.up) 0f else 1.3f * u
        val end = if (connections.down) size.height else 8.4f * u
        val height = end - start
        drawRect(woodDark, topLeft = Offset(5.4f * u, start), size = Size(1.25f * u, height))
        drawRect(woodSide, topLeft = Offset(4.4f * u, start), size = Size(1.45f * u, height))
        drawRect(woodTop, topLeft = Offset(3.8f * u, start), size = Size(0.9f * u, height))
        drawRect(Color.White.copy(alpha = 0.14f), topLeft = Offset(3.9f * u, start), size = Size(0.25f * u, height))
    }

    fun drawPost(topLeftX: Float, topLeftY: Float) {
        drawRect(shadow, topLeft = Offset(topLeftX + 0.25f * u, topLeftY + 4.9f * u), size = Size(2.2f * u, 0.8f * u))
        drawRect(woodDark, topLeft = Offset(topLeftX + 0.55f * u, topLeftY + 0.45f * u), size = Size(1.4f * u, 5.1f * u))
        drawRect(woodSide, topLeft = Offset(topLeftX, topLeftY), size = Size(1.75f * u, 5.3f * u))
        drawRect(woodTop, topLeft = Offset(topLeftX - 0.15f * u, topLeftY - 0.5f * u), size = Size(2.05f * u, 0.9f * u))
        drawRect(Color.White.copy(alpha = 0.16f), topLeft = Offset(topLeftX + 0.18f * u, topLeftY + 0.35f * u), size = Size(0.35f * u, 4.2f * u))
    }

    if (horizontal) {
        drawPost(if (connections.left) -0.7f * u else 1.2f * u, 2.1f * u)
        drawPost(4.15f * u, 1.8f * u)
        drawPost(if (connections.right) 8.9f * u else 7.1f * u, 2.1f * u)
    }
    if (vertical) {
        drawPost(4.15f * u, if (connections.up) -0.8f * u else 1.0f * u)
        if (!horizontal) {
            drawPost(4.15f * u, 4.1f * u)
        }
        drawPost(4.15f * u, if (connections.down) 8.2f * u else 6.4f * u)
    }
}
