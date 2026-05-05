package com.studygrove.app.ui.house

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.studygrove.app.data.model.GameData
import com.studygrove.app.data.model.ShopCatalog
import com.studygrove.app.ui.components.HouseObjectSprite
import com.studygrove.app.ui.components.PixelButton
import com.studygrove.app.ui.components.PixelPanel
import com.studygrove.app.ui.components.SectionTitle
import com.studygrove.app.ui.components.ZoomableMapViewport
import com.studygrove.app.ui.inventory.HouseInventoryPanel
import com.studygrove.app.ui.theme.GroveBerry
import com.studygrove.app.ui.theme.GroveInk
import com.studygrove.app.ui.theme.GroveMoss

@Composable
fun HouseScreen(
    gameData: GameData,
    selectedItemId: String?,
    onTileTapped: (Int, Int) -> Unit,
    onSelectFurniture: (String) -> Unit,
    onClearSelection: () -> Unit
) {
    var showHouseInventory by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier.verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            SectionTitle(gameData.house.title, Modifier.weight(1f))
            PixelButton("Furniture", color = GroveMoss, onClick = { showHouseInventory = true })
        }
        selectedItemId?.let { itemId ->
            PixelPanel(color = Color(0xFFFFE9BD)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Placing ${ShopCatalog.byId(itemId)?.name.orEmpty()}", fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
                    PixelButton("Cancel", color = GroveBerry, onClick = onClearSelection)
                }
            }
        }
        HouseGrid(gameData = gameData, onTileTapped = onTileTapped)
    }
    if (showHouseInventory) {
        AlertDialog(
            onDismissRequest = { showHouseInventory = false },
            title = {
                Text("House inventory", fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
            },
            text = {
                HouseInventoryPanel(gameData = gameData) { itemId ->
                    showHouseInventory = false
                    onSelectFurniture(itemId)
                }
            },
            confirmButton = {
                PixelButton("Close", color = GroveBerry) { showHouseInventory = false }
            }
        )
    }
}

@Composable
private fun HouseGrid(gameData: GameData, onTileTapped: (Int, Int) -> Unit) {
    val haptic = LocalHapticFeedback.current
    val objectsByTile = remember(gameData.house.objects) { gameData.house.objects.associateBy { it.x to it.y } }
    ZoomableMapViewport(
        gridWidth = gameData.house.width,
        gridHeight = gameData.house.height,
        panelColor = Color(0xFFC88A58),
        maxScale = 3.5f
    ) {
        BoxWithConstraints(Modifier.fillMaxSize()) {
            val tileSize = maxWidth / gameData.house.width
            Column(Modifier.fillMaxWidth()) {
                repeat(gameData.house.height) { y ->
                    Row {
                        repeat(gameData.house.width) { x ->
                            val obj = objectsByTile[x to y]
                            Box(
                                modifier = Modifier
                                    .size(tileSize)
                                    .background(if ((x + y) % 2 == 0) Color(0xFFE8B27A) else Color(0xFFD79A62))
                                    .border(0.5.dp, GroveInk.copy(alpha = 0.28f))
                                    .clickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        onTileTapped(x, y)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                obj?.let {
                                    HouseObjectSprite(it, Modifier.matchParentSize().padding(2.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
