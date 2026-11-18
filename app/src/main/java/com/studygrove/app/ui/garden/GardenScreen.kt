package com.studygrove.app.ui.garden

import androidx.compose.animation.core.LinearEasing
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.studygrove.app.data.model.GameData
import com.studygrove.app.data.model.GardenObjectFootprints
import com.studygrove.app.data.model.GardenObjectType
import com.studygrove.app.data.model.HouseSpecialUnlocks
import com.studygrove.app.data.model.ShopCatalog
import com.studygrove.app.ui.StudyGroveScreen
import com.studygrove.app.ui.components.MapTileGrid
import com.studygrove.app.ui.components.GardenArt
import com.studygrove.app.ui.components.PixelSheet
import com.studygrove.app.ui.components.PixelTile
import com.studygrove.app.ui.components.connectionMask
import com.studygrove.app.ui.components.drawPixelFrame
import com.studygrove.app.ui.components.rememberSheet
import com.studygrove.app.ui.components.GardenObjectSprite
import com.studygrove.app.ui.inventory.GardenInventoryPanel
import com.studygrove.app.ui.components.PixelButton
import com.studygrove.app.ui.components.PixelPanel
import com.studygrove.app.ui.components.QuestList
import com.studygrove.app.ui.components.SectionTitle
import com.studygrove.app.ui.components.StatChip
import com.studygrove.app.ui.components.ZoomableMapViewport
import com.studygrove.app.ui.theme.GroveBerry
import com.studygrove.app.ui.theme.GroveClay
import com.studygrove.app.ui.theme.GroveCream
import com.studygrove.app.ui.theme.GroveInk
import com.studygrove.app.ui.theme.GroveMoss
import com.studygrove.app.ui.theme.GroveNight
import com.studygrove.app.ui.theme.GroveSky
import java.time.Instant
import java.time.ZoneId
import kotlin.math.roundToInt

@Composable
fun GardenScreen(
    gameData: GameData,
    selectedItemId: String?,
    nowMillis: Long,
    onTileTapped: (Int, Int) -> Unit,
    onSelectGardenItem: (String) -> Unit,
    onClearSelection: () -> Unit,
    onHouseThemeChanged: (String, Boolean) -> Unit,
    onNavigate: (StudyGroveScreen) -> Unit
) {
    var showGardenInventory by remember { mutableStateOf(false) }
    var showHouseThemes by remember { mutableStateOf(false) }
    val visualTheme = gameData.upgrades.visualTheme
    val isNight = visualTheme == "night" || Instant.ofEpochMilli(nowMillis).atZone(ZoneId.systemDefault()).hour !in 6..18
    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        GardenHeader(gameData = gameData, isNight = isNight, visualTheme = visualTheme)
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
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PixelButton("House themes", color = Color(0xFF7B6BB7), onClick = { showHouseThemes = true })
                PixelButton("Garden bag", color = GroveMoss, onClick = { showGardenInventory = true })
            }
        }
        GardenGrid(gameData, selectedItemId != null, isNight, onTileTapped)
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
    if (showHouseThemes) {
        AlertDialog(
            onDismissRequest = { showHouseThemes = false },
            title = {
                Text("House themes", fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
            },
            text = {
                HouseThemesPanel(
                    unlockedThemes = gameData.house.unlockedThemes,
                    disabledThemes = gameData.house.disabledThemes,
                    onHouseThemeChanged = onHouseThemeChanged
                )
            },
            confirmButton = {
                PixelButton("Close", color = GroveMoss) { showHouseThemes = false }
            }
        )
    }
}

@Composable
private fun HouseThemesPanel(
    unlockedThemes: List<String>,
    disabledThemes: List<String>,
    onHouseThemeChanged: (String, Boolean) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (unlockedThemes.isEmpty()) {
            Text(
                "No house themes unlocked yet. Late-game house upgrades will appear here.",
                color = GroveInk.copy(alpha = 0.72f)
            )
        } else {
            unlockedThemes.forEach { themeId ->
                val enabled = themeId !in disabledThemes
                PixelPanel(color = GroveCream) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                houseThemeName(themeId),
                                color = GroveInk,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                if (enabled) "Exterior effect is on." else "Exterior effect is off.",
                                color = GroveInk.copy(alpha = 0.68f),
                                fontSize = 12.sp
                            )
                        }
                        PixelButton(
                            text = if (enabled) "On" else "Off",
                            color = if (enabled) GroveMoss else GroveClay,
                            onClick = { onHouseThemeChanged(themeId, !enabled) }
                        )
                    }
                }
            }
        }
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
private fun GardenHeader(gameData: GameData, isNight: Boolean, visualTheme: String?) {
    val enabledHouseThemes = gameData.house.unlockedThemes.filterNot { it in gameData.house.disabledThemes }
    val activeHouseTheme = when {
        HouseSpecialUnlocks.GrandEstateTheme in enabledHouseThemes -> HouseSpecialUnlocks.GrandEstateTheme
        HouseSpecialUnlocks.CelestialTheme in enabledHouseThemes -> HouseSpecialUnlocks.CelestialTheme
        HouseSpecialUnlocks.DreamLibraryTheme in enabledHouseThemes -> HouseSpecialUnlocks.DreamLibraryTheme
        else -> null
    }
    val themeRow = when (activeHouseTheme) {
        HouseSpecialUnlocks.DreamLibraryTheme -> 1
        HouseSpecialUnlocks.CelestialTheme -> 2
        HouseSpecialUnlocks.GrandEstateTheme -> 3
        else -> 0
    }
    val sceneryFrame = when (visualTheme) {
        "morning" -> 1
        "weather" -> 3
        "seasonal" -> 4
        "observatory" -> 5
        else -> if (isNight) 2 else 0
    }
    val cottages = if (isNight) PixelSheet.NightCottages else PixelSheet.Cottages
    val house = rememberSheet(cottages)
    val scenery = rememberSheet(PixelSheet.Scenery)
    val motion = rememberInfiniteTransition(label = "sky")
    val drift by motion.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(12000), RepeatMode.Reverse),
        label = "sky_drift"
    )
    PixelPanel(color = if (isNight) GroveNight else GroveSky) {
        Box(modifier = Modifier.fillMaxWidth().height(148.dp)) {
            Canvas(Modifier.matchParentSize()) {
                val width = size.width
                val height = size.height
                drawPixelFrame(scenery, PixelSheet.Scenery, sceneryFrame)
                val pixel = minOf(width / 224f, height / 95f)
                if (isNight && sceneryFrame != 2 && sceneryFrame != 5) {
                    drawRect(Color(0x553C476B))
                }
                // The cottage sits on the near bank of the lane across phone and tablet widths.
                drawPixelFrame(
                    house, cottages, themeRow * 8 + gameData.house.level.coerceIn(1, 8) - 1,
                    offset = IntOffset((width * 0.04f).roundToInt(), (height * 0.80f - 68 * pixel).roundToInt()),
                    destination = IntSize((112 * pixel).roundToInt(), (80 * pixel).roundToInt())
                )
                when (visualTheme) {
                    "weather" -> repeat(16) { i ->
                        val y = ((i * 0.19f + drift * 4) % 1f) * height
                        drawRect(Color(0xA4D2E4E4), Offset((i * 0.281f % 1f) * width, y), Size(pixel, 3 * pixel))
                    }
                    "cozy_lights", "golden_greenhouse" -> repeat(12) { i ->
                        val x = (i * 0.281f % 1f) * width
                        val y = height * (0.72f + (i % 3) * 0.07f) - (drift * 3).toInt() * pixel
                        drawRect(Color(0xFFF9DF94), Offset(x, y), Size(pixel, pixel))
                    }
                    else -> {
                        val x = width * 0.29f + (drift * 12).toInt() * pixel
                        val tint = Color.White.copy(alpha = if (isNight) 0.16f else 0.42f)
                        drawRect(tint, Offset(x, height * 0.15f), Size(18 * pixel, 2 * pixel))
                        drawRect(tint, Offset(x + 4 * pixel, height * 0.15f - 2 * pixel), Size(9 * pixel, 2 * pixel))
                    }
                }
            }
            Column(
                modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp)
                    .background(Color(0xD9384943)).padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalAlignment = Alignment.End
            ) {
                Text("Study Grove", fontSize = 26.sp, fontWeight = FontWeight.Black, color = GroveCream)
                Text(gameData.house.title, fontSize = 13.sp, color = GroveCream.copy(alpha = 0.86f))
            }
        }
    }
}

@Composable
private fun GardenGrid(
    gameData: GameData,
    placing: Boolean,
    isNight: Boolean,
    onTileTapped: (Int, Int) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val objectsByTile = remember(gameData.garden.objects) {
        buildMap {
            gameData.garden.objects.forEach { gardenObject ->
                val item = ShopCatalog.byId(gardenObject.shopItemId)
                GardenObjectFootprints.occupiedTiles(gardenObject, item).forEach { tile ->
                    if (tile !in this) put(tile, gardenObject)
                }
            }
        }
    }
    val hasAnimation = remember(gameData.garden.objects) {
        gameData.garden.objects.any { ShopCatalog.byId(it.shopItemId)?.visualKey in GardenArt.animated }
    }
    val animationFrame: (() -> Int)? = if (hasAnimation) {
        val transition = rememberInfiniteTransition(label = "garden")
        val phase = transition.animateFloat(
            initialValue = 0f,
            targetValue = 8f,
            animationSpec = infiniteRepeatable(tween(1200, easing = LinearEasing)),
            label = "garden_frame"
        )
        remember(phase) { { phase.value.toInt().coerceIn(0, 7) } }
    } else null
    val overlayObjects = remember(gameData.garden.objects) {
        gameData.garden.objects.filter { gardenObject ->
            val item = ShopCatalog.byId(gardenObject.shopItemId)
            GardenObjectFootprints.isLarge(item)
        }
    }
    ZoomableMapViewport(
        gridWidth = gameData.garden.width,
        gridHeight = gameData.garden.height,
        panelColor = Color(0xFF91AA67),
        maxScale = 4f
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val tiles = MapTileGrid(
                constraints.maxWidth, constraints.maxHeight, gameData.garden.width, gameData.garden.height, LocalDensity.current
            )
            Column(modifier = Modifier.fillMaxWidth()) {
                repeat(gameData.garden.height) { y ->
                    Row {
                        repeat(gameData.garden.width) { x ->
                            val obj = objectsByTile[x to y]
                            Box(Modifier.size(tiles.size(x, y)).clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onTileTapped(x, y)
                            }) {
                                val grass = (x * 7 + y * 11 + x * y * 3) % 16
                                PixelTile(PixelSheet.Grass, grass, Modifier.matchParentSize())
                                if (obj != null && !GardenObjectFootprints.isLarge(ShopCatalog.byId(obj.shopItemId))) {
                                    // The four bits select the matching rails at each cell edge.
                                    val mask = if (obj.type == GardenObjectType.FENCE) {
                                        connectionMask(x, y) { nx, ny ->
                                            objectsByTile[nx to ny]?.type == GardenObjectType.FENCE
                                        }
                                    } else 0
                                    GardenObjectSprite(obj, Modifier.matchParentSize(), mask, animationFrame)
                                }
                                if (placing) {
                                    Box(Modifier.matchParentSize().border(0.5.dp, GroveInk.copy(alpha = 0.30f)))
                                }
                            }
                        }
                    }
                }
            }
            overlayObjects.forEach { gardenObject ->
                val item = ShopCatalog.byId(gardenObject.shopItemId)
                val footprint = GardenObjectFootprints.sizeFor(item)
                GardenObjectSprite(
                    gardenObject = gardenObject,
                    animationFrame = animationFrame,
                    modifier = Modifier
                        .offset { tiles.offset(gardenObject.x, gardenObject.y) }
                        .size(tiles.size(gardenObject.x, gardenObject.y, footprint.width, footprint.height))
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onTileTapped(gardenObject.x, gardenObject.y)
                        }
                        .padding(2.dp)
                )
            }
            val tint = when {
                isNight -> Color(0x39323C68)
                gameData.upgrades.visualTheme == "seasonal" -> Color(0x20CC8838)
                else -> Color.Transparent
            }
            if (tint != Color.Transparent) Box(Modifier.matchParentSize().background(tint))
        }
    }
}

private fun houseThemeName(themeId: String): String =
    when (themeId) {
        HouseSpecialUnlocks.DreamLibraryTheme -> "Dream Library Theme"
        HouseSpecialUnlocks.CelestialTheme -> "Celestial Theme"
        HouseSpecialUnlocks.GrandEstateTheme -> "Grand Estate Theme"
        else -> themeId.replace('_', ' ').replaceFirstChar { it.uppercase() }
    }
